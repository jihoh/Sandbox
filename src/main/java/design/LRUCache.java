package design;

import java.util.HashMap;
import java.util.Map;

class LRUCache {

    class Node {
        Node prev;
        Node next;
        int key;
        int value;
    }

    class DLL {

        Node head;
        Node tail;

        DLL() {
            head = new Node();
            tail = new Node();
            head.next = tail;
            tail.prev = head;
        }

        void addNodeToFirst(Node node) {
            node.prev = head;
            node.next = head.next;
            head.next = node;
            node.next.prev = node;
        }

        Node removeLastNode() {
            Node lastNode = tail.prev;
            removeNode(lastNode);
            return lastNode;
        }

        void moveNodeToFirst(Node node) {
            removeNode(node);
            addNodeToFirst(node);
        }

        void removeNode(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

    }

    DLL dll;
    Map<Integer, Node> map;
    int capacity;

    public LRUCache(int capacity) {
        this.dll = new DLL();
        this.map = new HashMap<>();
        this.capacity = capacity;
    }

    public int get(int key) {
        if(map.containsKey(key)) {
            Node node = map.get(key);
            dll.moveNodeToFirst(node);
            return node.value;
        }
        return -1;
    }

    public void put(int key, int value) {
        if(map.containsKey(key)) {
            Node node = map.get(key);
            node.value = value;
            dll.moveNodeToFirst(node);
        } else {
            Node node = new Node();
            node.key = key;
            node.value = value;
            map.put(key, node);
            dll.addNodeToFirst(node);
            if (map.size() > capacity) {
                Node lastNode = dll.removeLastNode();
                map.remove(lastNode.key);
            }
        }
    }
}

/**
 * Your LRUCache object will be instantiated and called as such:
 * LRUCache obj = new LRUCache(capacity);
 * int param_1 = obj.get(key);
 * obj.put(key,value);
 */