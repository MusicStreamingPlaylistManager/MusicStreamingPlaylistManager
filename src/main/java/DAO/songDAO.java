package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import lib.DynamicArrayList;
import models.Song;
import utils.DBUtils;

public class songDAO {

    public DynamicArrayList getSongsByGenre(String genre, int limit) {

        DynamicArrayList songs = new DynamicArrayList();

        String sql = "SELECT FROM songs WHERE genre = ? LIMIT ?";

        try (
                 Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, genre);
            ps.setInt(2, limit);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Song song = new Song(
                        rs.getInt("song_id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getInt("duration")
                );

                songs.add(song);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return songs;
    }
}
