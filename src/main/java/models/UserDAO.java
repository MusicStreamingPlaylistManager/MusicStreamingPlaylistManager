package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import utils.DBUtils;
import utils.PasswordUtils;

public class UserDAO {

    public User login(String username, String password) throws Exception {
        User user = getByUsername(username);
        if (user == null) {
            return null;
        }
        if (!PasswordUtils.verify(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public boolean usernameExists(String username) throws Exception {
        String sql = "SELECT 1 FROM Users WHERE LOWER(Username) = LOWER(?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int register(String username, String password, String role) throws Exception {
        if (usernameExists(username)) {
            return -1;
        }

        String sql = "INSERT INTO Users (Username, Password, Role) VALUES (?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, PasswordUtils.hash(password));
            ps.setString(3, role);

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public boolean updateUsername(int userId, String newUsername) throws Exception {
        String sql = "UPDATE Users SET Username = ? WHERE UserID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newUsername);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int userId, String newPassword) throws Exception {
        String sql = "UPDATE Users SET Password = ? WHERE UserID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PasswordUtils.hash(newPassword));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public User getById(int userId) throws Exception {
        String sql = "SELECT * FROM Users WHERE UserID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public User getByUsername(String username) throws Exception {
        String sql = "SELECT * FROM Users WHERE LOWER(Username) = LOWER(?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    private User mapUser(ResultSet rs) throws Exception {
        return new User(
            rs.getInt("UserID"),
            rs.getString("Username"),
            rs.getString("Password"),
            rs.getString("Role"),
            rs.getTimestamp("CreatedAt")
        );
    }
}
