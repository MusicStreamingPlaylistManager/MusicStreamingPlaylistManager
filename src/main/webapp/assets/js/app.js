// ===== SOUNDWAVE - GLOBAL APP JS =====
// Handles: player state, API calls to Servlets, shared utilities

function resolveContextPath() {
  if (window.APP_CONTEXT) return window.APP_CONTEXT;
  const path = window.location.pathname;
  const slash = path.lastIndexOf('/');
  return slash > 0 ? path.substring(0, slash) : '';
}

const APP_BASE = window.location.origin + resolveContextPath();

const App = (() => {
  // --- State ---
  let state = {
    currentTrack: null,   // { songId, title, artist, genre, duration, coverPath }
    isPlaying: false,
    isShuffle: false,
    isLoop: false,        // false | 'one' | 'all'
    progressPct: 0,
    volume: 75,
    waitList: [],         // array of track objects (in-memory DLL on server, reflected here)
    favorites: new Set(),
    activeSongIdForModal: null,
    modalPlaylists: [],
    originalModalPlaylists: [],
  };

  let progressSeconds = 0;
  let durationSeconds = 0;

  // --- API helpers ---
  const API = {
    base: APP_BASE,

    async get(url) {
      try {
        const fetchUrl = this.base + url + (url.includes('?') ? '&' : '?') + '_t=' + Date.now();
        const r = await fetch(fetchUrl, { cache: 'no-store' });
        const text = await r.text();
        let data;
        try {
          data = text ? JSON.parse(text) : null;
        } catch (parseErr) {
          console.error('GET parse error', url, text);
          return { error: 'Invalid JSON from server', _failed: true };
        }
        if (!r.ok) {
          console.error('GET', url, r.status, data);
          return { error: (data && data.error) || r.statusText, _failed: true };
        }
        return data;
      } catch(e) {
        console.error('GET', url, e);
        return { error: e.message, _failed: true };
      }
    },

    async post(url, body) {
      try {
        const r = await fetch(this.base + url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        });
        if (!r.ok) throw new Error(r.statusText);
        return await r.json();
      } catch(e) { console.error('POST', url, e); return null; }
    },

    async postForm(url, params) {
      try {
        const r = await fetch(this.base + url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: new URLSearchParams(params)
        });
        const text = await r.text();
        return text ? JSON.parse(text) : null;
      } catch(e) { console.error('POST form', url, e); return null; }
    }
  };

  // HTML5 Audio player
  let audio = new Audio();

  function initAudio() {
    audio.addEventListener('timeupdate', () => {
      if (!audio.duration || isNaN(audio.duration)) return;
      progressSeconds = Math.floor(audio.currentTime);
      durationSeconds = Math.floor(audio.duration);
      state.progressPct = (audio.currentTime / audio.duration) * 100;
      _updateProgressUI();
    });

    audio.addEventListener('loadedmetadata', () => {
      if (!audio.duration || isNaN(audio.duration)) return;
      durationSeconds = Math.floor(audio.duration);
      _updateProgressUI();
    });

    audio.addEventListener('ended', () => {
      if (state.isLoop === 'one') {
        audio.currentTime = 0;
        audio.play().catch(() => {});
        return;
      }
      nextTrack();
    });

    audio.addEventListener('play', () => {
      state.isPlaying = true;
      _updatePlayBtns();
    });

    audio.addEventListener('pause', () => {
      state.isPlaying = false;
      _updatePlayBtns();
    });
  }

  function _audioSrc(track) {
    if (!track || !track.filePath) return '';
    return resolveContextPath() + track.filePath;
  }

  function _loadAndPlay(track) {
    const src = _audioSrc(track);
    if (!src) return;

    const needsReload = !audio.src || !audio.src.endsWith(track.filePath);
    audio.volume = state.volume / 100;

    if (needsReload) {
      audio.src = src;
      audio.load();
    }

    if (state.isPlaying) {
      audio.play().catch(err => console.warn('Audio play blocked:', err));
    }
  }

  // --- Player ---
  function applyTrack(track, waitList, autoplay) {
    if (autoplay === undefined) autoplay = true;

    state.currentTrack = track;
    state.isPlaying = autoplay;
    progressSeconds = 0;
    durationSeconds = track.duration || 0;

    _updatePlayerUI();
    _loadAndPlay(track);

    if (waitList) _setWaitList(waitList);
  }

  function playTrack(track) {
    applyTrack(track);
    showToast('▶ ' + track.title);

    API.postForm('/api/player/play', { songId: track.songId })
       .then(res => {
         if (res && res.track) applyTrack(res.track, res.waitList);
         else if (res && res.waitList) _setWaitList(res.waitList);
       });
  }

  function togglePlay() {
    if (!state.currentTrack) return;
    state.isPlaying = !state.isPlaying;
    if (state.isPlaying) {
      audio.play().catch(err => console.warn('Audio play blocked:', err));
    } else {
      audio.pause();
    }
    _updatePlayBtns();
    showToast(state.isPlaying ? '▶ Resume' : '⏸ Paused');
  }

  function nextTrack() {
    API.get('/api/player/next').then(res => {
      if (res && res.track) {
        applyTrack(res.track, res.waitList);
        showToast('▶ ' + res.track.title);
      } else {
        showToast('⏭ End of queue');
      }
    });
  }

  function prevTrack() {
    API.get('/api/player/prev').then(res => {
      if (res && res.track) {
        applyTrack(res.track, res.waitList);
        showToast('▶ ' + res.track.title);
      } else {
        showToast('⏮ Start of queue');
      }
    });
  }

  function toggleShuffle() {
    state.isShuffle = !state.isShuffle;
    _updateCtrlBtns();
    showToast(state.isShuffle ? '🔀 Shuffle on' : '🔀 Shuffle off');
    API.postForm('/api/player/shuffle', { enabled: state.isShuffle });
  }

  function toggleLoop() {
    // Cycle: false → 'all' → 'one' → false
    if (!state.isLoop) state.isLoop = 'all';
    else if (state.isLoop === 'all') state.isLoop = 'one';
    else state.isLoop = false;
    _updateCtrlBtns();
    const labels = { false: 'Loop off', all: '🔁 Repeat all', one: '🔂 Repeat one' };
    showToast(labels[state.isLoop]);
    API.postForm('/api/player/loop', { mode: state.isLoop });
  }

  function seekTo(pct) {
    state.progressPct = pct;
    if (audio.duration && !isNaN(audio.duration)) {
      audio.currentTime = (pct / 100) * audio.duration;
      progressSeconds = Math.floor(audio.currentTime);
    } else {
      progressSeconds = Math.round(pct * durationSeconds / 100);
    }
    _updateProgressUI();
  }

  function setVolume(val) {
    state.volume = val;
    audio.volume = val / 100;
    document.querySelectorAll('.vol-slider').forEach(s => s.value = val);
  }

  async function saveWaitingAsPlaylist(name) {
    const res = await API.postForm('/api/player/save-waiting', { name });
    if (res && res.success) {
      showToast('✅ Saved as playlist');
      return res.playlistId;
    }
    showToast('⚠ Could not save playlist');
    return null;
  }

  // Favorites
  async function toggleFavorite(songId, btn) {
    if (!songId) return;
    await showAddToPlaylistModal(songId);
  }

  // Seed favorites from server on page load
  async function loadFavorites() {
    const res = await API.get('/api/favorites');
    if (res && res.songIds) {
      state.favorites = new Set(res.songIds);
      if (state.currentTrack) {
        syncHeartButtons(state.currentTrack.songId, state.favorites.has(state.currentTrack.songId));
      }
    }
  }

  async function showAddToPlaylistModal(songId) {
    if (!songId) return;
    state.activeSongIdForModal = parseInt(songId, 10);
    const modal = document.getElementById('addToPlaylistModal');
    const search = document.getElementById('playlistSearchInput');
    const target = document.getElementById('modalPlaylistSections');
    if (!modal || !target) return;
    if (search) search.value = '';
    modal.classList.add('show');
    target.innerHTML = '<div class="modal-empty-state">Loading playlists...</div>';
    const res = await API.get('/api/playlists?songId=' + encodeURIComponent(state.activeSongIdForModal));
    if (!res || res._failed || !res.playlists) {
      target.innerHTML = '<div class="modal-empty-state">Could not load playlists.</div>';
      return;
    }
    state.modalPlaylists = JSON.parse(JSON.stringify(res.playlists || []));
    state.originalModalPlaylists = JSON.parse(JSON.stringify(res.playlists || []));
    renderModalPlaylists(state.modalPlaylists);
  }

  function closeAddToPlaylistModal(event) {
    if (event && event.target && event.target.id !== 'addToPlaylistModal') return;
    const modal = document.getElementById('addToPlaylistModal');
    if (modal) modal.classList.remove('show');
  }

  function renderModalPlaylists(playlists) {
    const target = document.getElementById('modalPlaylistSections');
    if (!target) return;
    const list = playlists || [];
    const defaultPlaylists = list.filter(pl => pl.isDefault);
    const customPlaylists = list.filter(pl => !pl.isDefault);
    const renderRow = (pl) => {
      const checked = !!pl.containsSong;
      const icon = pl.isDefault
        ? '<div class="modal-playlist-icon liked-icon"><svg><use href="#ic-heart"/></svg></div>'
        : '<div class="modal-playlist-icon music-icon">♪</div>';
      const songCount = pl.songCount || 0;
      const countText = songCount + (songCount === 1 ? ' song' : ' songs');
      return `
        <div class="modal-playlist-row ${checked ? 'checked' : ''}"
             data-playlist-name="${_escapeAttr(pl.name || '')}">
          ${icon}
          <span class="modal-playlist-info">
            <span class="modal-playlist-name">${_escapeHtml(pl.name || 'Untitled playlist')}</span>
            <span class="modal-playlist-meta">${countText}</span>
          </span>
          <span class="modal-checkmark-btn ${checked ? 'checked' : ''}"
                onclick="event.stopPropagation(); App.toggleSongInPlaylist(${pl.playlistId}, ${checked}, ${!!pl.isDefault})"
                title="${checked ? 'Remove from playlist' : 'Add to playlist'}">
            ${checked ? '✓' : ''}
          </span>
        </div>`;
    };
    let html = '';
    if (defaultPlaylists.length) {
      html += '<div class="modal-section-label">Saved in</div>';
      html += defaultPlaylists.map(renderRow).join('');
    }
    html += '<div class="modal-section-label">Recently updated</div>';
    html += customPlaylists.length
      ? customPlaylists.map(renderRow).join('')
      : '<div class="modal-empty-state">No custom playlists found.</div>';
    target.innerHTML = html;
  }

  function toggleSongInPlaylist(playlistId, isCurrentlyChecked, isDefault) {
    const songId = state.activeSongIdForModal;
    if (!songId || !playlistId) return;
    state.modalPlaylists = state.modalPlaylists.map(pl => {
      if (pl.playlistId !== playlistId) return pl;
      const next = Object.assign({}, pl);
      next.containsSong = !isCurrentlyChecked;
      next.songCount = Math.max(0, (next.songCount || 0) + (isCurrentlyChecked ? -1 : 1));
      return next;
    });
    renderModalPlaylists(state.modalPlaylists);
  }

  async function saveAddToPlaylistModal() {
    const songId = state.activeSongIdForModal;
    if (!songId) {
      closeAddToPlaylistModal();
      return;
    }
    const changes = [];
    for (let i = 0; i < state.modalPlaylists.length; i++) {
      const pl = state.modalPlaylists[i];
      const orig = state.originalModalPlaylists.find(o => o.playlistId === pl.playlistId);
      if (orig && orig.containsSong !== pl.containsSong) {
        changes.push({
          playlistId: pl.playlistId,
          add: pl.containsSong,
          isDefault: pl.isDefault
        });
      }
    }
    if (changes.length === 0) {
      closeAddToPlaylistModal();
      return;
    }
    const promises = changes.map(change => {
      const url = change.add ? '/api/playlists/addSong' : '/api/playlists/removeSong';
      return API.postForm(url, { playlistId: change.playlistId, songId });
    });
    await Promise.all(promises);
    const defaultFav = state.modalPlaylists.find(pl => pl.isDefault);
    if (defaultFav) {
      syncHeartButtons(songId, defaultFav.containsSong);
      if (defaultFav.containsSong) {
        state.favorites.add(songId);
      } else {
        state.favorites.delete(songId);
      }
    }
    showToast('Add successfully');
    if (typeof renderFavourites === 'function') renderFavourites();
    if (typeof loadDetail === 'function') loadDetail();
    closeAddToPlaylistModal();
  }

  function filterPlaylistModalList(query) {
    const q = (query || '').trim().toLowerCase();
    if (!q) {
      renderModalPlaylists(state.modalPlaylists);
      return;
    }
    renderModalPlaylists(state.modalPlaylists.filter(pl => (pl.name || '').toLowerCase().includes(q)));
  }

  function openCreatePlaylistFromModal() {
    const modal = document.getElementById('modalCreatePlaylist');
    const input = document.getElementById('modalPlNameInput');
    if (input) input.value = '';
    if (modal) modal.classList.add('show');
    setTimeout(() => input && input.focus(), 0);
  }

  function closeCreatePlaylistFromModal(event) {
    if (event && event.target && event.target.id !== 'modalCreatePlaylist') return;
    const modal = document.getElementById('modalCreatePlaylist');
    if (modal) modal.classList.remove('show');
  }

  async function confirmCreatePlaylistFromModal() {
    const input = document.getElementById('modalPlNameInput');
    const name = input ? input.value.trim() : '';
    const songId = state.activeSongIdForModal;
    if (!name) {
      showToast('Playlist name is required');
      return;
    }
    const createRes = await API.postForm('/api/playlists/create', { name });
    if (!createRes || !createRes.success || !createRes.playlistId || createRes.playlistId <= 0) {
      showToast('Could not create playlist');
      return;
    }
    if (songId) {
      await API.postForm('/api/playlists/addSong', {
        playlistId: createRes.playlistId,
        songId
      });
    }
    closeCreatePlaylistFromModal();
    showToast('Playlist created');
    if (songId) await showAddToPlaylistModal(songId);
  }

  function syncHeartButtons(songId, isLiked) {
    if (!songId) return;
    const id = parseInt(songId, 10);
    if (isLiked) state.favorites.add(id);
    else state.favorites.delete(id);
    document.querySelectorAll(`[data-song-id="${id}"]`).forEach(btn => {
      if (btn.classList && (btn.classList.contains('heart-btn') || btn.classList.contains('player-heart'))) {
        btn.classList.toggle('liked', isLiked);
      }
    });
  }

  function _escapeHtml(value) {
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function _escapeAttr(value) {
    return _escapeHtml(value).replace(/`/g, '&#96;');
  }

  // --- Progress (driven by audio.timeupdate) ---
  function _formatTime(secs) {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return m + ':' + String(s).padStart(2, '0');
  }

  // --- UI sync ---
  function _updatePlayerUI() {
    const t = state.currentTrack;
    if (!t) return;
    // Thumb
    document.querySelectorAll('.player-thumb').forEach(el => {
      el.innerHTML = t.coverPath
        ? `<img src="${t.coverPath}" alt="">`
        : `<span style="font-size:1.5rem">${t.emoji || '🎵'}</span>`;
    });
    document.querySelectorAll('.player-name').forEach(el => el.textContent = t.title);
    document.querySelectorAll('.player-artist').forEach(el => el.textContent = t.artist);
    // Now playing page specific
    const npCover = document.getElementById('np-cover');
    if (npCover) {
      npCover.innerHTML = t.coverPath
        ? `<img src="${t.coverPath}" alt="" style="width:100%;height:100%;object-fit:cover;border-radius:20px">`
        : `<span style="font-size:5rem">${t.emoji || '🎵'}</span>`;
    }
    if (document.getElementById('np-title')) document.getElementById('np-title').textContent = t.title;
    if (document.getElementById('np-artist')) document.getElementById('np-artist').textContent = t.artist;
    const heart = document.getElementById('bar-heart');
    if (heart) {
      heart.dataset.songId = t.songId;
      heart.classList.toggle('liked', state.favorites.has(t.songId));
    }
    _updatePlayBtns();
    _updateProgressUI();
  }

  function _updatePlayBtns() {
    const icon = state.isPlaying
      ? `<svg viewBox="0 0 24 24"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>`
      : `<svg viewBox="0 0 24 24"><polygon points="5,3 19,12 5,21"/></svg>`;
    document.querySelectorAll('.p-play').forEach(b => b.innerHTML = icon);
  }

  function _updateProgressUI() {
    const pct = state.progressPct;
    document.querySelectorAll('.progress-fill').forEach(el => el.style.width = pct + '%');
    const cur = _formatTime(progressSeconds);
    const total = _formatTime(durationSeconds);
    const times = document.querySelectorAll('.p-time-cur');
    times.forEach(el => el.textContent = cur);
    document.querySelectorAll('.p-time-total').forEach(el => el.textContent = total);
    // Now playing page progress
    const npFill = document.getElementById('np-progress');
    if (npFill) npFill.style.width = pct + '%';
    const npCur = document.getElementById('np-time-cur');
    if (npCur) npCur.textContent = cur;
    const npTotal = document.getElementById('np-time-total');
    if (npTotal) npTotal.textContent = total;
  }

  function _updateCtrlBtns() {
    document.querySelectorAll('[data-ctrl="shuffle"]').forEach(b => {
      b.classList.toggle('active', state.isShuffle);
    });
    document.querySelectorAll('[data-ctrl="loop"]').forEach(b => {
      b.classList.toggle('active', !!state.isLoop);
    });
  }

  function _setWaitList(list) {
    state.waitList = list;
    _syncWaitList();
  }

  function _syncWaitList() {
    // Re-render wait list on now playing page if visible
    if (typeof renderWaitList === 'function') renderWaitList(state.waitList);
  }

  // --- Utils ---
  let toastTimer;
  function showToast(msg) {
    let t = document.getElementById('sw-toast');
    if (!t) {
      t = document.createElement('div');
      t.id = 'sw-toast';
      t.className = 'toast';
      document.body.appendChild(t);
    }
    t.textContent = msg;
    t.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => t.classList.remove('show'), 2200);
  }

  // Theme toggle
  function initTheme() {
    const saved = localStorage.getItem('sw-theme') || 'light';
    if (saved === 'dark') document.body.classList.add('dark');
    document.querySelectorAll('.theme-toggle').forEach(btn => {
      btn.addEventListener('click', () => {
        document.body.classList.toggle('dark');
        localStorage.setItem('sw-theme', document.body.classList.contains('dark') ? 'dark' : 'light');
      });
    });
  }

  // Progress bar click
  function initProgressBars() {
    document.querySelectorAll('.progress-track').forEach(bar => {
      bar.addEventListener('click', e => {
        const rect = bar.getBoundingClientRect();
        const pct = ((e.clientX - rect.left) / rect.width) * 100;
        seekTo(Math.max(0, Math.min(100, pct)));
      });
    });
  }

  // Volume slider
  function initVolume() {
    document.querySelectorAll('.vol-slider').forEach(s => {
      s.value = state.volume;
      s.addEventListener('input', () => setVolume(parseInt(s.value)));
    });
  }

  // Keyboard shortcuts
  function initKeyboard() {
    document.addEventListener('keydown', e => {
      if (['INPUT', 'TEXTAREA'].includes(e.target.tagName)) return;
      if (e.code === 'Space') { e.preventDefault(); togglePlay(); }
      if (e.code === 'ArrowRight') nextTrack();
      if (e.code === 'ArrowLeft') prevTrack();
    });
  }

  // --- SPA Router (AJAX + History API) ---
  const SPA_PAGES = {
    'home.jsp': 'home',
    'search.jsp': 'search',
    'playlist.jsp': 'playlist',
    'playlist-detail.jsp': 'playlist-detail',
    'nowplaying.jsp': 'nowplaying',
    'profile.jsp': 'profile',
    'admin-songs.jsp': 'admin',
  };

  const Router = {
    currentPage: null,
    navigating: false,

    init() {
      this.currentPage = this.resolvePage(window.location.pathname);
      history.replaceState({ spa: true, url: window.location.href }, '', window.location.href);
      this.updateNav(this.currentPage);
      document.addEventListener('click', e => this.onClick(e));
      window.addEventListener('popstate', e => this.onPopState(e));
      this.runSearchUrlState();
    },

    resolvePage(pathname) {
      const file = pathname.substring(pathname.lastIndexOf('/') + 1);
      return SPA_PAGES[file] || null;
    },

    isSpaUrl(url) {
      try {
        const u = new URL(url, APP_BASE + '/');
        if (u.origin !== window.location.origin) return false;
        const ctx = resolveContextPath();
        if (ctx && !u.pathname.startsWith(ctx + '/')) return false;
        return !!this.resolvePage(u.pathname);
      } catch (e) {
        return false;
      }
    },

    normalizeUrl(url) {
      return new URL(url, APP_BASE + '/').href;
    },

    onClick(e) {
      const link = e.target.closest('a[href]');
      if (!link || link.target === '_blank' || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
      const href = link.getAttribute('href');
      if (!href || href.startsWith('#') || href.includes('AuthServlet')) return;
      const full = this.normalizeUrl(link.href);
      if (!this.isSpaUrl(full)) return;
      e.preventDefault();
      this.navigate(full);
    },

    onPopState(e) {
      const targetPage = this.resolvePage(window.location.pathname);
      if (targetPage === this.currentPage && targetPage === 'search') {
        this.runSearchUrlState();
        return;
      }
      this.navigate(window.location.href, { push: false });
    },

    updateNav(pageKey) {
      document.querySelectorAll('#sidebar .nav-item[data-page]').forEach(el => {
        el.classList.toggle('active', el.dataset.page === pageKey);
      });
    },

    cleanupPage() {
      if (window.__npSyncInterval) {
        clearInterval(window.__npSyncInterval);
        window.__npSyncInterval = null;
      }
      if (this.currentPage === 'search' && window.__defaultGlobalSearch) {
        window.handleGlobalSearch = window.__defaultGlobalSearch;
      }
    },

    applyPageStyles(doc) {
      let holder = document.getElementById('spa-page-styles');
      if (!holder) {
        holder = document.createElement('div');
        holder.id = 'spa-page-styles';
        document.head.appendChild(holder);
      }
      const styles = doc.querySelectorAll('head style');
      holder.innerHTML = Array.from(styles).map(s => s.outerHTML).join('');
    },

    runPageScripts(doc) {
      const scripts = Array.from(doc.body.querySelectorAll('script'));
      let afterAppJs = false;
      scripts.forEach(s => {
        if (s.src && s.src.includes('app.js')) {
          afterAppJs = true;
          return;
        }
        if (!afterAppJs) return;
        const text = s.textContent || '';
        if (text.includes('handleGlobalSearch') && text.includes('search-overlay')) return;
        const el = document.createElement('script');
        el.textContent = text;
        document.body.appendChild(el);
        el.remove();
      });
    },

    runSearchUrlState() {
      if (this.currentPage !== 'search') return;
      const params = new URLSearchParams(window.location.search);
      const genre = params.get('genre');
      if (genre && typeof window.filterGenre === 'function') {
        window.filterGenre(genre, false);
      } else if (typeof window.clearSearch === 'function') {
        window.clearSearch(false);
      }
    },

    async navigate(url, options = {}) {
      const { push = true } = options;
      const targetUrl = this.normalizeUrl(url);
      const targetPage = this.resolvePage(new URL(targetUrl).pathname);
      if (!targetPage) {
        window.location.href = targetUrl;
        return;
      }
      if (this.navigating) return;
      if (push && targetUrl === window.location.href && targetPage === this.currentPage) return;

      this.navigating = true;
      this.cleanupPage();

      try {
        const res = await fetch(targetUrl, {
          credentials: 'same-origin',
          headers: { 'X-Requested-With': 'SPA' },
        });
        if (!res.ok) throw new Error('HTTP ' + res.status);

        const html = await res.text();
        const doc = new DOMParser().parseFromString(html, 'text/html');
        const fetchedContent = doc.getElementById('page-content');
        const host = document.getElementById('page-content');
        if (!fetchedContent || !host) throw new Error('Missing #page-content');

        host.innerHTML = fetchedContent.innerHTML;
        if (doc.title) document.title = doc.title;
        this.applyPageStyles(doc);
        this.runPageScripts(doc);

        this.currentPage = targetPage;
        this.updateNav(targetPage);

        if (push) {
          history.pushState({ spa: true, url: targetUrl }, '', targetUrl);
        }

        initProgressBars();
        initVolume();
        _updatePlayerUI();
        this.runSearchUrlState();
      } catch (err) {
        console.error('SPA navigate failed, falling back to full load', err);
        window.location.href = targetUrl;
      } finally {
        this.navigating = false;
      }
    },
  };

  async function restorePlayerState() {
    const res = await API.get('/api/player/current');
    if (!res || res._failed || !res.track) return;

    if (res.loop === 'one') state.isLoop = 'one';
    else if (res.loop === 'all') state.isLoop = 'all';
    else state.isLoop = false;
    _updateCtrlBtns();

    applyTrack(res.track, res.waitList, false);
  }

  // --- Init ---
  async function init() {
    initAudio();
    initTheme();
    initProgressBars();
    initVolume();
    initKeyboard();
    await loadFavorites();
    await restorePlayerState();
    Router.init();
  }

  function renderTrackItem(t, num) {
    const isFav = state.favorites.has(t.songId);
    const cover = t.coverPath
      ? `<img src="${t.coverPath}" alt="">`
      : `<span style="font-size:1.3rem">${t.emoji || '🎵'}</span>`;
    const trackJson = JSON.stringify(t).replace(/"/g, '&quot;');
    return `
    <div class="track-item" ondblclick="App.playTrack(${trackJson})">
      <span class="track-num">${num}</span>
      <span class="track-play-icon"><svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5,3 19,12 5,21"/></svg></span>
      <div class="track-thumb">${cover}</div>
      <div class="track-info">
        <div class="t-name">${t.title}</div>
        <div class="t-artist">${t.artist}</div>
      </div>
      <div class="track-actions">
        <button class="heart-btn ${isFav ? 'liked' : ''}"
                data-song-id="${t.songId}"
                onclick="event.stopPropagation(); App.toggleFavorite(${t.songId}, this)"
                title="Favourite">
          <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor"
               fill="${isFav ? 'currentColor' : 'none'}"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
          </svg>
        </button>
        <span class="track-dur">${t.durationStr || ''}</span>
      </div>
    </div>`;
  }

  return {
    init, state, API, Router,
    playTrack, applyTrack, togglePlay, nextTrack, prevTrack,
    toggleShuffle, toggleLoop, seekTo, setVolume, toggleFavorite, saveWaitingAsPlaylist,
    showToast, loadFavorites, showAddToPlaylistModal, closeAddToPlaylistModal, saveAddToPlaylistModal,
    renderModalPlaylists, toggleSongInPlaylist, filterPlaylistModalList,
    openCreatePlaylistFromModal, closeCreatePlaylistFromModal,
    confirmCreatePlaylistFromModal, syncHeartButtons,
    getState: () => state,
    renderTrackItem,
  };
})();

// Global alias for inline scripts in JSP pages
function renderTrackItem(t, num) {
  return App.renderTrackItem(t, num);
}

// SPA navigation helper for inline scripts
function spaNavigate(url) {
  if (App.Router && App.Router.isSpaUrl(url)) {
    App.Router.navigate(url);
  } else {
    window.location.href = url;
  }
}

document.addEventListener('DOMContentLoaded', () => App.init());
