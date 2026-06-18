<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  if (session.getAttribute("user") == null) {
          response.sendRedirect(request.getContextPath() + "/login.jsp");
          return;
      }
  request.setAttribute("currentPage", "playlist");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SoundWave — My Playlist</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@700;800;900&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
  <style>
    /* Favourite banner row */
    .fav-banner-row {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 1rem 1.5rem;
      display: flex; align-items: center; gap: 1rem;
      cursor: pointer;
      margin-bottom: 1.5rem;
      transition: all var(--transition);
    }
    .fav-banner-row:hover { border-color: var(--border2); background: var(--surface2); }
    .fav-banner-thumb {
      width: 56px; height: 56px; border-radius: 10px;
      background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
      display: flex; align-items: center; justify-content: center;
      font-size: 1.6rem; flex-shrink: 0;
    }
    .fav-banner-title { font-family: var(--font-display); font-size: 1rem; font-weight: 700; }
    .fav-banner-count { font-size: 0.8rem; color: var(--text3); margin-top: 2px; }

    /* Custom playlists grid */
    .custom-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 1rem;
    }

    .pl-card {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 1rem;
      cursor: pointer;
      display: flex; align-items: center; gap: 12px;
      transition: all var(--transition);
      position: relative;
    }
    .pl-card:hover { border-color: var(--border2); background: var(--surface2); }
    .pl-thumb {
      width: 52px; height: 52px; border-radius: 10px;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.6rem; flex-shrink: 0; overflow: hidden;
    }
    .pl-thumb img { width: 100%; height: 100%; object-fit: cover; }
    .pl-name { font-size: 0.9rem; font-weight: 600; }
    .pl-count { font-size: 0.75rem; color: var(--text3); margin-top: 2px; }
    .pl-delete {
      position: absolute; top: 8px; right: 8px;
      background: none; border: none; color: var(--text3);
      cursor: pointer; padding: 4px; border-radius: 4px;
      display: none;
      transition: color var(--transition);
    }
    .pl-delete svg { width: 14px; height: 14px; stroke: currentColor; fill: none; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round; }
    .pl-card:hover .pl-delete { display: flex; }
    .pl-delete:hover { color: var(--red); }

    /* Create card */
    .create-card {
      background: transparent;
      border: 2px dashed var(--border2);
      border-radius: var(--radius);
      padding: 1rem;
      cursor: pointer;
      display: flex; align-items: center; gap: 12px;
      transition: all var(--transition);
      color: var(--text2);
    }
    .create-card:hover { border-color: var(--accent); color: var(--accent); background: var(--accent-soft); }
    .create-icon {
      width: 52px; height: 52px; border-radius: 10px;
      background: var(--accent-soft); color: var(--accent);
      display: flex; align-items: center; justify-content: center;
      font-size: 1.5rem; flex-shrink: 0;
    }
    .create-icon svg { width: 24px; height: 24px; stroke: currentColor; fill: none; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round; }
    .create-label { font-size: 0.9rem; font-weight: 600; }
    .create-sub { font-size: 0.75rem; color: var(--text3); margin-top: 2px; }
  </style>
</head>
<body>

<%@ include file="includes/layout-top.jspf" %>

      <h1 style="font-family:var(--font-display); font-size:1.5rem; font-weight:800; margin-bottom:1.5rem">My Playlist</h1>

      <!-- Favourite List (always shown, 1 per user) -->
      <div class="fav-banner-row"
           onclick="spaNavigate('<%= request.getContextPath() %>/playlist-detail.jsp?type=favourite')">
        <div class="fav-banner-thumb">❤️</div>
        <div>
          <div class="fav-banner-title">Favourite List</div>
          <div class="fav-banner-count" id="favCount">0 song</div>
        </div>
      </div>

      <!-- Custom Playlists -->
      <div class="custom-grid" id="playlistGrid">
        <!-- Create card (always first) -->
        <div class="create-card" onclick="showCreateModal()">
          <div class="create-icon"><svg><use href="#ic-plus"/></svg></div>
          <div>
            <div class="create-label" style="color:var(--accent)">Create your playlist</div>
            <div class="create-sub">Add your favourite music</div>
          </div>
        </div>
        <!-- Playlist cards injected by JS -->
      </div>

