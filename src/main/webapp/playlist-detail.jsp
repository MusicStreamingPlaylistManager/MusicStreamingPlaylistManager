<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
  if (session.getAttribute("user") == null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
  }
  request.setAttribute("currentPage", "playlist");
  // ?id=123 for custom playlist, ?type=favourite for favourite list
  String playlistId = request.getParameter("id");
  String type       = request.getParameter("type");
  boolean isFavourite = "favourite".equals(type);
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SoundWave — Playlist</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@700;800;900&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
  <style>
    /* Favourite header banner */
    .pl-detail-banner {
      background: linear-gradient(135deg, #1a0533 0%, #2e1a6e 60%, #1e1256 100%);
      border-radius: var(--radius);
      padding: 1.75rem 2rem;
      display: flex; align-items: center; gap: 1.5rem;
      margin-bottom: 1.5rem;
    }
    .pl-detail-icon { font-size: 3rem; flex-shrink: 0; }
    .pl-detail-title {
      font-family: var(--font-display);
      font-size: 1.6rem; font-weight: 900; color: #fff; margin-bottom: 4px;
    }
    .pl-detail-count { color: rgba(255,255,255,0.55); font-size: 0.85rem; }

    .pl-actions {
      display: flex; align-items: center; gap: 10px; margin-bottom: 1.25rem;
    }
    .empty-msg {
      color: var(--text3); font-size: 0.9rem; padding: 2rem 0.5rem;
      display: flex; align-items: center; gap: 8px;
    }
    .empty-msg a { color: var(--accent); }

    /* Drag-drop reorder hint */
    .track-item[draggable="true"] { cursor: grab; }
    .track-item.drag-over { background: var(--accent-soft); border-radius: var(--radius-sm); }
  </style>
</head>
<body>

<%@ include file="includes/layout-top.jspf" %>

      <!-- Header -->
      <div class="pl-detail-banner" id="plBanner">
        <div class="pl-detail-icon" id="plIcon">
          <%= isFavourite ? "❤️" : "🎵" %>
        </div>
        <div>
          <div class="pl-detail-title" id="plTitle">
            <%= isFavourite ? "Favourite List" : "Loading..." %>
          </div>
          <div class="pl-detail-count" id="plCount">0 song</div>
        </div>
      </div>

      <!-- Action buttons --> 
      <div class="pl-actions">
        <button class="btn-icon-lg" onclick="playAll()" title="Play all">
          <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" fill="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="5,3 19,12 5,21"/>
          </svg>
        </button>
        <% if (!isFavourite) { %>
        <button class="btn-icon" onclick="deleteCurrent()" title="Delete playlist">
          <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <use href="#ic-trash"/>
          </svg>
        </button>
        <% } %>
      </div>

      <!-- Track list -->
      <div class="track-list" id="trackList">
        <div style="color:var(--text3); font-size:.85rem; padding:.5rem">Loading...</div>
      </div>

<%@ include file="includes/layout-bottom.jspf" %>

<script>
// Dùng var (không phải const) để script chạy lại được khi điều hướng SPA nhiều lần.
var IS_FAVOURITE = <%= isFavourite %>;
var PLAYLIST_ID  = '<%= playlistId != null ? playlistId : "" %>';
var FAV_PLAYLIST_ID = 0; // id của playlist Favourite (lấy từ /api/favorites) để phát đúng hàng chờ
var tracks = [];

async function loadDetail() {
  // Hàm này là global xuyên SPA; bỏ qua nếu không còn ở trang playlist-detail.
  if (!document.getElementById('trackList')) return;
  let res;
  if (IS_FAVOURITE) {
    res = await App.API.get('/api/favorites');
    if (res && res.playlistId) FAV_PLAYLIST_ID = res.playlistId;
  } else {
    res = await App.API.get('/api/playlists/' + PLAYLIST_ID);
    if (res && res.playlist) {
      document.getElementById('plTitle').textContent = res.playlist.name;
      document.getElementById('plIcon').textContent = res.playlist.emoji || '🎵';
    }
  }

  if (!res) { document.getElementById('trackList').innerHTML = '<p style="color:var(--text3)">Failed to load.</p>'; return; }

  tracks = (IS_FAVOURITE ? res.songs : res.songs) || [];
  const count = tracks.length;
  document.getElementById('plCount').textContent = count + ' song' + (count !== 1 ? 's' : '');
  renderTracks();
}

function renderTracks() {
  const list = document.getElementById('trackList');
  if (!tracks.length) {
    const msg = IS_FAVOURITE
      ? 'No favorite songs yet. Tap ❤️ on a song to add it here.'
      : 'No songs yet. Search for songs and add them here.';
    list.innerHTML = `<div class="empty-msg">
      <span>🎵</span>
      <span>${msg}</span>
    </div>`;
    return;
  }

  list.innerHTML = tracks.map((t, i) => {
    const isFav = App.getState().favorites.has(t.songId);
    const cover = t.coverPath
      ? `<img src="${t.coverPath}" alt="">`
      : `<span style="font-size:1.3rem">${t.emoji || '🎵'}</span>`;
    return `
      <div class="track-item" data-id="${t.songId}"
           draggable="${!IS_FAVOURITE}"
           ondragstart="dragStart(event, ${i})"
           ondragover="dragOver(event)"
           ondrop="drop(event, ${i})"
           ondblclick="playSingle(${t.songId})">
        <span class="track-num">${i + 1}</span>
        <span class="track-play-icon">
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="5,3 19,12 5,21"/>
          </svg>
        </span>
        <div class="track-thumb">${cover}</div>
        <div class="track-info">
          <div class="t-name">${t.title}</div>
          <div class="t-artist">${t.artist}</div>
        </div>
        <div class="track-actions">
          ${IS_FAVOURITE
            ? `<button class="heart-btn liked"
                      onclick="event.stopPropagation(); removeFav(${t.songId})"
                      title="Remove from favourites">
                 <svg viewBox="0 0 24 24" width="18" height="18" stroke="#ef4444" fill="#ef4444" stroke-width="2">
                   <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
                 </svg>
               </button>`
            : `<button class="heart-btn" style="color:var(--red)"
                      onclick="event.stopPropagation(); removeFromPlaylist(${t.songId})"
                      title="Remove from playlist">
                 <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                   <use href="#ic-trash"/>
                 </svg>
               </button>`}
          <span class="track-dur">${t.durationStr || ''}</span>
        </div>
      </div>`;
  }).join('');
  App.fillMissingCovers();
}

// id playlist hiện tại (favourite hoặc custom).
function currentPlaylistId() {
  return IS_FAVOURITE ? FAV_PLAYLIST_ID : PLAYLIST_ID;
}

// Nút "Play list": nạp TOÀN BỘ bài trong list vào hàng chờ; nghe hết bài cuối mới nạp thêm.
function playAll() {
  if (!tracks.length) { App.showToast('No songs to play'); return; }
  const pid = currentPlaylistId();
  if (!pid) { App.playTrack(tracks[0]); App.showToast('▶ Playing playlist'); return; }
  App.API.postForm('/api/player/play', { songId: tracks[0].songId, playlistId: pid }).then(res => {
    if (res && res.track) {
      App.applyTrack(res.track, res.waitList);
      App.showToast('▶ Playing playlist');
    }
  });
}

// Bấm một bài lẻ trong list: hàng chờ CHỈ gồm bài đó (giống trang chủ/search),
// phát hết mới tự nạp thêm.
function playSingle(songId) {
  const t = tracks.find(x => x.songId === songId);
  if (t) App.playTrack(t);
}

async function removeFav(songId) {
  await App.toggleFavorite(songId);
}

async function removeFromPlaylist(songId) {
  const res = await App.API.postForm('/api/playlists/removeSong', { playlistId: PLAYLIST_ID, songId });
  if (res && res.success) { App.showToast('Removed'); loadDetail(); }
}

async function deleteCurrent() {
  const ok = await App.confirm({
    title: 'Delete playlist',
    message: 'Are you sure you want to delete this playlist? This cannot be undone.',
    okText: 'Delete',
    danger: true
  });
  if (!ok) return;
  const res = await App.API.postForm('/api/playlists/delete', { playlistId: PLAYLIST_ID });
  if (res && res.success) {
    App.showToast('🗑 Playlist deleted');
    // Điều hướng SPA về trang playlist (giữ nhạc đang phát).
    spaNavigate('<%= request.getContextPath() %>/playlist.jsp');
  }
}

// --- Drag & drop reorder ---
var dragIdx = null;
function dragStart(e, i) { dragIdx = i; }
function dragOver(e) { e.preventDefault(); e.currentTarget.classList.add('drag-over'); }
function drop(e, targetIdx) {
  e.currentTarget.classList.remove('drag-over');
  if (dragIdx === null || dragIdx === targetIdx) return;
  const moved = tracks.splice(dragIdx, 1)[0];
  tracks.splice(targetIdx, 0, moved);
  dragIdx = null;
  renderTracks();
  const order = tracks.map(t => t.songId);
  App.API.postForm('/api/playlists/reorder', { playlistId: PLAYLIST_ID, order: order.join(',') });
}
function onDocDragEnd() {
  document.querySelectorAll('.drag-over').forEach(el => el.classList.remove('drag-over'));
}

function initPlaylistDetail() {
  document.addEventListener('dragend', onDocDragEnd);
  loadDetail();
}

// Phase 3: gỡ listener dragend toàn cục khi rời trang để tránh tích tụ.
function cleanupPlaylistDetail() {
  document.removeEventListener('dragend', onDocDragEnd);
}

App.Router.register('playlist-detail', { init: initPlaylistDetail, cleanup: cleanupPlaylistDetail });
</script>
</body>
</html>
