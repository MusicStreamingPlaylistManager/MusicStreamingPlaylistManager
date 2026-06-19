package controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import models.User;
import models.UserDAO;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/ProfileServlet"})
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Không có giao diện riêng, chỉ xử lý POST → quay về trang profile.
        response.sendRedirect(request.getContextPath() + "/profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String newUsername = trim(request.getParameter("newUsername"));
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        boolean wantsName = newUsername != null && !newUsername.isEmpty();
        boolean wantsPass = newPassword != null && !newPassword.isEmpty();

        try {
            if (!wantsName && !wantsPass) {
                session.setAttribute("profileError", "Hãy nhập tên mới hoặc mật khẩu mới.");
                response.sendRedirect(request.getContextPath() + "/profile.jsp");
                return;
            }

            int userId = (Integer) session.getAttribute("userId");
            User current = (User) session.getAttribute("user");

            // --- Đổi tên ---
            if (wantsName && !newUsername.equalsIgnoreCase(current.getUsername())) {
                if (userDAO.usernameExists(newUsername)) {
                    session.setAttribute("profileError", "Tên \"" + newUsername + "\" đã được sử dụng.");
                    response.sendRedirect(request.getContextPath() + "/profile.jsp");
                    return;
                }
                if (!userDAO.updateUsername(userId, newUsername)) {
                    session.setAttribute("profileError", "Không cập nhật được tên. Vui lòng thử lại.");
                    response.sendRedirect(request.getContextPath() + "/profile.jsp");
                    return;
                }
                current.setUsername(newUsername);
                session.setAttribute("username", newUsername);
            }

            // --- Đổi mật khẩu ---
            if (wantsPass) {
                if (!newPassword.equals(confirmPassword)) {
                    session.setAttribute("profileError", "Mật khẩu xác nhận không khớp.");
                    response.sendRedirect(request.getContextPath() + "/profile.jsp");
                    return;
                }
                if (!userDAO.updatePassword(userId, newPassword)) {
                    session.setAttribute("profileError", "Không cập nhật được mật khẩu. Vui lòng thử lại.");
                    response.sendRedirect(request.getContextPath() + "/profile.jsp");
                    return;
                }
            }

            session.setAttribute("profileSuccess", "Cập nhật thông tin thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("profileError", "Lỗi hệ thống, vui lòng thử lại sau.");
        }

        response.sendRedirect(request.getContextPath() + "/profile.jsp");
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
