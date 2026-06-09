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

    public void playFromSong(Song song) {
        if (song == null) return;
        Node node = waitingList.getNodeById(song.getSongId());
        if (node != null) {
            currentTrackPointer = node;
        }
    }

    public Song nextTrack() {
        if (currentTrackPointer == null) return null;

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

    public static IndexedDoublyLinkedList buildQuickPlayQueue(Song selected, SongDAO songDAO) throws Exception {
        IndexedDoublyLinkedList list = new IndexedDoublyLinkedList();
        list.append(selected);

        DynamicArrayList similar = songDAO.getSongsByGenre(selected.getGenre(), 11);
        for (int i = 0; i < similar.size() && list.toSongList().size() < 11; i++) {
            Song song = similar.get(i);
            if (!list.contains(song.getSongId())) {
                list.append(song);
            }
        }
        return list;
    }
}
