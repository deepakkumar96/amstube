package com.assignment.amstube.moderator;

public class ModeratorMessage {
    public String filename, videoId;

    public ModeratorMessage(String file, String id){
        this.filename= file;
        this.videoId  = id;
    }

    public static ModeratorMessage of(String f, String id){
        return new ModeratorMessage(f,id);
    }
}
