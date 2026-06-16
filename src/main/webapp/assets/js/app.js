// ===== SOUNDWAVE - GLOBAL APP JS =====
// Handles: player state, API calls to Servlets, shared utilities

function resolveContextPath() {
    if (window.APP_CONTEXT)
        return window.APP_CONTEXT;
    const path = window.location.pathname;
    const slash = path.lastIndexOf('/');
    return slash > 0 ? path.substring(0, slash) : '';
}

const APP_BASE = window.location.origin + resolveContextPath();

const App = (() => {
    // --- State ---
    let state = {
        currentTrack: null, // { songId, title, artist, genre, duration, coverPath }
        isPlaying: false,
        isShuffle: false,
        isLoop: false, // false | 'one' | 'all'
        progressPct: 0,
        volume: 75,
        waitList: [], // array of track objects (in-memory DLL on server, reflected here)
        favorites: new Set(),
    };

    let progressSeconds = 0;
    let durationSeconds = 0;

    // --- API helpers ---
    const API = {
        base: APP_BASE,

        async get(url) {
            try {
                const r = await fetch(this.base + url);
                const text = await r.text();
                let data;
                try {
                    data = text ? JSON.parse(text) : null;
                } catch (parseErr) {
                    console.error('GET parse error', url, text);
                    return {error: 'Invalid JSON from server', _failed: true};
                }
                if (!r.ok) {
                    console.error('GET', url, r.status, data);
                    return {error: (data && data.error) || r.statusText, _failed: true};
                }
                return data;
            } catch (e) {
                console.error('GET', url, e);
                return {error: e.message, _failed: true};
            }
        },

        async post(url, body) {
            try {
                const r = await fetch(this.base + url, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(body)
                });
                if (!r.ok)
                    throw new Error(r.statusText);
                return await r.json();
            } catch (e) {
                console.error('POST', url, e);
                return null;
            }
        },

        async postForm(url, params) {
            try {
                const r = await fetch(this.base + url, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: new URLSearchParams(params)
                });
                const text = await r.text();
                return text ? JSON.parse(text) : null;
            } catch (e) {
                console.error('POST form', url, e);
                return null;
            }
        }
    };

    // HTML5 Audio player — dùng window._swAudio để giữ audio sống qua các lần chuyển trang
    if (!window._swAudio) {
        window._swAudio = new Audio();
    }
    let audio = window._swAudio;

    function initAudio() {
        // Xóa listener cũ trước khi add mới để tránh chồng chéo khi chuyển trang
        audio.removeEventListener('timeupdate', onTimeUpdate);
        audio.removeEventListener('loadedmetadata', onLoadedMetadata);
        audio.removeEventListener('ended', onEnded);
        audio.removeEventListener('play', onPlay);
        audio.removeEventListener('pause', onPause);

        audio.addEventListener('timeupdate', onTimeUpdate);
        audio.addEventListener('loadedmetadata', onLoadedMetadata);
        audio.addEventListener('ended', onEnded);
        audio.addEventListener('play', onPlay);
        audio.addEventListener('pause', onPause);
    }

    function onTimeUpdate() {
        if (!audio.duration || isNaN(audio.duration))
            return;
        progressSeconds = Math.floor(audio.currentTime);
        durationSeconds = Math.floor(audio.duration);
        state.progressPct = (audio.currentTime / audio.duration) * 100;
        _updateProgressUI();
    }

    function onLoadedMetadata() {
        if (!audio.duration || isNaN(audio.duration))
            return;
        durationSeconds = Math.floor(audio.duration);
        _updateProgressUI();
    }

    function onEnded() {
        if (state.isLoop === 'one') {
            audio.currentTime = 0;
            audio.play().catch(() => {
            });
            return;
        }
        nextTrack();
    }

    function onPlay() {
        state.isPlaying = true;
        _updatePlayBtns();
    }

    function onPause() {
        state.isPlaying = false;
        _updatePlayBtns();
    }

    function _loadAndPlay(track) {
        const src = _audioSrc(track);
        if (!src)
            return;

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
        if (autoplay === undefined)
            autoplay = true;

        state.currentTrack = track;
        // Lưu lại để restore khi chuyển trang
        sessionStorage.setItem('sw_current_track', JSON.stringify(track));
        state.isPlaying = autoplay;
        progressSeconds = 0;
        durationSeconds = track.duration || 0;

        _updatePlayerUI();
        _loadAndPlay(track);

        if (waitList)
            _setWaitList(waitList);
    }

    function playTrack(track) {
        applyTrack(track);
        showToast('▶ ' + track.title);

        API.postForm('/api/player/play', {songId: track.songId})
                .then(res => {
                    if (res && res.track)
                        applyTrack(res.track, res.waitList);
                    else if (res && res.waitList)
                        _setWaitList(res.waitList);
                });
    }

    function togglePlay() {
        if (!state.currentTrack)
            return;
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
        API.postForm('/api/player/shuffle', {enabled: state.isShuffle});
    }

    function toggleLoop() {
        // Cycle: false → 'all' → 'one' → false
        if (!state.isLoop)
            state.isLoop = 'all';
        else if (state.isLoop === 'all')
            state.isLoop = 'one';
        else
            state.isLoop = false;
        _updateCtrlBtns();
        const labels = {false: 'Loop off', all: '🔁 Repeat all', one: '🔂 Repeat one'};
        showToast(labels[state.isLoop]);
        API.postForm('/api/player/loop', {mode: state.isLoop});
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
        const res = await API.postForm('/api/player/save-waiting', {name});
        if (res && res.success) {
            showToast('✅ Saved as playlist');
            return res.playlistId;
        }
        showToast('⚠ Could not save playlist');
        return null;
    }

    // Favorites
    async function toggleFavorite(songId, btn) {
        const isFav = state.favorites.has(songId);
        const res = await API.postForm('/api/favorites/toggle', {songId});
        if (!res)
            return;
        if (isFav) {
            state.favorites.delete(songId);
            btn && btn.classList.remove('liked');
        } else {
            state.favorites.add(songId);
            btn && btn.classList.add('liked');
        }
        showToast(isFav ? '💔 Removed from favourites' : '❤️ Added to favourites');
        // Refresh if on favourites page
        if (typeof renderFavourites === 'function')
            renderFavourites();
    }

    // Seed favorites from server on page load
    async function loadFavorites() {
        const res = await API.get('/api/favorites');
        if (res && res.songIds)
            state.favorites = new Set(res.songIds);
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
        if (!t)
            return;
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
        if (document.getElementById('np-title'))
            document.getElementById('np-title').textContent = t.title;
        if (document.getElementById('np-artist'))
            document.getElementById('np-artist').textContent = t.artist;
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
        if (npFill)
            npFill.style.width = pct + '%';
        const npCur = document.getElementById('np-time-cur');
        if (npCur)
            npCur.textContent = cur;
        const npTotal = document.getElementById('np-time-total');
        if (npTotal)
            npTotal.textContent = total;
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
        if (typeof renderWaitList === 'function')
            renderWaitList(state.waitList);
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
        if (saved === 'dark')
            document.body.classList.add('dark');
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
            if (['INPUT', 'TEXTAREA'].includes(e.target.tagName))
                return;
            if (e.code === 'Space') {
                e.preventDefault();
                togglePlay();
            }
            if (e.code === 'ArrowRight')
                nextTrack();
            if (e.code === 'ArrowLeft')
                prevTrack();
        });
    }

    // --- Init ---
    function init() {
        initAudio();
        initTheme();
        initProgressBars();
        initVolume();
        initKeyboard();
        loadFavorites();

        // Restore bài đang phát khi chuyển trang
        const saved = sessionStorage.getItem('sw_current_track');
        if (saved) {
            try {
                const track = JSON.parse(saved);
                state.currentTrack = track;
                state.isPlaying = true;
                _loadAndPlay(track);
                _updatePlayerUI();
                _updateCtrlBtns();
            } catch (e) {
            }
        }
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
        init, state, API,
        playTrack, applyTrack, togglePlay, nextTrack, prevTrack,
        toggleShuffle, toggleLoop, seekTo, toggleFavorite, saveWaitingAsPlaylist,
        showToast, loadFavorites,
        getState: () => state,
        renderTrackItem,
    };
})();

// Global alias for inline scripts in JSP pages
function renderTrackItem(t, num) {
    return App.renderTrackItem(t, num);
}

document.addEventListener('DOMContentLoaded', () => App.init());
