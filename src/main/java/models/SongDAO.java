package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import lib.DynamicArrayList;
import utils.DBUtils;

public class SongDAO {

    public Song getById(int songId) throws Exception {
        String sql = "SELECT * FROM Songs WHERE SongID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, songId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapSong(rs) : null;
            }
        }
    }

    public DynamicArrayList getAllSongs() throws Exception {
        String sql = "SELECT * FROM Songs ORDER BY Title ASC";
        DynamicArrayList songs = new DynamicArrayList();

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                songs.add(mapSong(rs));
            }
        }

        return songs;
    }

    public DynamicArrayList searchByTitle(String keyword) throws Exception {
        String sql = "SELECT * FROM Songs WHERE LOWER(Title) LIKE LOWER(?) ORDER BY Title ASC";
        DynamicArrayList songs = new DynamicArrayList();

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapSong(rs));
                }
            }
        }

        return songs;
    }

    public DynamicArrayList getSongsByGenre(String genre, int limit) throws Exception {
        String sql = "SELECT * FROM Songs WHERE LOWER(Genre) = LOWER(?) ORDER BY SongID ASC LIMIT ?";
        DynamicArrayList songs = new DynamicArrayList();

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, genre);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapSong(rs));
                }
            }
        }

        return songs;
    }

    public boolean songExists(String title, String artist) throws Exception {
        String sql = "SELECT 1 FROM Songs WHERE LOWER(Title) = LOWER(?) AND LOWER(Artist) = LOWER(?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, artist);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insertSong(Song song) throws Exception {
        if (songExists(song.getTitle(), song.getArtist())) {
            return -1; // duplicate song
        }

        String sql = "INSERT INTO Songs (Title, Artist, Genre, Duration, FilePath, CoverPath) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, song.getTitle());
            ps.setString(2, song.getArtist());
            ps.setString(3, song.getGenre());
            ps.setInt(4, song.getDuration());
            ps.setString(5, song.getFilePath());
            ps.setString(6, song.getCoverPath());

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
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