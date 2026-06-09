package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lib.DynamicArrayList;
import models.Song;
import models.SongDAO;

@WebServlet(name = "SongServlet", urlPatterns = {"/api/songs", "/api/songs/search"})
public class SongServlet extends HttpServlet {

    private final SongDAO songDAO = new SongDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Thiết lập header để trình duyệt hiểu dữ liệu trả về là JSON (API)
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String path = request.getServletPath();
        PrintWriter out = response.getWriter();
        
        try {
            DynamicArrayList songs = null;
            
            // Xử lý logic chia nhánh theo URL
            if ("/api/songs/search".equals(path)) {
                String q = request.getParameter("q");
                String genre = request.getParameter("genre");
                
                if (q != null && !q.trim().isEmpty()) {
                    songs = songDAO.searchByTitle(q.trim());
                } else if (genre != null && !genre.trim().isEmpty()) {
                    songs = songDAO.getSongsByGenre(genre, 50); // Lấy tối đa 50 bài theo thể loại
                } else {
                    songs = new DynamicArrayList();
                }
            } else {
                // Mặc định gọi /api/songs (Dành cho trang Home)
                songs = songDAO.getAllSongs();
            }

            // Chuyển đổi mảng DynamicArrayList thành JsonArray
            JsonArray jsonArray = new JsonArray();
            if (songs != null) {
                int limit = songs.size();
                String limitParam = request.getParameter("limit");
                if (limitParam != null) {
                    try {
                        int reqLimit = Integer.parseInt(limitParam);
                        if (reqLimit < limit) limit = reqLimit;
                    } catch (NumberFormatException e) {}
                }

                for (int i = 0; i < limit; i++) {
                    Song s = songs.get(i);
                    JsonObject obj = new JsonObject();
                    obj.addProperty("songId", s.getSongId());
                    obj.addProperty("title", s.getTitle());
                    obj.addProperty("artist", s.getArtist());
                    obj.addProperty("genre", s.getGenre());
                    obj.addProperty("duration", s.getDuration());
                    obj.addProperty("filePath", s.getFilePath());
                    obj.addProperty("coverPath", s.getCoverPath());
                    
                    // Tính toán chuỗi hiển thị thời lượng phút:giây (MM:SS) cho giao diện
                    int min = s.getDuration() / 60;
                    int sec = s.getDuration() % 60;
                    obj.addProperty("durationStr", min + ":" + (sec < 10 ? "0" + sec : sec));
                    
                    jsonArray.add(obj);
                }
            }

            // Trả về chuỗi JSON theo đúng định dạng mà app.js yêu cầu
            if ("/api/songs/search".equals(path)) {
                // file search.jsp cần object có thuộc tính "songs"
                JsonObject root = new JsonObject();
                root.add("songs", jsonArray);
                out.print(root.toString());
            } else {
                // file home.jsp cần trực tiếp array
                out.print(jsonArray.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}