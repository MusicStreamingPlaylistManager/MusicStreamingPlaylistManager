/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author LeeTyy21
 */
public class AudioPlayEngine {
    private Node currentTrackPointer;              // Con trỏ bài hát đang phát hiện tại
    private IndexedDoublyLinkedList waitingList;   // Hàng chờ in-memory (DLL + HashMap) 
    private HistoryStack playbackHistory;          // Ngăn xếp lưu lịch sử (Stack LIFO) 
    private boolean isRepeatAllEnabled = false;    // Cờ thiết lập chế độ lặp 

    public AudioPlayEngine(IndexedDoublyLinkedList waitingList) {
        this.waitingList = waitingList;
        this.currentTrackPointer = waitingList.getHead(); // Gắn điểm bắt đầu bằng Head của DLL 
        this.playbackHistory = new HistoryStack();
    }

    /**
     * Thuật toán: Phát bài tiếp theo (Next Track Control)
     * Kịch bản: Di chuyển con trỏ liên kết xuôi, lưu vết lịch sử vào Stack
     */
    public void nextTrack() {
        if (currentTrackPointer == null) return;

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
     * Thuật toán: Quay lại bài trước (Previous Track Control)
     * Kịch bản: Rút trích bài hát gần nhất từ Stack lịch sử thay vì đi ngược con trỏ DLL
     * để đảm bảo chính xác trình tự nghe thực tế kể cả khi xáo trộn (Shuffle)
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
}
