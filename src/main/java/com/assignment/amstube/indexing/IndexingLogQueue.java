package com.assignment.amstube.indexing;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public enum IndexingLogQueue {
    INSTANCE;
    private BlockingQueue queue = new ArrayBlockingQueue(100);

    IndexingLogQueue(){
     //   enqueue("first log");
    }

    public String dequeue(){
        if(queue.isEmpty())
            return "Working...";
        else return queue.poll().toString();
    }

    public void enqueue(String s){
        queue.offer(s);
    }
}
