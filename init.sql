-- Chạy dòng này ở db postgres
-- DROP DATABASE IF EXISTS music_streaming_db WITH (FORCE);

-- -- Xong mới chạy dòng này
-- CREATE DATABASE music_streaming_db;

-- Rồi chạy đống dưới này
-- 1. XÓA TOÀN BỘ CÁC BẢNG CŨ (NẾU CÓ) ĐỂ TRÁNH LỖI CONFLICT
DROP TABLE IF EXISTS Playlist_Songs CASCADE;
DROP TABLE IF EXISTS Playlists CASCADE;
DROP TABLE IF EXISTS Songs CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

-- 2. TẠO LẠI CÁC BẢNG THEO ĐÚNG THIẾT KẾ DTO REPORT 2

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
    IsDefault BOOLEAN DEFAULT FALSE, -- Đã bổ sung trường isDefault khớp với Playlist DTO
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

-- 3. CHÈN DỮ LIỆU MẪU (DUMMY DATA)

INSERT INTO Users (Username, Password, Role) VALUES
('admin_phuc', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin'),
('user_an', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5d5a86aff3ca12020c923adc6ca6', 'User'),
('user_khoi', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5d5a86aff3ca12020c923adc6ca6', 'User');
-- Passwords above are SHA-256 hashes: admin123 / 123456

-- Chèn Playlists mẫu
INSERT INTO Playlists (UserID, Name, Type, IsDefault) VALUES
(2, 'Nhạc Trẻ Sôi Động', 'Favourite', FALSE),
(2, 'Danh Sách Chờ Tạm Thời', 'Waiting', FALSE),
(3, 'Chill Cuối Tuần', 'Favourite', FALSE),
(2, 'Liked Songs', 'Favourite', TRUE); -- Thêm 1 playlist mặc định (isDefault = true) để test tính năng thả tim

-- ĐÃ CẬP NHẬT ĐƯỜNG DẪN FILE NHẠC THỰC TẾ THEO THƯ MỤC CỦA BẠN
INSERT INTO Songs (Title, Artist, Genre, Duration, FilePath, CoverPath) VALUES
	('23843807-dance-of-the-sugar-plum-fairy-pyotr-ilyich-tchaikovsky-201s-11937', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/23843807-dance-of-the-sugar-plum-fairy-pyotr-ilyich-tchaikovsky-201s-11937.mp3', ''),
	('clavier-music-beethoven-fur-elise-relaxing-classical-piano-268551', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/clavier-music-beethoven-fur-elise-relaxing-classical-piano-268551.mp3', ''),
	('clavier-music-waltz-in-a-minor-chopin-268549', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/clavier-music-waltz-in-a-minor-chopin-268549.mp3', ''),
	('freemusicforvideo-calm-soft-446641', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/freemusicforvideo-calm-soft-446641.mp3', ''),
	('happinessinmusic-music-free-calm-459744', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/happinessinmusic-music-free-calm-459744.mp3', ''),
	('music_for_video-ding-dong-merrily-on-high-christmas-piano-background-music-12204', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/music_for_video-ding-dong-merrily-on-high-christmas-piano-background-music-12204.mp3', ''),
	('music_for_video-go-tell-it-on-the-mountain-christmas-piano-background-music-12207', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/music_for_video-go-tell-it-on-the-mountain-christmas-piano-background-music-12207.mp3', ''),
	('music_for_video-sad-piano-background-music-for-videos-7573', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/music_for_video-sad-piano-background-music-for-videos-7573.mp3', ''),
	('music_for_video-silent-night-piano-version-christmas-background-music-12457', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/music_for_video-silent-night-piano-version-christmas-background-music-12457.mp3', ''),
	('saturn-3-music-mozart-lacrimosa-requiem-piano-version-411229', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/saturn-3-music-mozart-lacrimosa-requiem-piano-version-411229.mp3', ''),
	('tunetank-ambient-piano-music-349141', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/tunetank-ambient-piano-music-349141.mp3', ''),
	('cinematic-soul-upbeat-rock-music-bright-future-beat-511440', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/cinematic-soul-upbeat-rock-music-bright-future-beat-511440.mp3', ''),
	('ikoliks_aj-fun-pop-background-music-419356', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/ikoliks_aj-fun-pop-background-music-419356.mp3', ''),
	('jonasblakewood-pop-524132', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/jonasblakewood-pop-524132.mp3', ''),
	('kornevmusic-inspiring-summer-pop-background-528497', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/kornevmusic-inspiring-summer-pop-background-528497.mp3', ''),
	('musictown-girl-pop-attitude-120209', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/musictown-girl-pop-attitude-120209.mp3', ''),
	('paulyudin-uplifting-pop-153901', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/paulyudin-uplifting-pop-153901.mp3', ''),
	('sergepavkinmusic-fleeting-memory-156195', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/sergepavkinmusic-fleeting-memory-156195.mp3', ''),
	('sergepavkinmusic-girls-116579', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/sergepavkinmusic-girls-116579.mp3', ''),
	('sigmaeffect-upbeat-pop-fun-happy-background-463387', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/sigmaeffect-upbeat-pop-fun-happy-background-463387.mp3', ''),
	('vivaleum-upbeat-happy-indie-pop-186815', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/vivaleum-upbeat-happy-indie-pop-186815.mp3', ''),
	('amsleybeats-genesis-_-pop-rap-beat-265631', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/amsleybeats-genesis-_-pop-rap-beat-265631.mp3', ''),
	('chill-music-smeraldelion-a-world-inside-your-smile-535795', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/chill-music-smeraldelion-a-world-inside-your-smile-535795.mp3', ''),
	('chill-music-smeraldelion-loose-love-535807', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/chill-music-smeraldelion-loose-love-535807.mp3', ''),
	('chill-music-smeraldelion-love-dont-let-go-529408', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/chill-music-smeraldelion-love-dont-let-go-529408.mp3', ''),
	('chill-music-smeraldelion-midnight-skies-535777', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/chill-music-smeraldelion-midnight-skies-535777.mp3', ''),
	('chill-music-smeraldelion-neon-dusty-535792', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/chill-music-smeraldelion-neon-dusty-535792.mp3', ''),
	('chill-music-smeraldelion-the-clouds-535771', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/chill-music-smeraldelion-the-clouds-535771.mp3', ''),
	('chill-music-smeraldelion-you-and-i-alive-529442', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/chill-music-smeraldelion-you-and-i-alive-529442.mp3', ''),
	('kontraa-old-love-tibetan-pop-music-504934', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/kontraa-old-love-tibetan-pop-music-504934.mp3', ''),
	('moonpetalmedia-say-my-name-emotional-cinematic-pop-song-female-vocals-496663', 'Unknown Artist', 'rnb', 200, '/assets/Songs/rnb/moonpetalmedia-say-my-name-emotional-cinematic-pop-song-female-vocals-496663.mp3', '');
