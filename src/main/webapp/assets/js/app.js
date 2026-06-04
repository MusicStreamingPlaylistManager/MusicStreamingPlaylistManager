// ===== SOUNDWAVE - GLOBAL APP JS =====
// Handles: player state, API calls to Servlets, shared utilities

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
  };

  // Simulated progress timer (real audio playback would hook into <audio> events)
  let progressTimer = null;
  let progressSeconds = 0;
  let durationSeconds = 0;

  // --- API helpers ---
  const API = {
    base: window.location.origin + '/soundwave',

    async get(url) {
      try {
        const r = await fetch(this.base + url);
        if (!r.ok) throw new Error(r.statusText);
        return await r.json();
      } catch(e) { console.error('GET', url, e); return null; }
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
        if (!r.ok) throw new Error(r.statusText);
        return await r.json();
      } catch(e) { console.error('POST form', url, e); return null; }
    }
  };

  // --- Player ---
  function playTrack(track) {
    state.currentTrack = track;
    state.isPlaying = true;
    progressSeconds = 0;
    durationSeconds = track.duration || 0;

    _updatePlayerUI();
    _startProgress();
    _syncWaitList();
    showToast('▶ ' + track.title);

    // Notify server: push to history stack, update session waiting list
    API.postForm('/api/player/play', { songId: track.songId })
       .then(res => { if (res && res.waitList) _setWaitList(res.waitList); });
  }

  function togglePlay() {
    if (!state.currentTrack) return;
    state.isPlaying = !state.isPlaying;
    _updatePlayBtns();
    if (state.isPlaying) _startProgress(); else _stopProgress();
    showToast(state.isPlaying ? '▶ Resume' : '⏸ Paused');
  }

  function nextTrack() {
    API.get('/api/player/next').then(res => {
      if (res && res.track) playTrack(res.track);
      else showToast('⏭ End of queue');
    });
  }

  function prevTrack() {
    API.get('/api/player/prev').then(res => {
      if (res && res.track) playTrack(res.track);
      else showToast('⏮ Start of queue');
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
    progressSeconds = Math.round(pct * durationSeconds / 100);
    _updateProgressUI();
  }

  function setVolume(val) {
    state.volume = val;
    const fill = document.querySelectorAll('.vol-slider');
    fill.forEach(s => s.value = val);
  }

  // Favorites
  async function toggleFavorite(songId, btn) {
    const isFav = state.favorites.has(songId);
    const res = await API.postForm('/api/favorites/toggle', { songId });
    if (!res) return;
    if (isFav) {
      state.favorites.delete(songId);
      btn && btn.classList.remove('liked');
    } else {
      state.favorites.add(songId);
      btn && btn.classList.add('liked');
    }
    showToast(isFav ? '💔 Removed from favourites' : '❤️ Added to favourites');
    // Refresh if on favourites page
    if (typeof renderFavourites === 'function') renderFavourites();
  }

  // Seed favorites from server on page load
  async function loadFavorites() {
    const res = await API.get('/api/favorites');
    if (res && res.songIds) state.favorites = new Set(res.songIds);
  }

  // --- Progress ---
  function _startProgress() {
    _stopProgress();
    if (durationSeconds <= 0) return;
    progressTimer = setInterval(() => {
      progressSeconds++;
      state.progressPct = (progressSeconds / durationSeconds) * 100;
      if (state.progressPct >= 100) {
        state.progressPct = 100;
        _stopProgress();
        // Auto-next after track ends
        setTimeout(() => nextTrack(), 500);
      }
      _updateProgressUI();
    }, 1000);
  }

  function _stopProgress() {
    if (progressTimer) clearInterval(progressTimer);
    progressTimer = null;
  }

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

  // --- Init ---
  function init() {
    initTheme();
    initProgressBars();
    initVolume();
    initKeyboard();
    loadFavorites();
  }

  return {
    init, state, API,
    playTrack, togglePlay, nextTrack, prevTrack,
    toggleShuffle, toggleLoop, seekTo, toggleFavorite,
    showToast, loadFavorites,
    getState: () => state,
  };
})();

document.addEventListener('DOMContentLoaded', () => App.init());
