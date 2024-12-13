package design;

import java.util.Comparator;
import java.util.PriorityQueue;

class MedianFinder {

    PriorityQueue<Integer> maxHeap;
    PriorityQueue<Integer> minHeap;

    public MedianFinder() {
        minHeap = new PriorityQueue<>(Comparator.naturalOrder());
        maxHeap = new PriorityQueue<>(Comparator.reverseOrder()); // (a, b) -> b - a
    }

    public void addNum(int num) {
        if(maxHeap.isEmpty()) { // need this to start
            maxHeap.add(num);
            return;
        }
        if(num > maxHeap.peek()) {
            minHeap.add(num);
        } else {
            maxHeap.add(num);
        }

        if(minHeap.size() > maxHeap.size()+1) {
            maxHeap.add(minHeap.remove());
            return ;
        }
        if(maxHeap.size() > minHeap.size()+1) {
            minHeap.add(maxHeap.remove());
            return ;
        }
    }

    public double findMedian() {
        if(maxHeap.size() > minHeap.size()) {
            return maxHeap.peek();
        } else if(maxHeap.size() < minHeap.size()) {
            return minHeap.peek();
        } else {
            return (minHeap.peek()+maxHeap.peek())/2.0;
        }

    }
}
