<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
//  if (session.getAttribute("user") == null) {
//    response.sendRedirect(request.getContextPath() + "/login.jsp");
//    return;
//  }
  request.setAttribute("currentPage", "search");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SoundWave — Search</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@700;800;900&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
  <style>
    .genre-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 0.875rem;
      margin-bottom: 2rem;
    }
    .genre-card {
      border-radius: var(--radius);
      padding: 1rem 1.25rem;
      cursor: pointer;
      font-family: var(--font-display);
      font-weight: 700;
      font-size: 1rem;
      height: 90px;
      display: flex; align-items: flex-end;
      position: relative; overflow: hidden;
      transition: transform var(--transition), box-shadow var(--transition);
    }
    .genre-card:hover { transform: scale(1.04); box-shadow: var(--shadow-lg); }
    .genre-card .g-icon {
      position: absolute; right: 12px; top: 12px;
      font-size: 2rem; opacity: 0.85;
    }
    .genre-card .g-name { position: relative; z-index: 1; }

    .search-results-section { display: none; margin-bottom: 2rem; }
    .search-results-section.show { display: block; }
    .no-results { color: var(--text3); font-size: 0.875rem; padding: 1rem 0.5rem; }
  </style>
</head>
<body>

<%@ include file="includes/layout-top.jspf" %>

    <div class="page-content">
      <h1 style="font-family:var(--font-display); font-size:1.5rem; font-weight:800; margin-bottom:1.5rem">Search</h1>

      <!-- Genre browse -->
      <div id="browseSection">
        <p style="font-size:1rem; font-weight:600; margin-bottom:1rem; color:var(--text2)">Browse all</p>
        <div class="genre-grid">
          <div class="genre-card" style="background:#2d1b69;color:#c4b5fd" onclick="filterGenre('V-Pop')">
            <span class="g-icon">🎤</span><span class="g-name">V-Pop</span>
          </div>
          <div class="genre-card" style="background:#1a3a1a;color:#86efac" onclick="filterGenre('Hiphop')">
            <span class="g-icon">✨</span><span class="g-name">Hiphop</span>
          </div>
          <div class="genre-card" style="background:#3a1a00;color:#fcd34d" onclick="filterGenre('Ballad')">
            <span class="g-icon">🎵</span><span class="g-name">Ballad</span>
          </div>
          <div class="genre-card" style="background:#001a1a;color:#67e8f9" onclick="filterGenre('Lo-fi')">
            <span class="g-icon">🎹</span><span class="g-name">Lo-fi</span>
          </div>
          <div class="genre-card" style="background:#2a002a;color:#f0abfc" onclick="filterGenre('R&B')">
            <span class="g-icon">🎷</span><span class="g-name">R&amp;B</span>
          </div>
          <div class="genre-card" style="background:#1a001a;color:#fca5a5" onclick="filterGenre('Pop')">
            <span class="g-icon">🎶</span><span class="g-name">Pop</span>
          </div>
          <div class="genre-card" style="background:#0a1a00;color:#bef264" onclick="filterGenre('Indie')">
            <span class="g-icon">🌱</span><span class="g-name">Indie</span>
          </div>
          <div class="genre-card" style="background:#00001a;color:#93c5fd" onclick="filterGenre('EDM')">
            <span class="g-icon">🔊</span><span class="g-name">EDM</span>
          </div>
        </div>
      </div>

      <!-- Search results (shown when query entered) -->
      <div class="search-results-section" id="searchResultsSection">
        <div class="section-title">
          <span id="resultsTitle">Results</span>
          <span class="section-more" onclick="clearSearch()">← Back</span>
        </div>
        <div class="track-list" id="searchResultsList">
          <div class="no-results">No results found.</div>
        </div>
      </div>
    </div>

<%@ include file="includes/layout-bottom.jspf" %>

<script>
// Override global search to show inline results on this page
const _origSearch = window.handleGlobalSearch;
window.handleGlobalSearch = async function(val) {
  if (!val.trim()) { clearSearch(); return; }
  // Hide global overlay
  document.getElementById('search-overlay').style.display = 'none';

  const res = await App.API.get('/api/songs/search?q=' + encodeURIComponent(val));
  const section = document.getElementById('searchResultsSection');
  const list = document.getElementById('searchResultsList');
  const title = document.getElementById('resultsTitle');
  document.getElementById('browseSection').style.display = 'none';
  section.classList.add('show');

  if (!res || !res.songs || !res.songs.length) {
    title.textContent = `No results for "${val}"`;
    list.innerHTML = '<div class="no-results">No songs found. Try another keyword.</div>';
    return;
  }
  title.textContent = `Results for "${val}" (${res.songs.length})`;
  list.innerHTML = res.songs.map((t, i) => renderTrackItem(t, i + 1)).join('');
};

async function filterGenre(genre) {
  document.getElementById('browseSection').style.display = 'none';
  const section = document.getElementById('searchResultsSection');
  const list = document.getElementById('searchResultsList');
  const title = document.getElementById('resultsTitle');
  section.classList.add('show');
  title.textContent = `Genre: ${genre}`;
  list.innerHTML = '<div style="color:var(--text3);font-size:.85rem;padding:.5rem">Loading...</div>';

  const res = await App.API.get('/api/songs/search?genre=' + encodeURIComponent(genre));
  if (!res || !res.songs || !res.songs.length) {
    list.innerHTML = '<div class="no-results">No songs in this genre yet.</div>';
    return;
  }
  title.textContent = `Genre: ${genre} (${res.songs.length})`;
  list.innerHTML = res.songs.map((t, i) => renderTrackItem(t, i + 1)).join('');
}

function clearSearch() {
  document.getElementById('browseSection').style.display = 'block';
  document.getElementById('searchResultsSection').classList.remove('show');
  document.getElementById('globalSearch').value = '';
}
</script>

</body>
</html>
