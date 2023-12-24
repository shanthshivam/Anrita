package com.zeronsec.event.threadpool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EventPool<T> {

    private final Deque<EventProcessor> stack;
    private final int maxSize;
    private final Lock lock;

    public EventPool(int maxSize) {
        this.stack = new ArrayDeque<EventProcessor>();
        this.maxSize = maxSize;
        this.lock = new ReentrantLock();
        for(int i = 0; i < maxSize; i++) {
        	push(new EventProcessor(i));
        }
    }

    public void push(EventProcessor item) {
        lock.lock();
        try {
            if (stack.size() == maxSize) {
                // If the stack is full, remove the oldest element
                stack.removeLast();
                
            }
            stack.push(item);
        } finally {
            lock.unlock();
        }
    }

    public EventProcessor pop() {
        lock.lock();
        try {
            return stack.pop();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return stack.size();
        } finally {
            lock.unlock();
        }
    }

//    public static void main(String[] args) {
//        // Create a thread-safe FILO with a maximum size of 3
//        ThreadSafeFILO<String> threadSafeFILO = new ThreadSafeFILO<>(3);
//
//        // Push elements onto the FILO
//        threadSafeFILO.push("Item 1");
//        threadSafeFILO.push("Item 2");
//        threadSafeFILO.push("Item 3");
//
//        // Pop elements from the FILO
//        while (threadSafeFILO.size() > 0) {
//            System.out.println(threadSafeFILO.pop());
//        }
//    }
}
