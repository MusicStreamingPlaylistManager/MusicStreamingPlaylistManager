<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
//  if (session.getAttribute("user") == null) {
//    response.sendRedirect(request.getContextPath() + "/login.jsp");
//    return;
//  }
  request.setAttribute("currentPage", "home");
  String username = (String) session.getAttribute("username");
  if (username == null) username = "Listener";

  // Greeting based on hour
  java.util.Calendar cal = java.util.Calendar.getInstance();
  int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
  String greeting = hour < 12 ? "Good Morning" : hour < 18 ? "Good Afternoon" : "Good Evening";
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SoundWave — Home</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@700;800;900&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
  <style>
    /* ---- HOME-SPECIFIC ---- */
    .banner {
      background: linear-gradient(135deg, #1a0533 0%, #2e1a6e 60%, #1e1256 100%);
      border-radius: var(--radius);
      padding: 1.5rem 2rem;
      display: flex; align-items: center; gap: 1.25rem;
      margin-bottom: 2rem;
      position: relative; overflow: hidden;
    }
    .banner::before {
      content: ''; position: absolute; right: -30px; top: -30px;
      width: 160px; height: 160px; border-radius: 50%;
      background: rgba(167,139,250,0.12);
    }
    .banner-icon { font-size: 2.5rem; flex-shrink: 0; }
    .banner-title {
      font-family: var(--font-display);
      font-size: 1.6rem; font-weight: 900; color: #fff; margin-bottom: 0.2rem;
    }
    .banner-sub { color: rgba(255,255,255,0.55); font-size: 0.85rem; }

    /* Picks cards */
    .picks-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 0.875rem;
      margin-bottom: 2rem;
    }
    @media (max-width: 1100px) { .picks-grid { grid-template-columns: repeat(3, 1fr); } }
    @media (max-width: 760px)  { .picks-grid { grid-template-columns: repeat(2, 1fr); } }

    .pick-card {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.875rem;
      cursor: pointer;
      transition: all var(--transition);
      position: relative; overflow: hidden;
    }
    .pick-card:hover { border-color: var(--border2); transform: translateY(-2px); background: var(--surface2); }
    .pick-card:hover .pick-play { opacity: 1; transform: translateY(0); }

    .pick-cover {
      width: 100%; aspect-ratio: 1;
      border-radius: 8px;
      background: var(--bg3);
      margin-bottom: 0.75rem;
      display: flex; align-items: center; justify-content: center;
      font-size: 2.8rem;
      position: relative; overflow: hidden;
    }
    .pick-cover img { width: 100%; height: 100%; object-fit: cover; }
    .pick-play {
      position: absolute; bottom: 6px; right: 6px;
      width: 34px; height: 34px; border-radius: 50%;
      background: var(--accent); color: #fff;
      display: flex; align-items: center; justify-content: center;
      opacity: 0; transform: translateY(6px);
      transition: all 0.2s;
      box-shadow: 0 4px 12px rgba(108,43,217,0.45);
    }
    .pick-play svg { width: 14px; height: 14px; fill: currentColor; stroke: currentColor; stroke-width: 1; }
    .pick-title { font-size: 0.85rem; font-weight: 600; color: var(--text); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .pick-artist { font-size: 0.75rem; color: var(--text3); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

    /* Recently Played */
    .track-item-home {
      grid-template-columns: 28px 44px 1fr auto;
    }
  </style>
</head>
<body>

<%-- Include top layout (sidebar + topbar opening) --%>
<%@ include file="includes/layout-top.jspf" %>

    <div class="page-content">
      <!-- Banner -->
      <div class="banner">
        <div class="banner-icon">🎵</div>
        <div>
          <div class="banner-title"><%= greeting %></div>
          <div class="banner-sub">Ready to start your rhythm today?</div>
        </div>
      </div>

      <!-- Personalized Picks -->
      <div class="section-title">
        Personalized Picks
        <span class="section-more" onclick="window.location='<%= request.getContextPath() %>/search.jsp'">All</span>
      </div>
      <div class="picks-grid" id="picksGrid">
        <div style="color:var(--text3); font-size:.85rem; padding:.5rem">Loading...</div>
      </div>

      <!-- Recently Played -->
      <div class="section-title">
        Recently Played
        <span class="section-more" onclick="window.location='<%= request.getContextPath() %>/nowplaying.jsp'">All</span>
      </div>
      <div class="track-list" id="recentList">
        <div style="color:var(--text3); font-size:.85rem; padding:.5rem">Loading...</div>
      </div>
    </div>

<%-- Include bottom layout (player bar + scripts closing) --%>
<%@ include file="includes/layout-bottom.jspf" %>

<script>
(async function initHome() {
  // Load picks
  const songs = await App.API.get('/api/songs?limit=5');
  const picksGrid = document.getElementById('picksGrid');
  if (songs && songs.length) {
    picksGrid.innerHTML = songs.map(t => `
      <div class="pick-card" onclick="App.playTrack(\${JSON.stringify(t).replace(/\"/g,'&quot;')})">
        <div class="pick-cover">
          \${t.coverPath ? `<img src="\${t.coverPath}" alt="">` : `<span>\${t.emoji || '🎵'}</span>`} 
          <div class="pick-play">
            <svg viewBox="0 0 24 24"><polygon points="5,3 19,12 5,21"/></svg>
          </div>
        </div>
        <div class="pick-title">\${t.title}</div>
        <div class="pick-artist">\${t.artist}</div>
      </div>`).join('');
  } else {
    picksGrid.innerHTML = '<p style="color:var(--text3);font-size:.85rem">No songs found.</p>';
  }

  // Load recently played
  const history = await App.API.get('/api/player/history');
  const recentList = document.getElementById('recentList');
  if (history && history.songs && history.songs.length) {
    recentList.innerHTML = history.songs.map((t, i) => renderTrackItem(t, i + 1)).join('');
  } else {
    // Fallback
    const all = await App.API.get('/api/songs?limit=6');
    if (all && all.length) {
      recentList.innerHTML = all.map((t, i) => renderTrackItem(t, i + 1)).join('');
    } else {
      recentList.innerHTML = '<p style="color:var(--text3);font-size:.85rem;padding:.5rem">Play a song to see your history here.</p>';
    }
  }
})();
</script>

</body>
</html>
