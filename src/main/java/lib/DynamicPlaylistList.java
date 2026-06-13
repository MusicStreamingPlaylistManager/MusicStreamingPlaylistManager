package lib;

import models.Playlist;

public class DynamicPlaylistList {
    private Playlist[] array;
    private int capacity;
    private int currentSize;

    public DynamicPlaylistList() {
        this.capacity = 10;
        this.array = new Playlist[capacity];
        this.currentSize = 0;
    }

    public void add(Playlist playlist) {
        if (currentSize == capacity) {
            capacity = capacity * 2;
            Playlist[] newArray = new Playlist[capacity];
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = playlist;
        currentSize++;
    }

    public Playlist get(int index) {
        if (index < 0 || index >= currentSize) {
            throw new IndexOutOfBoundsException("Vượt quá phạm vi mảng.");
        }
        return array[index];
    }

    public int size() {
        return this.currentSize;
    }
}
