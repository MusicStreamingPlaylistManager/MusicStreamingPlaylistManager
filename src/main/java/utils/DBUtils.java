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
 * 
 * @author huynh
 */
public class DBUtils {

    // ==========================================
    // CẤU HÌNH KẾT NỐI SUPABASE (ONLINE)
    // ==========================================
    // Bạn lấy thông tin này trên Supabase: Project Settings -> Database ->
    // Connection string -> JDBC
    // Lưu ý: Đổi URL, USER, PASSWORD theo đúng project của bạn
    private static final String HOST = "aws-0-ap-southeast-1.pooler.supabase.com"; // Thay bằng Host của bạn (thường có
                                                                                   // chữ
    // pooler)
    private static final String PORT = "6543"; // Dùng cổng 6543 để hỗ trợ IPv4 (Connection Pooling)
    private static final String DBNAME = "postgres"; // Mặc định của Supabase luôn là postgres
    private static final String USER = "postgres.wdqxtunkwlnumpsisgwp"; // Thay bằng User của bạn
    private static final String PASSWORD = "EydcO1xYCmIUQSD2"; // Mật khẩu lúc bạn tạo project Supabase

    // Chuỗi kết nối JDBC cho Supabase
    // Cần thêm ?sslmode=require vì Supabase yêu cầu mã hóa SSL
    // Trong JDBC, chuỗi URL bắt buộc phải bắt đầu bằng "jdbc:postgresql://"
    // Supabase (khi dùng qua pooler) yêu cầu thêm prepareThreshold=0
    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DBNAME
            + "?sslmode=require&prepareThreshold=0";

    // ==========================================
    // CẤU HÌNH KẾT NỐI POSTGRESQL LOCAL (OFFLINE)
    // (Bôi đen Ctrl + / để mở khóa khi cần dùng lại Local)
    // ==========================================
    // private static final String HOST = "localhost";
    // private static final String PORT = "5432";
    // private static final String DBNAME = "music_streaming_db";
    // private static final String USER = "postgres";
    // private static final String PASSWORD = "123";
    // private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT +
    // "/" + DBNAME;

    /**
     * Mở kết nối tới Database Local
     * 
     * @return Connection object
     * @throws ClassNotFoundException Nếu không tìm thấy Driver
     * @throws SQLException           Nếu thông tin kết nối sai hoặc server chưa bật
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        // Load Driver của PostgreSQL vào bộ nhớ
        Class.forName("org.postgresql.Driver");

        // Thực hiện và trả về kết nối
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Hàm test nhanh kết nối (Run file này trực tiếp (Shift + F6) trong NetBeans để
     * kiểm tra)
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
            System.err.println(
                    "Meo: Hay chac chan ban da chay file init.sql de tao database 'music_streaming_db' truoc nhe.");
            e.printStackTrace();
        }
    }
}
