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
import javax.servlet.http.HttpSession;
import lib.DynamicArrayList;
import lib.SongSearchEngine;
import models.Song;
import models.SongDAO;
import utils.JsonHelper;
import utils.SongLibraryUtils;

@WebServlet(name = "AdminServlet", urlPatterns = {"/api/admin/songs"})
public class AdminServlet extends HttpServlet {

    private final SongDAO songDAO = new SongDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            SongSearchEngine engine = SongLibraryUtils.getSearchEngine(getServletContext());
            DynamicArrayList songs = engine.getSortedLibrary();
            JsonObject root = new JsonObject();
            root.add("songs", JsonHelper.songsToJsonArray(songs));
            out.print(root.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String action = request.getParameter("action");
            JsonObject root = new JsonObject();

            if ("create".equals(action)) {
                Song song = buildSongFromRequest(request, 0);
                int id = songDAO.insertSong(song);
                root.addProperty("success", id > 0);
                root.addProperty("songId", id);
            } else if ("update".equals(action)) {
                int songId = Integer.parseInt(request.getParameter("songId"));
                Song song = buildSongFromRequest(request, songId);
                root.addProperty("success", songDAO.updateSong(song));
            } else if ("delete".equals(action)) {
                int songId = Integer.parseInt(request.getParameter("songId"));
                root.addProperty("success", songDAO.deleteSong(songId));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                root.addProperty("error", "Unknown action");
                out.print(root.toString());
                return;
            }

            if (root.has("success") && root.get("success").getAsBoolean()) {
                SongLibraryUtils.reload(getServletContext());
            }

            out.print(root.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    private Song buildSongFromRequest(HttpServletRequest request, int songId) {
        Song song = new Song();
        song.setSongId(songId);
        song.setTitle(request.getParameter("title"));
        song.setArtist(request.getParameter("artist"));
        song.setGenre(request.getParameter("genre"));
        song.setDuration(Integer.parseInt(request.getParameter("duration")));
        song.setFilePath(request.getParameter("filePath"));
        song.setCoverPath(request.getParameter("coverPath") != null ? request.getParameter("coverPath") : "");
        return song;
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null
                && session.getAttribute("user") != null
                && "Admin".equals(session.getAttribute("role"));
    }
}
