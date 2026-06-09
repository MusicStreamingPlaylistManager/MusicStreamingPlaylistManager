<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
  if (session.getAttribute("user") == null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
  }
  request.setAttribute("currentPage", "nowplaying");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SoundWave — Now Playing</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@700;800;900&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
  <style>
    /* ---- NOW PLAYING PAGE ---- */
    .np-page {
      display: grid;
      grid-template-columns: 1fr 340px;
      gap: 2rem;
      height: 100%;
      padding: 2rem;
      align-items: start;
    }
    @media (max-width: 900px) { .np-page { grid-template-columns: 1fr; } }

    /* Player panel */
    .np-player {
      display: flex; flex-direction: column; align-items: center;
      max-width: 500px; margin: 0 auto; width: 100%;
    }

    .np-cover {
      width: 280px; height: 280px;
      border-radius: 20px;
      background: var(--surface2);
      display: flex; align-items: center; justify-content: center;
      font-size: 6rem;
      box-shadow: 0 20px 60px rgba(108,43,217,0.25);
      margin-bottom: 1.5rem;
      overflow: hidden;
      animation: floatCover 4s ease-in-out infinite;
    }
    .np-cover img { width: 100%; height: 100%; object-fit: cover; }
    @keyframes floatCover {
      0%, 100% { transform: translateY(0); }
      50%       { transform: translateY(-8px); }
    }

    .np-title {
      font-family: var(--font-display);
      font-size: 1.4rem; font-weight: 800; color: var(--text);
      margin-bottom: 4px; text-align: center;
    }
    .np-artist { color: var(--accent); font-size: 0.9rem; margin-bottom: 1.25rem; text-align: center; }

    /* Progress */
    .np-progress-wrap { width: 100%; margin-bottom: 1.25rem; }
    .np-times {
      display: flex; justify-content: space-between;
      font-size: 0.75rem; color: var(--text3); margin-bottom: 8px;
    }
    .np-progress-track {
      width: 100%; height: 4px;
      background: var(--bg3);
      border-radius: 999px; cursor: pointer; position: relative;
    }
    .np-progress-fill {
      height: 100%; border-radius: 999px;
      background: var(--accent);
      width: 0%; transition: width 0.5s linear; position: relative;
    }
    .np-progress-fill::after {
      content: '';
      position: absolute; right: -6px; top: 50%;
      transform: translateY(-50%);
      width: 14px; height: 14px; border-radius: 50%;
      background: var(--accent);
    }

    /* Controls */
    .np-controls {
      display: flex; align-items: center; gap: 20px; margin-bottom: 1.25rem;
    }
    .np-ctrl {
      background: none; border: none; color: var(--text3);
      cursor: pointer; display: flex; align-items: center; padding: 4px;
      transition: color var(--transition);
    }
    .np-ctrl svg { width: 22px; height: 22px; stroke: currentColor; fill: none; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round; }
    .np-ctrl:hover { color: var(--text); }
    .np-ctrl.active { color: var(--accent); }
    .np-play {
      width: 56px; height: 56px; border-radius: 50%;
      background: var(--accent); border: none; color: #fff;
      display: flex; align-items: center; justify-content: center;
      cursor: pointer;
      box-shadow: 0 6px 20px rgba(108,43,217,0.4);
      transition: background var(--transition), transform 0.1s;
    }
    .np-play:hover { background: var(--accent2); }
    .np-play:active { transform: scale(0.92); }
    .np-play svg { width: 22px; height: 22px; stroke: currentColor; fill: currentColor; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round; }

    /* Volume */
    .np-volume { display: flex; align-items: center; gap: 10px; }
    .np-volume svg { width: 18px; height: 18px; color: var(--text3); stroke: currentColor; fill: none; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round; }
    .np-vol-slider {
      width: 120px; height: 3px;
      appearance: none; -webkit-appearance: none;
      background: var(--bg3); border-radius: 999px;
      cursor: pointer; outline: none; border: none;
    }
    .np-vol-slider::-webkit-slider-thumb {
      -webkit-appearance: none;
      width: 12px; height: 12px; border-radius: 50%;
      background: var(--accent); cursor: pointer;
    }

    /* Wait List panel */
    .wait-list-panel {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 1.25rem;
      height: fit-content;
    }
    .wl-title {
      font-family: var(--font-display);
      font-size: 0.9rem; font-weight: 700;
      letter-spacing: 1px; text-transform: uppercase;
      color: var(--accent);
      margin-bottom: 0.875rem;
    }
    .wl-track {
      display: grid;
      grid-template-columns: 24px 36px 1fr auto;
      align-items: center;
      gap: 10px;
      padding: 0.5rem 0.5rem;
      border-radius: var(--radius-sm);
      cursor: pointer;
      transition: background var(--transition);
    }
    .wl-track:hover { background: var(--bg3); }
    .wl-track.current { background: var(--accent-soft); }
    .wl-track.current .wl-name { color: var(--accent); font-weight: 600; }
    .wl-num { font-size: 0.8rem; color: var(--text3); text-align: center; }
    .wl-thumb {
      width: 36px; height: 36px; border-radius: 6px;
      background: var(--bg3);
      display: flex; align-items: center; justify-content: center;
      font-size: 1.1rem; overflow: hidden;
    }
    .wl-thumb img { width: 100%; height: 100%; object-fit: cover; }
    .wl-name { font-size: 0.82rem; font-weight: 500; }
    .wl-artist { font-size: 0.72rem; color: var(--text3); margin-top: 1px; }
    .wl-dur { font-size: 0.75rem; color: var(--text3); }
    .wl-empty { color: var(--text3); font-size: 0.85rem; padding: 0.5rem; }
  </style>
