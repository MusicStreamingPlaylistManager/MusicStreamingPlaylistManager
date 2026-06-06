package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import utils.DBUtils;

public class UserDAO {

    public User login(String username, String password) throws Exception {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (Connection conn = DBUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null; //kh tìm thấy user thì trả về null
            }
        }
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
            return -1; // duplicate username
        }

        String sql = "INSERT INTO Users (Username, Password, Role) VALUES (?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
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
