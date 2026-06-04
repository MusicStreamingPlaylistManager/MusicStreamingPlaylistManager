/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lib;

import java.util.HashMap;
import java.util.Map;
import models.Song;

public class IndexedDoublyLinkedList {

    private Node head;
    private Node tail;
    private Map<Integer, Node> songIndex; // Cấu trúc băm quản lý địa chỉ ô nhớ 

    public IndexedDoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.songIndex = new HashMap<>();
    }

    public void append(Song song) {
        if (songIndex.containsKey(song.getSongId())) {
            return;
        }

        Node newNode = new Node(song);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        songIndex.put(song.getSongId(), newNode); // Ánh xạ SongID sang Pointer Node 
    }

    public boolean removeById(int songId) {
        Node targetNode = songIndex.get(songId); // Tra cứu Pointer trong O(1) 
        if (targetNode == null) {
            return false;
        }

        if (targetNode == head) {
            head = targetNode.next;
            if (head != null) {
                head.prev = null;
            }
        } else {
            targetNode.prev.next = targetNode.next;
        }

        if (targetNode == tail) {
            tail = targetNode.prev;
            if (tail != null) {
                tail.next = null;
            }
        } else {
            targetNode.next.prev = targetNode.prev;
        }

        songIndex.remove(songId);
        return true;
    }

    public boolean moveSongAfter(int songIdToMove, int targetSongId) {
        Node nodeToMove = songIndex.get(songIdToMove);
        Node targetNode = songIndex.get(targetSongId);

        if (nodeToMove == null || targetNode == null || songIdToMove == targetSongId) {
            return false;
        }

        // Bước 1: Hủy liên kết cũ
        if (nodeToMove == head) {
            head = nodeToMove.next;
            if (head != null) {
                head.prev = null;
            }
        } else {
            nodeToMove.prev.next = nodeToMove.next;
        }

        if (nodeToMove == tail) {
            tail = nodeToMove.prev;
            if (tail != null) {
                tail.next = null;
            }
        } else {
            nodeToMove.next.prev = nodeToMove.prev;
        }

        // Bước 2: Tái kết nối vào vị trí mới đứng sau targetNode
        Node nextNodeOfTarget = targetNode.next;
        targetNode.next = nodeToMove;
        nodeToMove.prev = targetNode;

        if (nextNodeOfTarget == null) {
            tail = nodeToMove;
            nodeToMove.next = null;
        } else {
            nodeToMove.next = nextNodeOfTarget;
            nextNodeOfTarget.prev = nodeToMove;
        }
        return true;
    }

    public boolean contains(int songId) {
        return songIndex.containsKey(songId);
    }

    public Node getHead() {
        return this.head;
    }

    public Node getTail() {
        return this.tail;
    }
}