</head>
<body>

<%@ include file="includes/layout-top.jspf" %>

    <div class="np-page">
      <!-- Left: Player -->
      <div class="np-player">
        <div class="np-cover" id="np-cover">
          <span style="font-size:6rem">🎵</span>
        </div>
        <div class="np-title" id="np-title">Select a song to play</div>
        <div class="np-artist" id="np-artist">—</div>

        <!-- Progress -->
        <div class="np-progress-wrap">
          <div class="np-times">
            <span id="np-time-cur">0:00</span>
            <span id="np-time-total">0:00</span>
          </div>
          <div class="np-progress-track" onclick="handleSeek(event, this)">
            <div class="np-progress-fill" id="np-progress"></div>
          </div>
        </div>

        <!-- Controls -->
        <div class="np-controls">
          <button class="np-ctrl" data-ctrl="shuffle" onclick="App.toggleShuffle()" title="Shuffle">
            <svg><use href="#ic-shuffle"/></svg>
          </button>
          <button class="np-ctrl" onclick="App.prevTrack()" title="Previous">
            <svg><use href="#ic-skip-back"/></svg>
          </button>
          <button class="np-play" onclick="App.togglePlay()" title="Play/Pause">
            <svg viewBox="0 0 24 24"><polygon points="5,3 19,12 5,21"/></svg>
          </button>
          <button class="np-ctrl" onclick="App.nextTrack()" title="Next">
            <svg><use href="#ic-skip-fwd"/></svg>
          </button>
          <button class="np-ctrl" data-ctrl="loop" onclick="App.toggleLoop()" title="Repeat">
            <svg><use href="#ic-repeat"/></svg>
          </button>
        </div>

        <!-- Volume -->
        <div class="np-volume">
          <svg><use href="#ic-volume"/></svg>
          <input type="range" class="np-vol-slider vol-slider" min="0" max="100" value="75"
                 oninput="App.setVolume(this.value)">
        </div>
      </div>

      <!-- Right: Wait List -->
      <div class="wait-list-panel">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;gap:.75rem">
          <div class="wl-title" style="margin:0">Wait List</div>
          <button class="btn-confirm" style="font-size:.78rem;padding:.45rem .8rem;white-space:nowrap"
                  onclick="saveWaitingList()">Save as Playlist</button>
        </div>
        <div id="waitListEl">
          <div class="wl-empty">No songs queued. Play a song to start.</div>
        </div>
      </div>
    </div>

<%@ include file="includes/layout-bottom.jspf" %>

<script>
// Hook progress bar click for now playing page
function handleSeek(e, bar) {
  const rect = bar.getBoundingClientRect();
  const pct = ((e.clientX - rect.left) / rect.width) * 100;
  App.seekTo(Math.max(0, Math.min(100, pct)));
}

// Sync .p-play buttons on this page to match np-play
const origToggle = App.togglePlay.bind(App);

// Render wait list
window.renderWaitList = function(list) {
  const el = document.getElementById('waitListEl');
  if (!list || !list.length) {
    el.innerHTML = '<div class="wl-empty">No songs queued.</div>';
    return;
  }
  const cur = App.getState().currentTrack;
  el.innerHTML = list.map((t, i) => {
    const isCurrent = cur && cur.songId === t.songId;
    const cover = t.coverPath
      ? `<img src="${t.coverPath}" alt="">`
      : `<span>${t.emoji || '🎵'}</span>`;
    const trackJson = JSON.stringify(t).replace(/"/g, '&quot;');
    return `
      <div class="wl-track ${isCurrent ? 'current' : ''}"
           ondblclick="App.playTrack(${trackJson})">
        <span class="wl-num">${isCurrent ? '▶' : i + 1}</span>
        <div class="wl-thumb">${cover}</div>
        <div>
          <div class="wl-name">${t.title}</div>
          <div class="wl-artist">${t.artist}</div>
        </div>
        <span class="wl-dur">${t.durationStr || ''}</span>
      </div>`;
  }).join('');
};

// Load wait list on open
(async function() {
  const res = await App.API.get('/api/player/waitlist');
  if (res && res.waitList) renderWaitList(res.waitList);
})();

async function saveWaitingList() {
  const name = prompt('Enter playlist name to save this waiting list:');
  if (!name || !name.trim()) return;
  const id = await App.saveWaitingAsPlaylist(name.trim());
  if (id) {
    window.location = '<%= request.getContextPath() %>/playlist-detail.jsp?id=' + id;
  }
}

// Sync all np-ctrl active states
function syncNpControls() {
  const s = App.getState();
  document.querySelectorAll('[data-ctrl="shuffle"]').forEach(b => b.classList.toggle('active', s.isShuffle));
  document.querySelectorAll('[data-ctrl="loop"]').forEach(b => b.classList.toggle('active', !!s.isLoop));
  // Sync np-play button icon
  const np = document.querySelector('.np-play');
  if (np) {
    np.innerHTML = s.isPlaying
      ? `<svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" fill="currentColor" stroke-width="2"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>`
      : `<svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" fill="currentColor" stroke-width="2"><polygon points="5,3 19,12 5,21"/></svg>`;
  }
}
setInterval(syncNpControls, 300);
</script>
</body>
</html>
