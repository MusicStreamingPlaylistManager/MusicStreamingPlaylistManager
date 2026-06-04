/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

public class DynamicArrayList {
    private Song[] array;
    private int capacity;
    private int currentSize;

    public DynamicArrayList() {
        this.capacity = 10;
        this.array = new Song[capacity];
        this.currentSize = 0;
    }

    public void add(Song song) {
        if (currentSize == capacity) {
            capacity = capacity * 2;
            Song[] newArray = new Song[capacity];
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = song;
        currentSize++;
    }

    public Song get(int index) {
        if (index < 0 || index >= currentSize) {
            throw new IndexOutOfBoundsException("Vượt quá phạm vi mảng.");
        }
        return array[index];
    }

    public int size() {
        return this.currentSize;
    }
}
