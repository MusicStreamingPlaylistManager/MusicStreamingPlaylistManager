package controllers;

import java.util.Random;
import lib.DynamicArrayList;
import lib.HistoryStack;
import lib.IndexedDoublyLinkedList;
import lib.Node;
import models.Song;
import models.SongDAO;

public class AudioPlayEngine {
    private Node currentTrackPointer;
    private IndexedDoublyLinkedList waitingList;
    private HistoryStack playbackHistory;
    private boolean isRepeatAllEnabled = false;
    private boolean repeatOne = false;
    private final SongDAO songDAO = new SongDAO();

    public AudioPlayEngine(IndexedDoublyLinkedList waitingList) {
        this.waitingList = waitingList;
        this.currentTrackPointer = waitingList.getHead();
        this.playbackHistory = new HistoryStack();
    }

    public Song getCurrentSong() {
        return currentTrackPointer != null ? currentTrackPointer.data : null;
    }

    public IndexedDoublyLinkedList getWaitingList() {
        return waitingList;
    }

    public HistoryStack getPlaybackHistory() {
        return playbackHistory;
    }

    // Cho phép giữ lại lịch sử nghe khi tạo engine mới (phát bài/queue khác trong cùng phiên).
    public void setPlaybackHistory(HistoryStack history) {
        if (history != null) {
            this.playbackHistory = history;
        }
    }

    public void playFromSong(Song song) {
        if (song == null) return;
        Node node = waitingList.getNodeById(song.getSongId());
        if (node != null) {
            currentTrackPointer = node;
        }
    }

    /**
     * Dời con trỏ phát tới một bài đã có sẵn trong hàng chờ (KHÔNG dựng lại hàng chờ).
     * Dùng khi người dùng bấm một bài trong Wait List.
     */
    public Song jumpTo(int songId) {
        Node node = waitingList.getNodeById(songId);
        if (node == null) return null;
        currentTrackPointer = node;
        return node.data;
    }

    /**
     * Xóa một bài khỏi hàng chờ và GIỮ con trỏ phát hợp lệ.
     * Nếu xóa đúng bài đang phát thì dời con trỏ sang bài kế tiếp (hoặc trước đó,
     * hoặc null nếu hàng chờ rỗng) để Next/Prev không trỏ vào node đã tách rời.
     */
    public boolean removeSong(int songId) {
        Node node = waitingList.getNodeById(songId);
        if (node == null) return false;

        if (currentTrackPointer == node) {
            currentTrackPointer = (node.next != null) ? node.next : node.prev;
        }
        return waitingList.removeById(songId);
    }

    public Song nextTrack() {
        // Con trỏ null nhưng hàng chờ còn bài (vd: vừa xóa bài đầu đang phát) → phát từ đầu.
        if (currentTrackPointer == null) {
            currentTrackPointer = waitingList.getHead();
            return currentTrackPointer != null ? currentTrackPointer.data : null;
        }

        if (repeatOne) {
            return currentTrackPointer.data;
        }

        playbackHistory.push(currentTrackPointer.data);

        if (currentTrackPointer.next != null) {
            currentTrackPointer = currentTrackPointer.next;
            return currentTrackPointer.data;
        }

        if (isRepeatAllEnabled) {
            currentTrackPointer = waitingList.getHead();
            return currentTrackPointer != null ? currentTrackPointer.data : null;
        }

        autoAppendSongs();
        if (currentTrackPointer.next != null) {
            currentTrackPointer = currentTrackPointer.next;
            return currentTrackPointer.data;
        }

        return null;
    }

    public Song previousTrack() {
        if (playbackHistory.isEmpty()) {
            return null;
        }

        Song previousSong = playbackHistory.pop();
        Node node = waitingList.getNodeById(previousSong.getSongId());
        if (node != null) {
            currentTrackPointer = node;
        }
        return previousSong;
    }

    public void setRepeatAll(boolean status) {
        this.isRepeatAllEnabled = status;
        if (status) {
            this.repeatOne = false;
        }
    }

    public void setRepeatOne(boolean status) {
        this.repeatOne = status;
        if (status) {
            this.isRepeatAllEnabled = false;
        }
    }

    public boolean isRepeatAllEnabled() {
        return isRepeatAllEnabled;
    }

    public boolean isRepeatOne() {
        return repeatOne;
    }

    public void shuffleUpcoming() {
        if (currentTrackPointer == null || currentTrackPointer.next == null) {
            return;
        }

        DynamicArrayList songs = new DynamicArrayList();
        Node temp = currentTrackPointer.next;
        while (temp != null) {
            songs.add(temp.data);
            temp = temp.next;
        }

        Random random = new Random();
        for (int i = songs.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Song tempSong = songs.get(i);
            songs.set(i, songs.get(j));
            songs.set(j, tempSong);
        }

        temp = currentTrackPointer.next;
        int index = 0;
        while (temp != null) {
            temp.data = songs.get(index);
            temp = temp.next;
            index++;
        }

        // Quan trọng: vì đã ghi đè data của các node, phải dựng lại HashMap id→Node,
        // nếu không jumpTo/removeById/contains sẽ trỏ sai node sau khi shuffle.
        waitingList.rebuildIndex();
    }

    public void autoAppendSongs() {
        if (currentTrackPointer == null) return;

        String genre = currentTrackPointer.data.getGenre();
        try {
            DynamicArrayList songs = songDAO.getSongsByGenre(genre, 10);
            for (int i = 0; i < songs.size(); i++) {
                Song song = songs.get(i);
                if (!waitingList.contains(song.getSongId())) {
                    waitingList.append(song);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Khi phát một bài lẻ (trang chủ / search / thể loại / một bài trong playlist),
     * hàng chờ CHỈ gồm bài đó. Khi phát hết, nextTrack() sẽ tự nạp thêm qua autoAppendSongs()
     * — đúng như mô tả trong report. songDAO giữ lại cho tương thích chữ ký gọi.
     */
    public static IndexedDoublyLinkedList buildQuickPlayQueue(Song selected, SongDAO songDAO) throws Exception {
        IndexedDoublyLinkedList list = new IndexedDoublyLinkedList();
        list.append(selected);
        return list;
    }
}
