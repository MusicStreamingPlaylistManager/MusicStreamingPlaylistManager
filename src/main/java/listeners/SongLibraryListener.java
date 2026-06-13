package listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import utils.SongLibraryUtils;

@WebListener
public class SongLibraryListener implements ServletContextListener {

    public static final String SONG_LIBRARY_ATTR = "songLibrary";
    public static final String SEARCH_ENGINE_ATTR = "songSearchEngine";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            SongLibraryUtils.reload(sce.getServletContext());
        } catch (Exception e) {
            sce.getServletContext().log("Failed to preload song library: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        context.removeAttribute(SONG_LIBRARY_ATTR);
        context.removeAttribute(SEARCH_ENGINE_ATTR);
    }
}
