package utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public class SQLGenerator {
    
    public static void main(String[] args) {
        // 1. Trỏ đến thư mục Songs trong source code của bạn
        String songsDirPath = "src/main/webapp/assets/Songs";
        File songsDir = new File(songsDirPath);

        if (!songsDir.exists() || !songsDir.isDirectory()) {
            System.out.println("Không tìm thấy thư mục: " + songsDir.getAbsolutePath());
            System.out.println("Hãy chắc chắn bạn đã kéo thư mục Songs vào web/assets/");
            return;
        }

        System.out.println("-- ======== COPY ĐOẠN DƯỚI ĐÂY DÁN VÀO FILE SQL CỦA BẠN ======== --\n");
        System.out.println("INSERT INTO Songs (Title, Artist, Genre, Duration, FilePath, CoverPath) VALUES");

        // Đã sửa lại cú pháp quét thư mục cho tương thích Java 7
        File[] genreFolders = songsDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        boolean isFirst = true;

        if (genreFolders != null) {
            for (File genreFolder : genreFolders) {
                String genre = genreFolder.getName(); // Lấy tên thư mục làm Thể loại 
                
                // Đã sửa lại cú pháp lọc đuôi .mp3 cho tương thích Java 7
                File[] songFiles = genreFolder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".mp3");
                    }
                });

                if (songFiles == null) continue;

                for (File songFile : songFiles) {
                    String fileName = songFile.getName();
                    
                    String title = fileName.substring(0, fileName.lastIndexOf('.')).replace("'", "''"); 
                    
                    String artist = "Unknown Artist"; 
                    int duration = 200; // Fix cứng 200 giây
                    
                    String filePath = "/assets/Songs/" + genre + "/" + fileName;

                    if (!isFirst) {
                        System.out.println(",");
                    }
                    System.out.printf("\t('%s', '%s', '%s', %d, '%s', '')", 
                            title, artist, genre, duration, filePath.replace("'", "''"));
                    isFirst = false;
                }
            }
        }
        System.out.println(";");
        System.out.println("\n-- ======== KẾT THÚC ======== --");
    }
}