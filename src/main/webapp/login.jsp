<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  // If already logged in, redirect to home
  if (session.getAttribute("user") != null) {
    response.sendRedirect(request.getContextPath() + "/home.jsp");
    return;
  }
  String errorMsg = (String) session.getAttribute("loginError");
  if (errorMsg != null) session.removeAttribute("loginError");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SoundWave — Login</title>
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

    <form method="post" action="<%= request.getContextPath() %>/AuthServlet" id="loginForm">
      <input type="hidden" name="action" value="login">

      <div class="field-group">
        <label class="field-label" for="username">Username</label>
        <input class="auth-input" type="text" id="username" name="username"
               placeholder="Enter your username" required autocomplete="username">
        <div class="error-msg" id="userErr">Please enter your username.</div>
      </div>

      <div class="field-group">
        <label class="field-label" for="password">Password</label>
        <input class="auth-input" type="password" id="password" name="password"
               placeholder="••••••••" required autocomplete="current-password">
        <div class="error-msg" id="passErr">Password is required.</div>
      </div>

      <button type="submit" class="btn-primary" style="margin-top:0.5rem">Login</button>
    </form>

    <div class="auth-divider">OR</div>
    <p class="auth-link">Don't have an account? <a href="<%= request.getContextPath() %>/register.jsp">Register</a></p>
  </div>
</div>

<script>
document.getElementById('loginForm').addEventListener('submit', function(e) {
  let ok = true;
  const u = document.getElementById('username').value.trim();
  const pass  = document.getElementById('password').value;
  const userErr = document.getElementById('userErr');
  const passErr = document.getElementById('passErr');

  userErr.classList.remove('show');
  passErr.classList.remove('show');

  if (u.length < 1) {
    userErr.classList.add('show'); ok = false;
  }
  if (pass.length < 1) {
    passErr.classList.add('show'); ok = false;
  }
  if (!ok) e.preventDefault();
});
</script>
</body>
</html>