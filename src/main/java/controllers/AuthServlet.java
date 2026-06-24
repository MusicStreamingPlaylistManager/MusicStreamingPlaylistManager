package controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import models.PlaylistDAO;
import models.User;
import models.UserDAO;

@WebServlet(name = "AuthServlet", urlPatterns = {"/AuthServlet"})
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate(); // Xóa session khi đăng xuất
            }
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/home.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        try {
            if ("login".equals(action)) {
                String username = request.getParameter("username"); 
                String password = request.getParameter("password");

                User user = userDAO.login(username, password);

                if (user != null) {
                    // Đăng nhập thành công, lưu thông tin vào session
                    session.setAttribute("user", user);
                    session.setAttribute("userId", user.getUserId());
                    session.setAttribute("username", user.getUsername());

                    response.sendRedirect(request.getContextPath() + "/home.jsp");
                } else {
                    session.setAttribute("loginError", "Sai tên đăng nhập hoặc mật khẩu!");
                    response.sendRedirect(request.getContextPath() + "/login.jsp");
                }

            } else if ("register".equals(action)) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                // Mặc định người dùng đăng ký mới sẽ có quyền 'User'
                int newUserId = userDAO.register(username, password);

                if (newUserId > 0) {
                    PlaylistDAO playlistDAO = new PlaylistDAO();
                    playlistDAO.ensureDefaultFavouritePlaylist(newUserId);

                    User newUser = userDAO.getById(newUserId);
                    session.setAttribute("user", newUser);
                    session.setAttribute("userId", newUser.getUserId());
                    session.setAttribute("username", newUser.getUsername());

                    response.sendRedirect(request.getContextPath() + "/home.jsp");
                } else {
                    session.setAttribute("registerError", "Tên đăng nhập đã tồn tại!");
                    response.sendRedirect(request.getContextPath() + "/register.jsp");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ("login".equals(action)) {
                session.setAttribute("loginError", "Lỗi hệ thống, vui lòng thử lại sau.");
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            } else {
                session.setAttribute("registerError", "Lỗi hệ thống, vui lòng thử lại sau.");
                response.sendRedirect(request.getContextPath() + "/register.jsp");
            }
        }
    }
}