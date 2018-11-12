package com.assignment.amstube.moderator;

import com.assignment.amstube.indexing.IndexingResult;
import com.assignment.amstube.indexing.IndexingService;
import com.assignment.amstube.indexing.VideoIndexer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.Future;

@Service
public class ContentModerator extends Thread{

    ModeratorQueue queue = ModeratorQueue.INSTANCE;

    @Override
    public void run(){
        try {
            process();
        }catch (InterruptedException iex){
            System.err.println("Failed to start moderator + " + iex.getMessage());
        }
    }

    @Async
    public Future<String> process() throws InterruptedException {
        System.out.println("Moderator Started");
        while(true){
            ModeratorMessage msg = queue.dequeue();
            IndexingResult newResult = IndexingService.submitTask(msg.filename, "Azure Media Content Moderator");
            System.err.println("Starting Morderator On "+ msg.filename + " with "+ msg.videoId);
            String contentBaseFolder = "IndexerOutput/content";
            Path path = Paths.get(contentBaseFolder);
            try {
                Optional<Path> file = Files.list(path)
                        .filter(p -> !Files.isDirectory(p))
                        .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
                file.ifPresent(f -> System.out.println("Morderator Success: " + f));
            }catch(IOException ioex){
                System.err.println("File Not Found: "+ ioex.getMessage());
            }

        }
        //return new AsyncResult<>("");
    }

}
