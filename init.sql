-- Chạy dòng này ở db postgres
-- DROP DATABASE IF EXISTS music_streaming_db WITH (FORCE);

-- Xong mới chạy dòng này
-- CREATE DATABASE music_streaming_db;

-- Rồi chạy đống dưới này
-- 1. XÓA TOÀN BỘ CÁC BẢNG CŨ (NẾU CÓ) ĐỂ TRÁNH LỖI CONFLICT
DROP TABLE IF EXISTS "Playlist_Songs" CASCADE;
DROP TABLE IF EXISTS "Playlists" CASCADE;
DROP TABLE IF EXISTS "Songs" CASCADE;
DROP TABLE IF EXISTS "Users" CASCADE;

DROP TABLE IF EXISTS playlist_songs CASCADE;
DROP TABLE IF EXISTS playlists CASCADE;
DROP TABLE IF EXISTS songs CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 2. TẠO LẠI CÁC BẢNG THEO ĐÚNG THIẾT KẾ DTO REPORT 2

CREATE TABLE users (
    userid SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    createdat TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE songs (
    songid SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    duration INT NOT NULL,
    filepath TEXT NOT NULL,
    coverpath TEXT
);

CREATE TABLE playlists (
    playlistid SERIAL PRIMARY KEY,
    userid INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('Favourite', 'Waiting')),
    isdefault BOOLEAN DEFAULT FALSE,
    createdat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userid) REFERENCES users(userid) ON DELETE CASCADE
);

CREATE TABLE playlist_songs (
    playlistid INT NOT NULL,
    songid INT NOT NULL,
    orderindex INT NOT NULL,
    PRIMARY KEY (playlistid, songid),
    FOREIGN KEY (playlistid) REFERENCES playlists(playlistid) ON DELETE CASCADE,
    FOREIGN KEY (songid) REFERENCES songs(songid) ON DELETE CASCADE
);

-- 3. CHÈN DỮ LIỆU MẪU (DUMMY DATA)

