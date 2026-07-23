package lib;

import models.Song;

/**
 * Danh sách liên kết đôi NGUYÊN BẢN (không dùng HashMap).
 *
 * Chỉ giữ hai con trỏ đầu/cuối. Mọi thao tác tra cứu theo songId đều DUYỆT
 * TUYẾN TÍNH (Linear Search) từ head đến tail — đúng yêu cầu refactor: bỏ hẳn
 * việc lập chỉ mục địa chỉ node bằng bảng băm.
 */
public class DoublyLinkedList {
    private Node head;
    private Node tail;

    public DoublyLinkedList() {
        this.head = null;
        this.tail = null;
    }

    /** Thêm một bài vào cuối danh sách (bỏ qua nếu đã tồn tại). */
    public void append(Song song) {
        if (contains(song.getSongId())) return;

        Node newNode = new Node(song);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    /** Duyệt tuyến tính tìm node theo songId. Trả về null nếu không có. */
    public Node getNodeById(int songId) {
        Node current = head;
        while (current != null) {
            if (current.data.getSongId() == songId) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    /** Kiểm tra tồn tại bằng duyệt tuyến tính. */
    public boolean contains(int songId) {
        return getNodeById(songId) != null;
    }

    /** Xóa node theo songId (duyệt tuyến tính để tìm), nối lại hàng xóm. */
    public boolean removeById(int songId) {
        Node target = getNodeById(songId);
        if (target == null) return false;

        if (target == head) {
            head = target.next;
            if (head != null) head.prev = null;
        } else {
            target.prev.next = target.next;
        }

        if (target == tail) {
            tail = target.prev;
            if (tail != null) tail.next = null;
        } else {
            target.next.prev = target.prev;
        }
        return true;
    }

    /** Chuyển bài songIdToMove tới ngay sau targetSongId (kéo–thả sắp xếp lại). */
    public boolean moveSongAfter(int songIdToMove, int targetSongId) {
        Node nodeToMove = getNodeById(songIdToMove);
        Node targetNode = getNodeById(targetSongId);

        if (nodeToMove == null || targetNode == null || songIdToMove == targetSongId) {
            return false;
        }

        // Bước 1: gỡ nodeToMove khỏi vị trí cũ
        if (nodeToMove == head) {
            head = nodeToMove.next;
            if (head != null) head.prev = null;
        } else {
            nodeToMove.prev.next = nodeToMove.next;
        }
        if (nodeToMove == tail) {
            tail = nodeToMove.prev;
            if (tail != null) tail.next = null;
        } else {
            nodeToMove.next.prev = nodeToMove.prev;
        }

        // Bước 2: chèn nodeToMove vào ngay sau targetNode
        Node afterTarget = targetNode.next;
        targetNode.next = nodeToMove;
        nodeToMove.prev = targetNode;
        if (afterTarget == null) {
            tail = nodeToMove;
            nodeToMove.next = null;
        } else {
            nodeToMove.next = afterTarget;
            afterTarget.prev = nodeToMove;
        }
        return true;
    }

    /** Chuyển bài songIdToMove tới ngay TRƯỚC targetSongId (thả lên phía trên bài đích). */
    public boolean moveSongBefore(int songIdToMove, int targetSongId) {
        Node nodeToMove = getNodeById(songIdToMove);
        Node targetNode = getNodeById(targetSongId);

        if (nodeToMove == null || targetNode == null || songIdToMove == targetSongId) {
            return false;
        }

        // Bước 1: gỡ nodeToMove khỏi vị trí cũ
        if (nodeToMove == head) {
            head = nodeToMove.next;
            if (head != null) head.prev = null;
        } else {
            nodeToMove.prev.next = nodeToMove.next;
        }
        if (nodeToMove == tail) {
            tail = nodeToMove.prev;
            if (tail != null) tail.next = null;
        } else {
            nodeToMove.next.prev = nodeToMove.prev;
        }

        // Bước 2: chèn nodeToMove vào ngay TRƯỚC targetNode
        // (đọc beforeTarget SAU khi đã gỡ, vì targetNode.prev có thể vừa đổi)
        Node beforeTarget = targetNode.prev;
        nodeToMove.next = targetNode;
        targetNode.prev = nodeToMove;
        if (beforeTarget == null) {
            head = nodeToMove;
            nodeToMove.prev = null;
        } else {
            beforeTarget.next = nodeToMove;
            nodeToMove.prev = beforeTarget;
        }
        return true;
    }

    public Node getHead() { return this.head; }
    public Node getTail() { return this.tail; }

    /** Trả về danh sách bài hát (theo thứ tự) để phía Servlet đóng gói JSON. */
    public DynamicArrayList toSongList() {
        DynamicArrayList songs = new DynamicArrayList();
        Node current = head;
        while (current != null) {
            songs.add(current.data);
            current = current.next;
        }
        return songs;
    }
}