<!-- Create Playlist Modal -->
<div class="modal-bg" id="createModal">
  <div class="modal">
    <div class="modal-title">Create New Playlist</div>
    <div class="modal-sub">Enter your playlist name</div>
    <input class="modal-input" type="text" id="plNameInput"
           placeholder="Eg: Chill Vibes, Workout Mix..." maxlength="60">
    <div class="modal-actions">
      <button class="btn-cancel" onclick="closeCreateModal()">Cancel</button>
      <button class="btn-confirm" onclick="confirmCreate()">Create</button>
    </div>
  </div>
</div>

<%@ include file="includes/layout-bottom.jspf" %>

<script>
var playlists = [];

async function loadPlaylists() {
  const res = await App.API.get('/api/playlists');
  if (!res) return;

  // Update fav count
  if (res.favouriteCount !== undefined) {
    document.getElementById('favCount').textContent = res.favouriteCount + ' song' + (res.favouriteCount !== 1 ? 's' : '');
  }

  playlists = res.playlists || [];
  renderPlaylists();
}

function renderPlaylists() {
  const grid = document.getElementById('playlistGrid');
  // Keep create card + re-add playlist cards
  const createCard = grid.querySelector('.create-card');
  grid.innerHTML = '';
  grid.appendChild(createCard);

  playlists.forEach(pl => {
    const card = document.createElement('div');
    card.className = 'pl-card';
    card.innerHTML = `
      <div class="pl-thumb" style="background:${pl.color || '#1a0533'}">
        ${pl.coverPath ? `<img src="${pl.coverPath}" alt="">` : (pl.emoji || '🎵')}
      </div>
      <div>
        <div class="pl-name">${pl.name}</div>
        <div class="pl-count">${pl.songCount} song${pl.songCount !== 1 ? 's' : ''}</div>
      </div>
      <button class="pl-delete" onclick="deletePlaylist(event, ${pl.playlistId})" title="Delete">
        <svg viewBox="0 0 24 24" style="width: 16px; height: 16px; stroke: currentColor; fill: none; stroke-width: 2.5; stroke-linecap: round; stroke-linejoin: round;">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>`;
    
    // Sửa lỗi đường dẫn: Dùng request.getContextPath() từ JSP
    card.addEventListener('click', () => {
      spaNavigate('<%= request.getContextPath() %>/playlist-detail.jsp?id=' + pl.playlistId);
    });
    grid.appendChild(card);
  });
}

async function deletePlaylist(e, id) {
  e.stopPropagation();
  if (!confirm('Delete this playlist?')) return;
  const res = await App.API.postForm('/api/playlists/delete', { playlistId: id });
  if (res && res.success) {
    App.showToast('🗑 Playlist deleted');
    loadPlaylists();
  }
}

// Modal
function showCreateModal() {
  document.getElementById('createModal').classList.add('show');
  setTimeout(() => document.getElementById('plNameInput').focus(), 100);
}
function closeCreateModal() {
  document.getElementById('createModal').classList.remove('show');
  document.getElementById('plNameInput').value = '';
}
async function confirmCreate() {
  const name = document.getElementById('plNameInput').value.trim();
  if (!name) { App.showToast('⚠ Please enter a name'); return; }
  const res = await App.API.postForm('/api/playlists/create', { name });
  if (res && res.success) {
    closeCreateModal();
    App.showToast('✅ Playlist "' + name + '" created');
    loadPlaylists();
  } else {
    App.showToast('❌ Failed to create playlist');
  }
}

// Enter key in modal
document.getElementById('plNameInput').addEventListener('keydown', e => {
  if (e.key === 'Enter') confirmCreate();
  if (e.key === 'Escape') closeCreateModal();
});

loadPlaylists();
</script>
</body>
</html>
