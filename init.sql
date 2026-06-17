-- Chạy dòng này ở db postgres
-- DROP DATABASE IF EXISTS music_streaming_db WITH (FORCE);

-- Xong mới chạy dòng này
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
	('Billie Bossa Nova', 'Billie Eilish', 'ballad', 200, '/assets/Songs/ballad/Billie Bossa Nova.mp3', ''),
	('Dancing With Your Ghost', 'Sasha Sloan', 'ballad', 200, '/assets/Songs/ballad/Dancing With Your Ghost.mp3', ''),
	('Easy On Me', 'Adele', 'ballad', 200, '/assets/Songs/ballad/Easy On Me.mp3', ''),
	('happier', 'Olivia Rodrigo', 'ballad', 200, '/assets/Songs/ballad/happier.mp3', ''),
	('Happy For You', 'Carly Pearce', 'ballad', 200, '/assets/Songs/ballad/Happy For You.mp3', ''),
	('How To Cry', 'Fletcher', 'ballad', 200, '/assets/Songs/ballad/How To Cry.mp3', ''),
	('No Promises', 'Cheat Codes ft. Demi Lovato', 'ballad', 200, '/assets/Songs/ballad/No Promises.mp3', ''),
	('pov', 'Ariana Grande', 'ballad', 200, '/assets/Songs/ballad/pov.mp3', ''),
	('Someone Like You', 'Adele', 'ballad', 200, '/assets/Songs/ballad/Someone Like You.mp3', ''),
	('Stay With Me', 'Sam Smith', 'ballad', 200, '/assets/Songs/ballad/Stay With Me.mp3', ''),
	('Stuck With U', 'Ariana Grande & Justin Bieber', 'ballad', 200, '/assets/Songs/ballad/Stuck With U.mp3', ''),
	('Barbie And The Diamond Castle_ We''Re Gonna Find It', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/Barbie And The Diamond Castle_ We''Re Gonna Find It.mp3', ''),
	('Barbie And The Three Musketeers_ All For One', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/Barbie And The Three Musketeers_ All For One.mp3', ''),
	('Barbie In A Mermaid Tale 2_ Do The Mermaid', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/Barbie In A Mermaid Tale 2_ Do The Mermaid.mp3', ''),
	('Be A Friend', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/Be A Friend.mp3', ''),
	('Life Is A Fairytale', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/Life Is A Fairytale.mp3', ''),
	('Look How High We Can Fly', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/Look How High We Can Fly.mp3', ''),
	('On Top Of The World', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/On Top Of The World.mp3', ''),
	('Only A Breath Away', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/Only A Breath Away.mp3', ''),
	('To Be A Princess _ To Be A Popstar', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/To Be A Princess _ To Be A Popstar.mp3', ''),
	('You Can Tell She''S A Princess', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/You Can Tell She''S A Princess.mp3', ''),
	('dance of the sugar plum fairy', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/23843807-dance-of-the-sugar-plum-fairy-pyotr-ilyich-tchaikovsky-201s-11937.mp3', ''),
	('fur elise', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/clavier-music-beethoven-fur-elise-relaxing-classical-piano-268551.mp3', ''),
	('waltz in a minor chopin', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/clavier-music-waltz-in-a-minor-chopin-268549.mp3', ''),
	('free music for video', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/freemusicforvideo-calm-soft-446641.mp3', ''),
	('happiness in music', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/happinessinmusic-music-free-calm-459744.mp3', ''),
	('ding dong merrily', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/music_for_video-ding-dong-merrily-on-high-christmas-piano-background-music-12204.mp3', ''),
	('go tell it on the mountain christmas', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/music_for_video-go-tell-it-on-the-mountain-christmas-piano-background-music-12207.mp3', ''),
	('sad piano', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/music_for_video-sad-piano-background-music-for-videos-7573.mp3', ''),
	('silent night', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/music_for_video-silent-night-piano-version-christmas-background-music-12457.mp3', ''),
	('saturn 3 mozart', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/saturn-3-music-mozart-lacrimosa-requiem-piano-version-411229.mp3', ''),
	('tunetank ambient', 'Unknown Artist', 'piano', 200, '/assets/Songs/piano/tunetank-ambient-piano-music-349141.mp3', ''),
	('Baby', 'Justin Bieber', 'pop', 200, '/assets/Songs/pop/Baby.mp3', ''),
	('Beauty And A Beat', 'Justin Bieber', 'pop', 200, '/assets/Songs/pop/Beauty And A Beat.mp3', ''),
	('Die With A Smile', 'Lady Gaga & Bruno Mars', 'pop', 200, '/assets/Songs/pop/Die With A Smile.mp3', ''),
	('Faded', 'Alan Walker', 'pop', 200, '/assets/Songs/pop/Faded.mp3', ''),
	('Music Town Pop Girl', 'Unknown Artist', 'pop', 200, '/assets/Songs/pop/Music Town Pop Girl.mp3', ''),
	('One Of The Girls', 'The Weeknd, Jennie & Lily-Rose Depp', 'pop', 200, '/assets/Songs/pop/One Of The Girls.mp3', ''),
	('Shape Of You', 'Ed Sheeran', 'pop', 200, '/assets/Songs/pop/Shape Of You.mp3', ''),
	('STAY (E)', 'Justin Bieber & The Kid LAROI', 'pop', 200, '/assets/Songs/pop/STAY (E).mp3', ''),
	('That Girl', 'Olly Murs', 'pop', 200, '/assets/Songs/pop/That Girl.mp3', ''),
	('The Fate of Ophelia', 'Taylor Swift', 'pop', 200, '/assets/Songs/pop/The Fate of Ophelia.mp3', ''),
	('We Don''t Talk Anymore', 'Charlie Puth ft. Selena Gomez', 'pop', 200, '/assets/Songs/pop/We Don''t Talk Anymore.mp3', '');

