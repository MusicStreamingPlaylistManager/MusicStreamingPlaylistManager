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
import lib.SongSearchEngine;
import models.Song;
import utils.JsonHelper;
import utils.SongLibraryUtils;

@WebServlet(name = "SongServlet", urlPatterns = {"/api/songs", "/api/songs/search"})
public class SongServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();
        PrintWriter out = response.getWriter();

        try {
            SongSearchEngine searchEngine = SongLibraryUtils.getSearchEngine(getServletContext());
            DynamicArrayList songs;

            if ("/api/songs/search".equals(path)) {
                String q = request.getParameter("q");
                String genre = request.getParameter("genre");

                if (q != null && !q.trim().isEmpty()) {
                    songs = searchEngine.searchByTitle(q.trim());
                } else if (genre != null && !genre.trim().isEmpty()) {
                    songs = searchEngine.filterByGenre(genre);
                } else {
                    songs = new DynamicArrayList();
                }
            } else {
                songs = searchEngine.getSortedLibrary();
            }

            JsonArray jsonArray = buildSongArray(songs, request.getParameter("limit"));

            if ("/api/songs/search".equals(path)) {
                JsonObject root = new JsonObject();
                root.add("songs", jsonArray);
                out.print(root.toString());
            } else {
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

    private JsonArray buildSongArray(DynamicArrayList songs, String limitParam) {
        JsonArray jsonArray = new JsonArray();
        if (songs == null) return jsonArray;

        int limit = songs.size();
        if (limitParam != null) {
            try {
                int reqLimit = Integer.parseInt(limitParam);
                if (reqLimit < limit) limit = reqLimit;
            } catch (NumberFormatException e) {
            }
        }

        for (int i = 0; i < limit; i++) {
            Song s = songs.get(i);
            jsonArray.add(JsonHelper.songToJson(s));
        }
        return jsonArray;
    }
}
