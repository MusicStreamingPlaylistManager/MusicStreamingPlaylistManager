package utils;

import javax.servlet.ServletContext;
import lib.DynamicArrayList;
import lib.SongSearchEngine;
import listeners.SongLibraryListener;
import models.SongDAO;

public class SongLibraryUtils {

    public static void reload(ServletContext context) throws Exception {
        SongDAO songDAO = new SongDAO();
        DynamicArrayList songs = songDAO.getAllSongs();
        context.setAttribute(SongLibraryListener.SONG_LIBRARY_ATTR, songs);
        context.setAttribute(SongLibraryListener.SEARCH_ENGINE_ATTR, new SongSearchEngine(songs));
        context.log("Reloaded song library with " + songs.size() + " songs.");
    }

    public static SongSearchEngine getSearchEngine(ServletContext context) throws Exception {
        SongSearchEngine engine = (SongSearchEngine) context.getAttribute(SongLibraryListener.SEARCH_ENGINE_ATTR);
        if (engine == null) {
            reload(context);
            engine = (SongSearchEngine) context.getAttribute(SongLibraryListener.SEARCH_ENGINE_ATTR);
        }
        return engine;
    }
}
