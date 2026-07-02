/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lib;

import models.Song;

public class HistoryStack {
    private Node top;
    private int size;
    private final int MAX_SIZE = 30; // Giới hạn nghiệp vụ 

    public HistoryStack() {
        this.top = null;
        this.size = 0;
    }

    public void push(Song song) {
        Node newNode = new Node(song);
        if (top == null) {
            top = newNode;
        } else {
            newNode.next = top;
            top = newNode;
        }
        size++;

        if (size > MAX_SIZE) {
            Node current = top;
            while (current.next != null && current.next.next != null) {
                current = current.next;
            }
            current.next = null; // Cắt bỏ bài hát cũ nhất ở đáy stack
            size = MAX_SIZE;
        }
    }

    public Song pop() {
        if (top == null) return null;
        Song poppedData = top.data;
        top = top.next;
        size--;
        return poppedData;
    }

    public Song peek() {
        return (top != null) ? top.data : null;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public DynamicArrayList toList() {
        DynamicArrayList songs = new DynamicArrayList();
        Node current = top;
        while (current != null) {
            songs.add(current.data);
            current = current.next;
        }
        return songs;
    }
}
