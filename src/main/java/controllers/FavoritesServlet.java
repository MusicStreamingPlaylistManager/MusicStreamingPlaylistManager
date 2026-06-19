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
import models.Playlist;
import models.PlaylistDAO;
import utils.JsonHelper;

@WebServlet(name = "FavoritesServlet", urlPatterns = {"/api/favorites", "/api/favorites/toggle"})
public class FavoritesServlet extends HttpServlet {

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

        try {
            int userId = getUserId(request);
            playlistDAO.ensureDefaultFavouritePlaylist(userId);

            Playlist fav = playlistDAO.getDefaultFavouritePlaylist(userId);
            DynamicArrayList songs = fav != null
                    ? playlistDAO.getSongsInPlaylist(fav.getPlaylistId())
                    : new DynamicArrayList();

            JsonArray songIds = new JsonArray();
            for (int i = 0; i < songs.size(); i++) {
                songIds.add(songs.get(i).getSongId());
            }

            JsonObject root = new JsonObject();
            root.add("songIds", songIds);
            root.add("songs", JsonHelper.songsToJsonArray(songs));
            root.addProperty("favouriteCount", songs.size());
            // playlistId của danh sách Favourite (mặc định) để phát đúng hàng chờ này.
            root.addProperty("playlistId", fav != null ? fav.getPlaylistId() : -1);
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
        if (!isLoggedIn(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int userId = getUserId(request);
            int songId = Integer.parseInt(request.getParameter("songId"));
            playlistDAO.ensureDefaultFavouritePlaylist(userId);

            Playlist fav = playlistDAO.getDefaultFavouritePlaylist(userId);
            if (fav == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Default favourite playlist not found\"}");
                return;
            }

            boolean alreadyLiked = playlistDAO.playlistSongExistsPublic(fav.getPlaylistId(), songId);
            boolean success;
            if (alreadyLiked) {
                success = playlistDAO.removeSongFromPlaylist(fav.getPlaylistId(), songId);
            } else {
                success = playlistDAO.addSongToPlaylist(fav.getPlaylistId(), songId);
            }

            JsonObject root = new JsonObject();
            root.addProperty("success", success);
            root.addProperty("liked", !alreadyLiked && success);
            out.print(root.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    private int getUserId(HttpServletRequest request) {
        return (Integer) request.getSession().getAttribute("userId");
    }
}
