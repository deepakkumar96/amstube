/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.assignment.amstube.models;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author deepak
 */

public enum LogQueue {
    INSTANCE;
    private BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);

    public String dequeue(){
        if(queue.isEmpty())
            return "All Task Completed!";
        else return queue.poll();
    }

    public void enqueue(String s){
        queue.offer(s);
    }
}

