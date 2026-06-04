/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

public class Node {
    public Song data; 
    public Node prev; 
    public Node next; 

    public Node(Song data) {
        this.data = data;
        this.prev = null;
        this.next = null;
    }

    public Node(Song data, Node prev, Node next) {
        this.data = data;
        this.prev = prev;
        this.next = next;
    }
    
}
