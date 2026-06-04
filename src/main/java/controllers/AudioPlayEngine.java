/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import lib.HistoryStack;
import lib.IndexedDoublyLinkedList;
import lib.IndexedDoublyLinkedList;
import lib.Node;
import models.Song;
import models.Song;
import DAO.songDAO;
import lib.DynamicArrayList;
import java.util.Random;
import lib.DynamicArrayList;

public class AudioPlayEngine {

    private Node currentTrackPointer;              // Con trỏ bài hát đang phát hiện tại
    private IndexedDoublyLinkedList waitingList;   // Hàng chờ in-memory (DLL + HashMap) 
    private HistoryStack playbackHistory;          // Ngăn xếp lưu lịch sử (Stack LIFO) 
    private boolean isRepeatAllEnabled = false;    // Cờ thiết lập chế độ lặp 
    private boolean repeatOne = false;

    public AudioPlayEngine(IndexedDoublyLinkedList waitingList) {
        this.waitingList = waitingList;
        this.currentTrackPointer = waitingList.getHead(); // Gắn điểm bắt đầu bằng Head của DLL 
        this.playbackHistory = new HistoryStack();
    }

    public void setRepeatOne(boolean status) {
        this.repeatOne = status;
    }
    
//    nextTrack mới
    
    
//    public void nextTrack() {
//
//    if (currentTrackPointer == null) {
//        return;
//    }
//
//    // ===== REPEAT ONE =====
//    if (repeatOne) {
//        System.out.println("Repeat One đang bật. Phát lại: "
//                + currentTrackPointer.data.getTitle());
//        return;
//    }
//
//    // ===== LƯU LỊCH SỬ =====
//    playbackHistory.push(currentTrackPointer.data);
//
//    // ===== DI CHUYỂN NEXT =====
//    if (currentTrackPointer.next != null) {
//
//        currentTrackPointer = currentTrackPointer.next;
//
//        System.out.println("Đang phát bài tiếp theo: "
//                + currentTrackPointer.data.getTitle());
//
//    } else {
//
//        // ===== REPEAT ALL =====
//        if (isRepeatAllEnabled) {
//
//            currentTrackPointer = waitingList.getHead();
//
//            System.out.println("Vòng lặp kích hoạt! Quay về bài đầu tiên: "
//                    + currentTrackPointer.data.getTitle());
//
//        } else {
//
//            System.out.println("Đã hết danh sách chờ.");
//
//            autoAppendSongs();
//
//            // Nếu append thành công thì phát tiếp
//            if (currentTrackPointer.next != null) {
//
//                currentTrackPointer = currentTrackPointer.next;
//
//                System.out.println("Đang phát bài mới được tải thêm: "
//                        + currentTrackPointer.data.getTitle());
//            }
//        }
//    }
//}

    /**
     * Thuật toán: Phát bài tiếp theo (Next Track Control) Kịch bản: Di chuyển
     * con trỏ liên kết xuôi, lưu vết lịch sử vào Stack
     */
    public void nextTrack() {
        if (currentTrackPointer == null) {
            return;
        }

        // Đẩy bài hát hiện tại vào ngăn xếp lịch sử phát 
        playbackHistory.push(currentTrackPointer.data);

        if (currentTrackPointer.next != null) {
            currentTrackPointer = currentTrackPointer.next; // Di chuyển sang nút tiếp theo trong O(1) 
            System.out.println("Đang phát bài tiếp theo: " + currentTrackPointer.data.getTitle());
        } else {
            // Xử lý logic lặp toàn bộ hoặc kích hoạt mở rộng danh sách tự động từ database
            if (isRepeatAllEnabled) {
                currentTrackPointer = waitingList.getHead(); // Quay ngược về đầu playlist 
                System.out.println("Vòng lặp kích hoạt! Quay về bài đầu tiên: " + currentTrackPointer.data.getTitle());
            } else {
                System.out.println("Đã hết danh sách chờ. Kích hoạt truy vấn tự động thêm 10 bài cùng thể loại...");
                // Thực hiện phương thức Auto-Queue Expansion tại đây... 
            }
        }
    }

    /**
     * Thuật toán: Quay lại bài trước (Previous Track Control) Kịch bản: Rút
     * trích bài hát gần nhất từ Stack lịch sử thay vì đi ngược con trỏ DLL để
     * đảm bảo chính xác trình tự nghe thực tế kể cả khi xáo trộn (Shuffle)
     */
    public void previousTrack() {
        if (playbackHistory.isEmpty()) {
            System.out.println("Không có lịch sử bài hát trước đó.");
            return;
        }

        // Lấy bài hát từ đỉnh Stack 
        Song previousSong = playbackHistory.pop();
        System.out.println("Trở lại bài hát trong lịch sử: " + previousSong.getTitle());

        // Cập nhật lại con trỏ thực tế trong danh sách chờ (nếu cần thiết)
        // (Trong thực tế hệ thống, có thể thiết lập cấu trúc phát theo luồng lịch sử riêng)
    }

    public void setRepeatAll(boolean status) {
        this.isRepeatAllEnabled = status;
    }

    private void autoAppendSongs() {

        if (currentTrackPointer == null) {
            return;
        }

        String genre = currentTrackPointer.data.getGenre();

        songDAO dao = new songDAO();

        DynamicArrayList songs = dao.getSongsByGenre(genre, 10);

        for (int i = 0; i < songs.size(); i++) {

            Song song = songs.get(i);

            // tránh duplicate
            if (!waitingList.contains(song.getSongId())) {

                waitingList.append(song);

                System.out.println("Đã thêm bài: "
                        + song.getTitle());
            }
        }
    }

    public void shuffleWaitingList() {

        if (currentTrackPointer == null
                || currentTrackPointer.next == null) {

            System.out.println("Không đủ bài hát để shuffle.");
            return;
        }

        // ===== EXPORT FUTURE SONGS =====
        DynamicArrayList songs = new DynamicArrayList();

        Node temp = currentTrackPointer.next;

        while (temp != null) {

            songs.add(temp.data);

            temp = temp.next;
        }

        // ===== FISHER-YATES SHUFFLE =====
        Random random = new Random();

        for (int i = songs.size() - 1; i > 0; i--) {

            int j = random.nextInt(i + 1);

            Song tempSong = songs.get(i);

            songs.set(i, songs.get(j));

            songs.set(j, tempSong);
        }

        // ===== GHI DỮ LIỆU ĐÃ SHUFFLE NGƯỢC LẠI DLL =====
        temp = currentTrackPointer.next;

        int index = 0;

        while (temp != null) {

            temp.data = songs.get(index);

            temp = temp.next;

            index++;
        }

        System.out.println("Đã xáo trộn danh sách chờ.");
    }
}
