package models;

import java.sql.Timestamp;

public class Playlist {
    private int playlistId;
    private int userId;
    private String name;
    private String type; //fav or waiting
    private Timestamp createdAt;
    private boolean isDefault; 

    public Playlist() {
    }

    public Playlist(int playlistId, int userId, String name, String type, boolean isDefault, Timestamp createdAt) {
        this.playlistId = playlistId;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
}
