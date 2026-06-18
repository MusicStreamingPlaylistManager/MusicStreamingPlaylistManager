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
import lib.DynamicPlaylistList;
import models.Playlist;
import models.PlaylistDAO;
import utils.JsonHelper;

@WebServlet(name = "PlaylistServlet", urlPatterns = {"/api/playlists", "/api/playlists/*"})
public class PlaylistServlet extends HttpServlet {

    private final PlaylistDAO playlistDAO = new PlaylistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isLoggedIn(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();

        try {
            int userId = getUserId(request);

            if (pathInfo == null || "/".equals(pathInfo)) {
                String songIdParam = request.getParameter("songId");
                if (songIdParam != null) {
                    int songId = Integer.parseInt(songIdParam);
                    writePlaylistListWithSongStatus(out, userId, songId);
                } else {
                    writePlaylistList(out, userId);
                }
            } else {
                int playlistId = Integer.parseInt(pathInfo.substring(1));
                writePlaylistDetail(out, userId, playlistId);
            }
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
        if (!isLoggedIn(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String path = request.getServletPath() + (request.getPathInfo() != null ? request.getPathInfo() : "");

        try {
            int userId = getUserId(request);

            if (path.endsWith("/create")) {
                handleCreate(request, out, userId);
            } else if (path.endsWith("/delete")) {
                handleDelete(request, out, userId);
            } else if (path.endsWith("/addSong")) {
                handleAddSong(request, out, userId);
            } else if (path.endsWith("/removeSong")) {
                handleRemoveSong(request, out, userId);
            } else if (path.endsWith("/reorder")) {
                handleReorder(request, out, userId);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Unknown action\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    private void writePlaylistList(PrintWriter out, int userId) throws Exception {
        playlistDAO.ensureDefaultFavouritePlaylist(userId);
        DynamicPlaylistList list = playlistDAO.getPlaylistsByUserId(userId);
        Playlist fav = playlistDAO.getDefaultFavouritePlaylist(userId);
        int favouriteCount = fav != null ? playlistDAO.countSongsInPlaylist(fav.getPlaylistId()) : 0;

        JsonArray playlists = new JsonArray();
        for (int i = 0; i < list.size(); i++) {
            Playlist pl = list.get(i);
            if (pl.isIsDefault()) continue;

            JsonObject obj = new JsonObject();
            obj.addProperty("playlistId", pl.getPlaylistId());
            obj.addProperty("name", pl.getName());
            obj.addProperty("type", pl.getType());
            obj.addProperty("songCount", playlistDAO.countSongsInPlaylist(pl.getPlaylistId()));
            playlists.add(obj);
        }

        JsonObject root = new JsonObject();
        root.add("playlists", playlists);
        root.addProperty("favouriteCount", favouriteCount);
        out.print(root.toString());
    }

    private void writePlaylistDetail(PrintWriter out, int userId, int playlistId) throws Exception {
        Playlist playlist = playlistDAO.getById(playlistId);
        if (playlist == null || playlist.getUserId() != userId) {
            out.print("{\"error\":\"Playlist not found\"}");
            return;
        }

        DynamicArrayList songs = playlistDAO.getSongsInPlaylist(playlistId);
        JsonObject playlistJson = new JsonObject();
        playlistJson.addProperty("playlistId", playlist.getPlaylistId());
        playlistJson.addProperty("name", playlist.getName());
        playlistJson.addProperty("type", playlist.getType());

        JsonObject root = new JsonObject();
        root.add("playlist", playlistJson);
        root.add("songs", JsonHelper.songsToJsonArray(songs));
        out.print(root.toString());
    }

    private void handleCreate(HttpServletRequest request, PrintWriter out, int userId) throws Exception {
        String name = request.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            out.print("{\"success\":false,\"error\":\"Name is required\"}");
            return;
        }

        int id = playlistDAO.createPlaylist(userId, name.trim(), "Favourite", false);
        JsonObject root = new JsonObject();
        root.addProperty("success", id > 0);
        root.addProperty("playlistId", id);
        out.print(root.toString());
    }

    private void handleDelete(HttpServletRequest request, PrintWriter out, int userId) throws Exception {
        int playlistId = Integer.parseInt(request.getParameter("playlistId"));
        Playlist playlist = playlistDAO.getById(playlistId);
        if (playlist == null || playlist.getUserId() != userId || playlist.isIsDefault()) {
            out.print("{\"success\":false}");
            return;
        }

        JsonObject root = new JsonObject();
        root.addProperty("success", playlistDAO.deletePlaylist(playlistId, userId));
        out.print(root.toString());
    }

    private void handleRemoveSong(HttpServletRequest request, PrintWriter out, int userId) throws Exception {
        int playlistId = Integer.parseInt(request.getParameter("playlistId"));
        int songId = Integer.parseInt(request.getParameter("songId"));
        Playlist playlist = playlistDAO.getById(playlistId);
        if (playlist == null || playlist.getUserId() != userId) {
            out.print("{\"success\":false}");
            return;
        }

        JsonObject root = new JsonObject();
        root.addProperty("success", playlistDAO.removeSongFromPlaylist(playlistId, songId));
        out.print(root.toString());
    }

    private void handleReorder(HttpServletRequest request, PrintWriter out, int userId) throws Exception {
        int playlistId = Integer.parseInt(request.getParameter("playlistId"));
        String order = request.getParameter("order");
        Playlist playlist = playlistDAO.getById(playlistId);
        if (playlist == null || playlist.getUserId() != userId || order == null) {
            out.print("{\"success\":false}");
            return;
        }

        String[] parts = order.split(",");
        int[] songIds = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            songIds[i] = Integer.parseInt(parts[i].trim());
        }

        JsonObject root = new JsonObject();
        root.addProperty("success", playlistDAO.updateOrderIndexesPublic(playlistId, songIds));
        out.print(root.toString());
    }

    private void handleAddSong(HttpServletRequest request, PrintWriter out, int userId) throws Exception {
        int playlistId = Integer.parseInt(request.getParameter("playlistId"));
        int songId = Integer.parseInt(request.getParameter("songId"));
        Playlist playlist = playlistDAO.getById(playlistId);
        if (playlist == null || playlist.getUserId() != userId) {
            out.print("{\"success\":false,\"error\":\"Playlist not found\"}");
            return;
        }

        JsonObject root = new JsonObject();
        root.addProperty("success", playlistDAO.addSongToPlaylist(playlistId, songId));
        out.print(root.toString());
    }

    private void writePlaylistListWithSongStatus(PrintWriter out, int userId, int songId) throws Exception {
        playlistDAO.ensureDefaultFavouritePlaylist(userId);
        DynamicPlaylistList list = playlistDAO.getPlaylistsByUserId(userId);

        JsonArray playlists = new JsonArray();
        for (int i = 0; i < list.size(); i++) {
            Playlist pl = list.get(i);

            JsonObject obj = new JsonObject();
            obj.addProperty("playlistId", pl.getPlaylistId());
            obj.addProperty("name", pl.getName());
            obj.addProperty("type", pl.getType());
            obj.addProperty("isDefault", pl.isIsDefault());
            obj.addProperty("songCount", playlistDAO.countSongsInPlaylist(pl.getPlaylistId()));
            obj.addProperty("containsSong", playlistDAO.playlistSongExistsPublic(pl.getPlaylistId(), songId));
            playlists.add(obj);
        }

        JsonObject root = new JsonObject();
        root.add("playlists", playlists);
        out.print(root.toString());
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    private int getUserId(HttpServletRequest request) {
        return (Integer) request.getSession().getAttribute("userId");
    }
}
