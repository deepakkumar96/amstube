package com.assignment.amstube.indexing;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexingService {

    //@Autowired
    private static VideoIndexer videoIndexer = new VideoIndexer();

    private static List<String> videoFileTypes = Arrays.asList("mp4", "avi");
    private static List<String> audioFileTypes = Arrays.asList("ogg", "mp3");


    public static IndexingResult submitTask(String filePath, String service){
        return idetifyTypeAndSubmit(filePath, service);
    }

    public static IndexingResult idetifyTypeAndSubmit(String filePath, String service) {
        switch(type(filePath)){
            case VIDEO: return videoIndexer.index(filePath, service, "");
            case AUDIO: return videoIndexer.index(filePath, service, "");
            default: return IndexingResult.UNSUCCESFUL;
        }
    }

    public static FileType type(String file){
        if(file.contains(".")) {
            String fileType = file.substring(file.lastIndexOf(".")+1);
            if(videoFileTypes.contains(fileType))
                return FileType.VIDEO;
            else if(audioFileTypes.contains(fileType))
                return FileType.AUDIO;
        }
        return FileType.OTHER;
    }

}
