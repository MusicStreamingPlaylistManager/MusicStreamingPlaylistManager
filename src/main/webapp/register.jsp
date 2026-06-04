<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  if (session.getAttribute("user") != null) {
    response.sendRedirect(request.getContextPath() + "/home.jsp");
    return;
  }
  String errorMsg = (String) session.getAttribute("registerError");
  if (errorMsg != null) session.removeAttribute("registerError");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SoundWave — Register</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@700;800;900&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body>

<div class="auth-page">
  <div class="auth-card">
    <div class="auth-logo">SoundWave</div>
    <p class="auth-sub">Listen to unlimited music, anytime, anywhere</p>

    <% if (errorMsg != null) { %>
      <div class="error-msg show" style="text-align:center; margin-bottom:0.75rem"><%= errorMsg %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/AuthServlet" id="regForm">
      <input type="hidden" name="action" value="register">

      <div class="field-group">
        <label class="field-label" for="username">Username</label>
        <input class="auth-input" type="text" id="username" name="username"
               placeholder="name" required minlength="3" autocomplete="username">
        <div class="error-msg" id="uErr">Username must be at least 3 characters.</div>
      </div>

      <div class="field-group">
        <label class="field-label" for="email">Email</label>
        <input class="auth-input" type="email" id="email" name="email"
               placeholder="name@email.com" required autocomplete="email">
        <div class="error-msg" id="eErr">Please enter a valid email.</div>
      </div>

      <div class="field-group">
        <label class="field-label" for="password">Password</label>
        <input class="auth-input" type="password" id="password" name="password"
               placeholder="At least 8 characters" required minlength="8" autocomplete="new-password">
        <div class="error-msg" id="pErr">Password must be at least 8 characters.</div>
      </div>

      <button type="submit" class="btn-primary" style="margin-top:0.5rem">Register</button>
    </form>

    <p class="auth-link" style="margin-top:1rem">Already have an account? <a href="<%= request.getContextPath() %>/login.jsp">Login</a></p>
  </div>
</div>

<script>
document.getElementById('regForm').addEventListener('submit', function(e) {
  let ok = true;
  const u = document.getElementById('username').value.trim();
  const em = document.getElementById('email').value.trim();
  const p = document.getElementById('password').value;
  document.querySelectorAll('.error-msg').forEach(el => el.classList.remove('show'));

  if (u.length < 3)          { document.getElementById('uErr').classList.add('show'); ok = false; }
  if (!em.includes('@'))     { document.getElementById('eErr').classList.add('show'); ok = false; }
  if (p.length < 8)          { document.getElementById('pErr').classList.add('show'); ok = false; }
  if (!ok) e.preventDefault();
});
</script>
</body>
</html>
