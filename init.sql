-- Chạy dòng này ở db postgres
-- DROP DATABASE IF EXISTS music_streaming_db WITH (FORCE);

-- -- Xong mới chạy dòng này
-- CREATE DATABASE music_streaming_db;

-- Rồi chạy đống dưới này
CREATE TABLE Users (
    UserID SERIAL PRIMARY KEY,
    Username VARCHAR(255) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Role VARCHAR(50) NOT NULL CHECK (Role IN ('Admin', 'User')),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Songs (
    SongID SERIAL PRIMARY KEY,
    Title VARCHAR(255) NOT NULL,
    Artist VARCHAR(255) NOT NULL,
    Genre VARCHAR(100) NOT NULL,
    Duration INT NOT NULL,
    FilePath TEXT NOT NULL,
    CoverPath TEXT NOT NULL
);

CREATE TABLE Playlists (
    PlaylistID SERIAL PRIMARY KEY,
    UserID INT NOT NULL,
    Name VARCHAR(255) NOT NULL,
    Type VARCHAR(50) NOT NULL CHECK (Type IN ('Favourite', 'Waiting')),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);

CREATE TABLE Playlist_Songs (
    PlaylistID INT NOT NULL,
    SongID INT NOT NULL,
    OrderIndex INT NOT NULL,
    PRIMARY KEY (PlaylistID, SongID),
    FOREIGN KEY (PlaylistID) REFERENCES Playlists(PlaylistID) ON DELETE CASCADE,
    FOREIGN KEY (SongID) REFERENCES Songs(SongID) ON DELETE CASCADE
);

INSERT INTO Users (Username, Password, Role) VALUES
('admin_phuc', 'hashed_password_123', 'Admin'),
('user_an', 'hashed_password_456', 'User'),
('user_khoi', 'hashed_password_789', 'User');

INSERT INTO Songs (Title, Artist, Genre, Duration, FilePath, CoverPath) VALUES
('Shape of You', 'Ed Sheeran', 'Pop', 233, 'https://supabase.co/storage/shapeofyou.mp3', 'https://supabase.co/storage/shape_cover.jpg'),
('Blinding Lights', 'The Weeknd', 'Pop', 200, 'https://supabase.co/storage/blinding.mp3', 'https://supabase.co/storage/blinding_cover.jpg'),
('As It Was', 'Harry Styles', 'Pop', 167, 'https://supabase.co/storage/asitwas.mp3', 'https://supabase.co/storage/asitwas_cover.jpg'),
('Someone Like You', 'Adele', 'Ballad', 284, 'https://supabase.co/storage/someone.mp3', 'https://supabase.co/storage/someone_cover.jpg'),
('All Of Me', 'John Legend', 'Ballad', 269, 'https://supabase.co/storage/allofme.mp3', 'https://supabase.co/storage/allofme_cover.jpg'),
('Perfect', 'Ed Sheeran', 'Ballad', 263, 'https://supabase.co/storage/perfect.mp3', 'https://supabase.co/storage/perfect_cover.jpg'),
('Wake Me Up', 'Avicii', 'EDM', 247, 'https://supabase.co/storage/wakemeup.mp3', 'https://supabase.co/storage/wake_cover.jpg'),
('Titanium', 'David Guetta', 'EDM', 245, 'https://supabase.co/storage/titanium.mp3', 'https://supabase.co/storage/titanium_cover.jpg');

INSERT INTO Playlists (UserID, Name, Type) VALUES
(2, 'Nhạc Trẻ Sôi Động', 'Favourite'),
(2, 'Danh Sách Chờ Tạm Thời', 'Waiting'),
(3, 'Chill Cuối Tuần', 'Favourite');

INSERT INTO Playlist_Songs (PlaylistID, SongID, OrderIndex) VALUES
(1, 1, 1),
(1, 2, 2),
(1, 7, 3);

INSERT INTO Playlist_Songs (PlaylistID, SongID, OrderIndex) VALUES
(2, 4, 1),
(2, 5, 2);

INSERT INTO Playlist_Songs (PlaylistID, SongID, OrderIndex) VALUES
(3, 6, 1),
(3, 4, 2);