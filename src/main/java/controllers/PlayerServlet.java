package controllers;

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
import lib.IndexedDoublyLinkedList;
import models.PlaylistDAO;
import models.Song;
import models.SongDAO;
import utils.JsonHelper;

@WebServlet(name = "PlayerServlet", urlPatterns = {
    "/api/player/play",
    "/api/player/next",
    "/api/player/prev",
    "/api/player/shuffle",
    "/api/player/loop",
    "/api/player/history",
    "/api/player/waitlist",
    "/api/player/save-waiting"
})
public class PlayerServlet extends HttpServlet {

    public static final String SESSION_ENGINE = "playEngine";
    public static final String SESSION_WAITING_LIST = "sessionWaitingList";

    private final SongDAO songDAO = new SongDAO();
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
        String path = request.getServletPath();
        HttpSession session = request.getSession();

        try {
            AudioPlayEngine engine = getOrCreateEngine(session);

            if ("/api/player/next".equals(path)) {
                Song track = engine.nextTrack();
                writeTrackResponse(out, engine, track);
            } else if ("/api/player/prev".equals(path)) {
                Song track = engine.previousTrack();
                writeTrackResponse(out, engine, track);
            } else if ("/api/player/history".equals(path)) {
                JsonObject root = new JsonObject();
                root.add("songs", JsonHelper.songsToJsonArray(engine.getPlaybackHistory().toList()));
                out.print(root.toString());
            } else if ("/api/player/waitlist".equals(path)) {
                JsonObject root = new JsonObject();
                root.add("waitList", JsonHelper.waitListToJson(engine.getWaitingList()));
                out.print(root.toString());
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
        String path = request.getServletPath();
        HttpSession session = request.getSession();

        try {
            if ("/api/player/play".equals(path)) {
                handlePlay(request, session, out);
            } else if ("/api/player/shuffle".equals(path)) {
                AudioPlayEngine engine = getOrCreateEngine(session);
                boolean enabled = "true".equalsIgnoreCase(request.getParameter("enabled"));
                if (enabled) {
                    engine.shuffleUpcoming();
                }
                syncWaitingList(session, engine);
                JsonObject root = new JsonObject();
                root.addProperty("success", true);
                root.add("waitList", JsonHelper.waitListToJson(engine.getWaitingList()));
                out.print(root.toString());
            } else if ("/api/player/loop".equals(path)) {
                AudioPlayEngine engine = getOrCreateEngine(session);
                String mode = request.getParameter("mode");
                engine.setRepeatAll("all".equals(mode));
                engine.setRepeatOne("one".equals(mode));
                JsonObject root = new JsonObject();
                root.addProperty("success", true);
                out.print(root.toString());
            } else if ("/api/player/save-waiting".equals(path)) {
                handleSaveWaiting(request, session, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    private void handlePlay(HttpServletRequest request, HttpSession session, PrintWriter out)
            throws Exception {
        String songIdParam = request.getParameter("songId");
        String playlistIdParam = request.getParameter("playlistId");

        if (songIdParam == null || songIdParam.trim().isEmpty()) {
            responseError(out, "Missing songId");
            return;
        }

        int songId = Integer.parseInt(songIdParam);
        Song song = songDAO.getById(songId);
        if (song == null) {
            responseError(out, "Song not found");
            return;
        }

        IndexedDoublyLinkedList waitingList;
        boolean inMemoryWaiting;

        if (playlistIdParam != null && !playlistIdParam.trim().isEmpty()) {
            waitingList = playlistDAO.loadWaitingList(Integer.parseInt(playlistIdParam));
            inMemoryWaiting = false;
        } else {
            waitingList = AudioPlayEngine.buildQuickPlayQueue(song, songDAO);
            inMemoryWaiting = true;
        }

        AudioPlayEngine engine = new AudioPlayEngine(waitingList);
        engine.playFromSong(song);
        session.setAttribute(SESSION_ENGINE, engine);
        session.setAttribute(SESSION_WAITING_LIST, waitingList);
        session.setAttribute("waitingListInMemory", inMemoryWaiting);

        JsonObject root = new JsonObject();
        root.addProperty("success", true);
        root.addProperty("inMemoryWaiting", inMemoryWaiting);
        root.add("track", JsonHelper.songToJson(song));
        root.add("waitList", JsonHelper.waitListToJson(waitingList));
        out.print(root.toString());
    }

    private void handleSaveWaiting(HttpServletRequest request, HttpSession session, PrintWriter out)
            throws Exception {
        String name = request.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            responseError(out, "Playlist name is required");
            return;
        }

        AudioPlayEngine engine = (AudioPlayEngine) session.getAttribute(SESSION_ENGINE);
        if (engine == null || engine.getWaitingList() == null || engine.getWaitingList().getHead() == null) {
            responseError(out, "No waiting list to save");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        int playlistId = playlistDAO.saveWaitingListAsFavourite(userId, name.trim(), engine.getWaitingList());

        JsonObject root = new JsonObject();
        root.addProperty("success", playlistId > 0);
        root.addProperty("playlistId", playlistId);
        out.print(root.toString());
    }

    private AudioPlayEngine getOrCreateEngine(HttpSession session) {
        AudioPlayEngine engine = (AudioPlayEngine) session.getAttribute(SESSION_ENGINE);
        if (engine == null) {
            IndexedDoublyLinkedList waitingList = new IndexedDoublyLinkedList();
            engine = new AudioPlayEngine(waitingList);
            session.setAttribute(SESSION_ENGINE, engine);
            session.setAttribute(SESSION_WAITING_LIST, waitingList);
            session.setAttribute("waitingListInMemory", true);
        }
        return engine;
    }

    private void syncWaitingList(HttpSession session, AudioPlayEngine engine) {
        session.setAttribute(SESSION_WAITING_LIST, engine.getWaitingList());
    }

    private void writeTrackResponse(PrintWriter out, AudioPlayEngine engine, Song track) {
        JsonObject root = new JsonObject();
        if (track != null) {
            root.add("track", JsonHelper.songToJson(track));
        }
        root.add("waitList", JsonHelper.waitListToJson(engine.getWaitingList()));
        out.print(root.toString());
    }

    private void responseError(PrintWriter out, String message) {
        JsonObject root = new JsonObject();
        root.addProperty("error", message);
        out.print(root.toString());
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }
}
