/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp tiện ích hỗ trợ kết nối đến cơ sở dữ liệu PostgreSQL cục bộ (Local)
 * @author huynh
 */
public class DBUtils {
    
    // Cấu hình thông tin kết nối PostgreSQL Local
    private static final String HOST = "localhost";
    private static final String PORT = "5432"; // Cổng mặc định khi cài PostgreSQL
    private static final String DBNAME = "music_streaming_db"; // Đã cập nhật khớp với file init.sql
    private static final String USER = "postgres"; // Username mặc định của PostgreSQL
    private static final String PASSWORD = "csd"; // 

    // Chuỗi kết nối JDBC cho PostgreSQL Local
    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DBNAME;

    /**
     * Mở kết nối tới Database Local
     * @return Connection object
     * @throws ClassNotFoundException Nếu không tìm thấy Driver
     * @throws SQLException Nếu thông tin kết nối sai hoặc server chưa bật
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        // Load Driver của PostgreSQL vào bộ nhớ
        Class.forName("org.postgresql.Driver");
        
        // Thực hiện và trả về kết nối
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Hàm test nhanh kết nối (Run file này trực tiếp (Shift + F6) trong NetBeans để kiểm tra)
     */
    public static void main(String[] args) {
        try {
            System.out.println("Dang ket noi toi PostgreSQL Local (DB: music_streaming_db)...");
            Connection conn = DBUtils.getConnection();
            
            if (conn != null) {
                System.out.println("Ket noi PostgreSQL Local thanh cong!");
                // Đóng kết nối sau khi test xong
                conn.close();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Loi: Khong tim thay JDBC Driver cua PostgreSQL.");
            System.err.println("Hay chac chan ban da add file postgresql-x.x.x.jar vao thu muc Libraries cua project!");
        } catch (SQLException e) {
            System.err.println("Loi ket noi Database Local: " + e.getMessage());
            System.err.println("Meo: Hay chac chan ban da chay file init.sql de tao database 'music_streaming_db' truoc nhe.");
            e.printStackTrace();
        }
    }
}