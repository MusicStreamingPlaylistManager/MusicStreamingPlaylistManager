<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
  if (session.getAttribute("user") == null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
  }
  if (!"Admin".equals(session.getAttribute("role"))) {
    response.sendRedirect(request.getContextPath() + "/home.jsp");
    return;
  }
  request.setAttribute("currentPage", "admin");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SoundWave — Admin Songs</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@700;800;900&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
  <style>
    .admin-grid { display:grid; grid-template-columns: 1fr 340px; gap:1.5rem; }
    @media (max-width: 960px) { .admin-grid { grid-template-columns: 1fr; } }
    .admin-form, .admin-list { background:var(--surface); border:1px solid var(--border); border-radius:var(--radius); padding:1.25rem; }
    .admin-form label { display:block; font-size:.8rem; color:var(--text2); margin:.75rem 0 .35rem; }
    .admin-form input { width:100%; padding:.55rem .75rem; border:1px solid var(--border); border-radius:8px; background:var(--bg); color:var(--text); }
    .admin-table { width:100%; border-collapse:collapse; font-size:.85rem; }
    .admin-table th, .admin-table td { text-align:left; padding:.55rem .4rem; border-bottom:1px solid var(--border); vertical-align:top; }
    .admin-actions { display:flex; gap:.35rem; flex-wrap:wrap; }
    .btn-sm { font-size:.75rem; padding:.35rem .55rem; border-radius:6px; border:1px solid var(--border); background:var(--surface2); cursor:pointer; }
    .btn-sm.danger { color:#ef4444; border-color:#fecaca; }
  </style>
</head>
<body>

<%@ include file="includes/layout-top.jspf" %>

      <h1 style="font-family:var(--font-display);font-size:1.5rem;font-weight:800;margin-bottom:1rem">Manage Songs</h1>
      <div class="admin-grid">
        <div class="admin-list">
          <div class="section-title" style="margin-bottom:.75rem">Song Library</div>
          <div id="adminSongList">Loading...</div>
        </div>
        <div class="admin-form">
          <div class="section-title" id="formTitle" style="margin-bottom:.75rem">Add Song</div>
          <input type="hidden" id="songId" value="0">
          <label>Title</label>
          <input id="title" type="text" placeholder="Song title">
          <label>Artist</label>
          <input id="artist" type="text" placeholder="Artist name">
          <label>Genre</label>
          <input id="genre" type="text" placeholder="ballad / barbie playlist / piano / pop">
          <label>Duration (seconds)</label>
          <input id="duration" type="number" value="200">
          <label>File Path</label>
          <input id="filePath" type="text" placeholder="/assets/Songs/pop/example.mp3">
          <label>Cover Path</label>
          <input id="coverPath" type="text" placeholder="">
          <div style="display:flex;gap:.5rem;margin-top:1rem">
            <button class="btn-confirm" onclick="saveSong()">Save</button>
            <button class="btn-cancel" onclick="resetForm()">Reset</button>
          </div>
        </div>
      </div>

<%@ include file="includes/layout-bottom.jspf" %>

<script>
var songs = [];

async function loadSongs() {
  const res = await App.API.get('/api/admin/songs');
  const el = document.getElementById('adminSongList');
  if (!res || !res.songs) {
    el.textContent = 'Failed to load songs.';
    return;
  }
  songs = res.songs;
  if (!songs.length) {
    el.textContent = 'No songs in library.';
    return;
  }
  el.innerHTML = `
    <table class="admin-table">
      <thead><tr><th>ID</th><th>Title</th><th>Genre</th><th></th></tr></thead>
      <tbody>
        ${songs.map(s => `
          <tr>
            <td>${s.songId}</td>
            <td>${s.title}<br><span style="color:var(--text3);font-size:.75rem">${s.artist}</span></td>
            <td>${s.genre}</td>
            <td class="admin-actions">
              <button class="btn-sm" onclick="editSong(${s.songId})">Edit</button>
              <button class="btn-sm danger" onclick="deleteSong(${s.songId})">Delete</button>
            </td>
          </tr>`).join('')}
      </tbody>
    </table>`;
}

function editSong(id) {
  const s = songs.find(x => x.songId === id);
  if (!s) return;
  document.getElementById('formTitle').textContent = 'Edit Song #' + id;
  document.getElementById('songId').value = s.songId;
  document.getElementById('title').value = s.title;
  document.getElementById('artist').value = s.artist;
  document.getElementById('genre').value = s.genre;
  document.getElementById('duration').value = s.duration;
  document.getElementById('filePath').value = s.filePath || '';
  document.getElementById('coverPath').value = s.coverPath || '';
}

function resetForm() {
  document.getElementById('formTitle').textContent = 'Add Song';
  document.getElementById('songId').value = '0';
  ['title','artist','genre','filePath','coverPath'].forEach(id => document.getElementById(id).value = '');
  document.getElementById('duration').value = '200';
}

async function saveSong() {
  const songId = parseInt(document.getElementById('songId').value, 10);
  const params = {
    title: document.getElementById('title').value.trim(),
    artist: document.getElementById('artist').value.trim(),
    genre: document.getElementById('genre').value.trim(),
    duration: document.getElementById('duration').value,
    filePath: document.getElementById('filePath').value.trim(),
    coverPath: document.getElementById('coverPath').value.trim()
  };
  if (!params.title || !params.artist || !params.genre || !params.filePath) {
    App.showToast('Please fill required fields');
    return;
  }
  params.action = songId > 0 ? 'update' : 'create';
  if (songId > 0) params.songId = songId;
  const res = await App.API.postForm('/api/admin/songs', params);
  if (res && res.success) {
    App.showToast(songId > 0 ? 'Updated' : 'Created');
    resetForm();
    loadSongs();
  } else {
    App.showToast('Save failed');
  }
}

async function deleteSong(id) {
  if (!confirm('Delete song #' + id + '?')) return;
  const res = await App.API.postForm('/api/admin/songs', { action: 'delete', songId: id });
  if (res && res.success) {
    App.showToast('Deleted');
    loadSongs();
  }
}

function initAdmin() {
  // Trang chỉ admin (server đã chặn redirect non-admin; sidebar cũng chỉ hiện link cho admin).
  loadSongs();
}

App.Router.register('admin', { init: initAdmin });
</script>
</body>
</html>
