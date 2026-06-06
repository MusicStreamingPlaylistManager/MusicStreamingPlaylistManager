package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import lib.DynamicArrayList;
import lib.IndexedDoublyLinkedList;
import utils.DBUtils;

public class PlaylistDAO {

    public int createPlaylist(int userId, String name, String type, boolean isDefault) throws Exception {
        if (playlistExists(userId, name, type)) return -1; //check duplicate

        String sql = "INSERT INTO Playlists (UserID, Name, Type, IsDefault) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setBoolean(4, isDefault);  

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public boolean playlistExists(int userId, String name, String type) throws Exception {
        String sql = "SELECT 1 FROM Playlists "
                   + "WHERE UserID = ? AND LOWER(Name) = LOWER(?) AND Type = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, type);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Playlist getById(int playlistId) throws Exception {
        String sql = "SELECT * FROM Playlists WHERE PlaylistID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playlistId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapPlaylist(rs) : null;
            }
        }
    }

    public DynamicArrayList getPlaylistsByUserId(int userId) throws Exception {
        String sql = "SELECT * FROM Playlists WHERE UserID = ? ORDER BY CreatedAt DESC";
        DynamicArrayList playlists = new DynamicArrayList();

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    playlists.add(null); // DynamicArrayList hiện chỉ chứa Song, nên không phù hợp Playlist.
                }
            }
        }

        return playlists;
    }

    public boolean deletePlaylist(int playlistId, int userId) throws Exception {
        String sql = "DELETE FROM Playlists WHERE PlaylistID = ? AND UserID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playlistId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean addSongToPlaylist(int playlistId, int songId) throws Exception {
        if (playlistSongExists(playlistId, songId)) {
            return false; // duplicate song in same playlist
        }

        String sql = "INSERT INTO Playlist_Songs (PlaylistID, SongID, OrderIndex) "
                   + "VALUES (?, ?, ?)";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playlistId);
            ps.setInt(2, songId);
            ps.setInt(3, getNextOrderIndex(playlistId));

            return ps.executeUpdate() > 0;
        }
    }

    public boolean removeSongFromPlaylist(int playlistId, int songId) throws Exception {
        String sql = "DELETE FROM Playlist_Songs WHERE PlaylistID = ? AND SongID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playlistId);
            ps.setInt(2, songId);

            boolean removed = ps.executeUpdate() > 0;
            if (removed) {
                normalizeOrderIndex(playlistId);
            }
            return removed;
        }
    }

    public DynamicArrayList getSongsInPlaylist(int playlistId) throws Exception {
        String sql = "SELECT s.* FROM Songs s "
                   + "JOIN Playlist_Songs ps ON s.SongID = ps.SongID "
                   + "WHERE ps.PlaylistID = ? "
                   + "ORDER BY ps.OrderIndex ASC";

        DynamicArrayList songs = new DynamicArrayList();

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playlistId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapSong(rs));
                }
            }
        }

        return songs;
    }

    // Load playlist/favourite/waiting list into IndexedDoublyLinkedList for AudioPlayEngine.
    public IndexedDoublyLinkedList loadWaitingList(int playlistId) throws Exception {
        DynamicArrayList songs = getSongsInPlaylist(playlistId);
        IndexedDoublyLinkedList waitingList = new IndexedDoublyLinkedList();

        for (int i = 0; i < songs.size(); i++) {
            waitingList.append(songs.get(i));
        }

        return waitingList;
    }

    // Persistent version of drag-and-drop reorder.
    public boolean moveSongAfter(int playlistId, int songIdToMove, int targetSongId) throws Exception {
        if (songIdToMove == targetSongId) return false;
        if (!playlistSongExists(playlistId, songIdToMove)) return false;
        if (!playlistSongExists(playlistId, targetSongId)) return false;

        DynamicArrayList oldSongs = getSongsInPlaylist(playlistId);
        int size = oldSongs.size();
        int[] order = new int[size];
        int count = 0;

        for (int i = 0; i < size; i++) {
            int currentId = oldSongs.get(i).getSongId();
            if (currentId != songIdToMove) {
                order[count++] = currentId;
            }
        }

        int[] newOrder = new int[size];
        int newCount = 0;
        for (int i = 0; i < count; i++) {
            newOrder[newCount++] = order[i];
            if (order[i] == targetSongId) {
                newOrder[newCount++] = songIdToMove;
            }
        }

        return updateOrderIndexes(playlistId, newOrder);
    }

    private boolean updateOrderIndexes(int playlistId, int[] songIds) throws Exception {
        String sql = "UPDATE Playlist_Songs SET OrderIndex = ? "
                   + "WHERE PlaylistID = ? AND SongID = ?";

        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < songIds.length; i++) {
                    ps.setInt(1, i + 1);
                    ps.setInt(2, playlistId);
                    ps.setInt(3, songIds[i]);
                    ps.addBatch();
                }

                ps.executeBatch();
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private boolean playlistSongExists(int playlistId, int songId) throws Exception {
        String sql = "SELECT 1 FROM Playlist_Songs WHERE PlaylistID = ? AND SongID = ?";
        try (Connection conn = DBUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playlistId);
            ps.setInt(2, songId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int getNextOrderIndex(int playlistId) throws Exception {
        String sql = "SELECT COALESCE(MAX(OrderIndex), 0) + 1 AS NextIndex "
                   + "FROM Playlist_Songs WHERE PlaylistID = ?";
        try (Connection conn = DBUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playlistId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("NextIndex") : 1;
            }
        }
    }

    private void normalizeOrderIndex(int playlistId) throws Exception {
        DynamicArrayList songs = getSongsInPlaylist(playlistId);
        int[] songIds = new int[songs.size()];

        for (int i = 0; i < songs.size(); i++) {
            songIds[i] = songs.get(i).getSongId();
        }

        updateOrderIndexes(playlistId, songIds);
    }
    
    // Lấy playlist favourite mặc định của user
    public Playlist getDefaultFavouritePlaylist(int userId) throws Exception {
        String sql = "SELECT * FROM Playlists WHERE UserID = ? AND Type = 'fav' AND IsDefault = TRUE";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapPlaylist(rs) : null;
            }
        }
    }

    //gọi khi user bấm "Like" một bài hát
    public boolean likeSong(int userId, int songId) throws Exception {
        Playlist defaultFav = getDefaultFavouritePlaylist(userId);
        if (defaultFav == null) return false; // chưa có playlist mặc định

        return addSongToPlaylist(defaultFav.getPlaylistId(), songId);
    }

    private Playlist mapPlaylist(ResultSet rs) throws Exception {
        return new Playlist(
            rs.getInt("PlaylistID"),
            rs.getInt("UserID"),
            rs.getString("Name"),
            rs.getString("Type"),
            rs.getBoolean("IsDefault"),  
            rs.getTimestamp("CreatedAt")
        );
    }

    private Song mapSong(ResultSet rs) throws Exception {
        return new Song(
            rs.getInt("SongID"),
            rs.getString("Title"),
            rs.getString("Artist"),
            rs.getString("Genre"),
            rs.getInt("Duration"),
            rs.getString("FilePath"),
            rs.getString("CoverPath")
        );
    }
}