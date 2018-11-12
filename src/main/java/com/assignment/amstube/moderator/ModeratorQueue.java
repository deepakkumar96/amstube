package com.assignment.amstube.moderator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public enum ModeratorQueue {
    INSTANCE;

    private BlockingQueue<ModeratorMessage> uploadedVideoPathQueue = new ArrayBlockingQueue<>(100);

    public ModeratorMessage dequeue(){
        try {
            return uploadedVideoPathQueue.take();
        }catch(InterruptedException ex){
            return null;
        }
    }


    public void enqueue(ModeratorMessage path){
        try {
            uploadedVideoPathQueue.put(path);
        }catch(InterruptedException ex){
            System.err.println(ex.getMessage());
        }
    }
}
