package models;

public class Song {
    private int songId;       
    private String title;     
    private String artist;    
    private String genre;     
    private int duration;
    private String filePath;
    private String coverPath;

    public Song() {
    }

    public Song (int songId, String title, String artist, String genre, int duration) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
    }
    
    // Constructor theo database init.sql
    public Song(int songId, String title, String artist, String genre,
                int duration, String filePath, String coverPath) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
        this.filePath = filePath;
        this.coverPath = coverPath;
    }


    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }
}
