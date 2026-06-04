<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
//  if (session.getAttribute("user") == null) {
//    response.sendRedirect(request.getContextPath() + "/login.jsp");
//    return;
//  }
  request.setAttribute("currentPage", "");
  String username = (String) session.getAttribute("username");
  if (username == null) username = "User";
  String initials = username.length() >= 2 ? username.substring(0,2).toUpperCase() : username.toUpperCase();
  String successMsg = (String) session.getAttribute("profileSuccess");
  String errorMsg   = (String) session.getAttribute("profileError");
  if (successMsg != null) session.removeAttribute("profileSuccess");
  if (errorMsg   != null) session.removeAttribute("profileError");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SoundWave — Profile</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@700;800;900&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
  <style>
    .profile-wrap {
      max-width: 560px; margin: 0 auto;
    }
    .avatar-card {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 2rem;
      text-align: center;
      margin-bottom: 1.25rem;
    }
    .avatar-circle {
      width: 90px; height: 90px; border-radius: 50%;
      background: var(--bg3);
      border: 2px solid var(--border2);
      display: flex; align-items: center; justify-content: center;
      margin: 0 auto 1rem;
      color: var(--text3);
    }
    .avatar-circle svg { width: 44px; height: 44px; stroke: currentColor; fill: none; stroke-width: 1.5; stroke-linecap: round; stroke-linejoin: round; }
    .avatar-name { color: var(--accent); font-size: 1rem; font-weight: 600; }

    .profile-form-card {
      background: var(--surface2);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 2rem;
    }
    .profile-form-title {
      font-family: var(--font-display);
      font-size: 1.1rem; font-weight: 800;
      color: var(--accent); letter-spacing: 0.5px;
      text-align: center; text-transform: uppercase;
      margin-bottom: 1.5rem;
    }
    .form-row { margin-bottom: 1rem; }
    .form-row label { display: block; font-size: 0.82rem; font-weight: 600; color: var(--text2); margin-bottom: 6px; }
    .form-input {
      width: 100%; padding: 0.7rem 1rem;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      color: var(--text); font-family: var(--font-body); font-size: 0.9rem;
      outline: none;
      transition: border-color var(--transition), box-shadow var(--transition);
    }
    .form-input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px var(--accent-soft); }
    .form-input::placeholder { color: var(--text3); }
  </style>
</head>
<body>

<%@ include file="includes/layout-top.jspf" %>

    <div class="page-content">
      <div class="profile-wrap">
        <!-- Avatar card -->
        <div class="avatar-card">
          <div class="avatar-circle">
            <svg><use href="#ic-user"/></svg>
          </div>
          <div class="avatar-name"><%= username %></div>
        </div>

        <!-- Change profile form -->
        <div class="profile-form-card">
          <div class="profile-form-title">Change Profile</div>

          <% if (successMsg != null) { %>
            <div class="success-msg" style="text-align:center; margin-bottom:0.75rem">✅ <%= successMsg %></div>
          <% } %>
          <% if (errorMsg != null) { %>
            <div class="error-msg show" style="text-align:center; margin-bottom:0.75rem"><%= errorMsg %></div>
          <% } %>

          <form method="post" action="<%= request.getContextPath() %>/ProfileServlet" id="profileForm">
            <input type="hidden" name="action" value="update">

            <div class="form-row">
              <label>Change Name</label>
              <input class="form-input" type="text" name="newUsername"
                     placeholder="New name" autocomplete="username">
            </div>

            <div class="form-row">
              <label>Change Password</label>
              <input class="form-input" type="password" id="newPass" name="newPassword"
                     placeholder="New password" autocomplete="new-password">
            </div>

            <div class="form-row">
              <label>Confirm Password</label>
              <input class="form-input" type="password" id="confirmPass" name="confirmPassword"
                     placeholder="Confirm password" autocomplete="new-password">
              <div class="error-msg" id="confirmErr">Passwords do not match.</div>
            </div>

            <button type="submit" class="btn-primary" style="margin-top:0.5rem">Save</button>
          </form>
        </div>
      </div>
    </div>

<%@ include file="includes/layout-bottom.jspf" %>

<script>
document.getElementById('profileForm').addEventListener('submit', function(e) {
  const np = document.getElementById('newPass').value;
  const cp = document.getElementById('confirmPass').value;
  const err = document.getElementById('confirmErr');
  err.classList.remove('show');
  if (np && np !== cp) {
    err.classList.add('show');
    e.preventDefault();
  }
});
</script>
</body>
</html>