INSERT INTO users (username, password) VALUES
('user_phuc', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'),
('user_an', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5d5a86aff3ca12020c923adc6ca6'),
('user_khoi', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5d5a86aff3ca12020c923adc6ca6');

-- Chèn Playlists mẫu
INSERT INTO playlists (userid, name, type, isdefault) VALUES
(2, 'Nhạc Trẻ Sôi Động', 'Favourite', FALSE),
(2, 'Danh Sách Chờ Tạm Thời', 'Waiting', FALSE),
(3, 'Chill Cuối Tuần', 'Favourite', FALSE),
(2, 'Liked Songs', 'Favourite', TRUE);

-- ĐÃ CẬP NHẬT ĐƯỜNG DẪN FILE NHẠC THỰC TẾ THEO THƯ MỤC CỦA BẠN
INSERT INTO Songs (Title, Artist, Genre, Duration, FilePath, CoverPath) VALUES
	-- BALLAD (11 bài)
	('Billie Bossa Nova', 'Billie Eilish', 'ballad', 211, '/assets/Songs/ballad/Billie Bossa Nova.mp3', NULL),
	('Dancing With Your Ghost', 'Sasha Sloan', 'ballad', 214, '/assets/Songs/ballad/Dancing With Your Ghost.mp3', NULL),
	('Easy On Me', 'Adele', 'ballad', 239, '/assets/Songs/ballad/Easy On Me.mp3', NULL),
	('happier', 'Olivia Rodrigo', 'ballad', 191, '/assets/Songs/ballad/happier.mp3', NULL),
	('Happy For You', 'Carly Pearce', 'ballad', 241, '/assets/Songs/ballad/Happy For You.mp3', NULL),
	('How To Cry', 'Fletcher', 'ballad', 175, '/assets/Songs/ballad/How To Cry.mp3', NULL),
	('No Promises', 'Cheat Codes ft. Demi Lovato', 'ballad', 223, '/assets/Songs/ballad/No Promises.mp3', NULL),
	('pov', 'Ariana Grande', 'ballad', 217, '/assets/Songs/ballad/pov.mp3', NULL),
	('Someone Like You', 'Adele', 'ballad', 285, '/assets/Songs/ballad/Someone Like You.mp3', NULL),
	('Stay With Me', 'Sam Smith', 'ballad', 172, '/assets/Songs/ballad/Stay With Me.mp3', NULL),
	('Stuck With U', 'Ariana Grande & Justin Bieber', 'ballad', 211, '/assets/Songs/ballad/Stuck With U.mp3', NULL),
	-- BARBIE PLAYLIST (10 bài)
	('Barbie And The Diamond Castle: We''Re Gonna Find It', 'Barbie', 'barbie playlist', 196, '/assets/Songs/barbie playlist/Barbie And The Diamond Castle_ We''Re Gonna Find It.mp3', NULL),
	('Barbie And The Three Musketeers: All For One', 'Barbie', 'barbie playlist', 203, '/assets/Songs/barbie playlist/Barbie And The Three Musketeers_ All For One.mp3', NULL),
	('Barbie In A Mermaid Tale 2: Do The Mermaid', 'Barbie', 'barbie playlist', 167, '/assets/Songs/barbie playlist/Barbie In A Mermaid Tale 2_ Do The Mermaid.mp3', NULL),
	('Be A Friend', 'Barbie', 'barbie playlist', 168, '/assets/Songs/barbie playlist/Be A Friend.mp3', NULL),
	('Life Is A Fairytale', 'Barbie', 'barbie playlist', 170, '/assets/Songs/barbie playlist/Life Is A Fairytale.mp3', NULL),
	('Look How High We Can Fly', 'Barbie', 'barbie playlist', 198, '/assets/Songs/barbie playlist/Look How High We Can Fly.mp3', NULL),
	('On Top Of The World', 'Barbie', 'barbie playlist', 243, '/assets/Songs/barbie playlist/On Top Of The World.mp3', NULL),
	('Only A Breath Away', 'Barbie', 'barbie playlist', 200, '/assets/Songs/barbie playlist/Only A Breath Away.mp3', NULL),
	('To Be A Princess / To Be A Popstar', 'Barbie', 'barbie playlist', 209, '/assets/Songs/barbie playlist/To Be A Princess _ To Be A Popstar.mp3', NULL),
	('You Can Tell She''S A Princess', 'Barbie', 'barbie playlist', 179, '/assets/Songs/barbie playlist/You Can Tell She''S A Princess.mp3', NULL),
	-- POP (11 bài)
	('Baby', 'Justin Bieber', 'pop', 214, '/assets/Songs/pop/Baby.mp3', NULL),
	('Beauty And A Beat', 'Justin Bieber', 'pop', 227, '/assets/Songs/pop/Beauty And A Beat.mp3', NULL),
	('Die With A Smile', 'Lady Gaga & Bruno Mars', 'pop', 251, '/assets/Songs/pop/Die With A Smile.mp3', NULL),
	('Faded', 'Alan Walker', 'pop', 212, '/assets/Songs/pop/Faded.mp3', NULL),
	('Music Town Pop Girl', 'Unknown Artist', 'pop', 300, '/assets/Songs/pop/Music Town Pop Girl.mp3', NULL),
	('One Of The Girls', 'The Weeknd, Jennie & Lily-Rose Depp', 'pop', 242, '/assets/Songs/pop/One Of The Girls.mp3', NULL),
	('Shape Of You', 'Ed Sheeran', 'pop', 234, '/assets/Songs/pop/Shape Of You.mp3', NULL),
	('STAY', 'Justin Bieber & The Kid LAROI', 'pop', 141, '/assets/Songs/pop/STAY (E).mp3', NULL),
	('That Girl', 'Olly Murs', 'pop', 168, '/assets/Songs/pop/That Girl.mp3', NULL),
	('The Fate of Ophelia', 'Taylor Swift', 'pop', 226, '/assets/Songs/pop/The Fate of Ophelia.mp3', NULL),
	('We Don''t Talk Anymore', 'Charlie Puth ft. Selena Gomez', 'pop', 218, '/assets/Songs/pop/We Don''t Talk Anymore.mp3', NULL);

