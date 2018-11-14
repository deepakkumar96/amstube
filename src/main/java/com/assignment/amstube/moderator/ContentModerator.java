package com.assignment.amstube.moderator;

import com.assignment.amstube.indexing.IndexingResult;
import com.assignment.amstube.indexing.IndexingService;
import com.assignment.amstube.indexing.VideoIndexer;
import com.assignment.amstube.models.StreamingVideo;
import com.assignment.amstube.repo.StreamingVideoRepo;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
@Configurable
public class ContentModerator implements IMessageHandler {

    ModeratorQueue queue = ModeratorQueue.INSTANCE;
    @Autowired
    private StreamingVideoRepo repo;


    public CompletableFuture<Void> onMessageAsync(IMessage message) {
        final String[] messageString = new String(message.getBody(), StandardCharsets.UTF_8).split(",");

        try {
            process(ModeratorMessage.of(messageString[0],messageString[1]));
        } catch (InterruptedException e) {
            System.err.println("Enable to process Modererator msg");
            e.printStackTrace();
        }

        System.out.println("Received message: " + messageString[0] + ", "+messageString[1]);

        return CompletableFuture.completedFuture(null);
    }

    public void notifyException(Throwable exception, ExceptionPhase phase) {
        System.out.println(phase + " encountered exception:" + exception.getMessage());
    }



    @Async
    public CompletableFuture<Void>  process(ModeratorMessage msg) throws InterruptedException {
        System.out.println("Moderator Started");
        //while(true){
            IndexingResult newResult = IndexingService.submitTask(msg.filename, "Azure Media Content Moderator");
            System.err.println("Starting Morderator On "+ msg.filename + " with "+ msg.videoId);

            String contentBaseFolder = "IndexerOutput/content";
            Path path = Paths.get(contentBaseFolder);

            try {
                Optional<Path> file = Files.list(path)
                        .filter(p -> !Files.isDirectory(p))
                        .max(Comparator.comparingLong(f -> f.toFile().lastModified()));

                file.ifPresent(f -> {
                    System.out.println("Morderator Success: " + f);
                    try {
                        String moderatorContent = new String(Files.readAllBytes(f));
                        if(ContentReviewer.voilateContatePolicy(moderatorContent)){
                            deleteVideo(msg.videoId);
                            System.err.println("Video "+file+" is deleted by moderator. Due to content policy voilation");
                        }
                        else
                            System.err.println("Uploaded video("+file+") is reviewd by modertor. NO Policy Voilation Found");

                    }catch(Exception ex){
                        System.out.println("Moderator file not found! ");
                        ex.printStackTrace();
                    }
                });

            }catch(IOException ioex){
                System.err.println("File Not Found: "+ ioex.getMessage());
            }

        return CompletableFuture.completedFuture(null);
    }

    private void deleteVideo(String videoId) {
        System.err.println("REPOSITORY: "+ repo);
        Optional<StreamingVideo> video = repo.findById(videoId);
        if(video.isPresent()) {
            System.out.println("DELETING "+video);
            repo.delete(video.get());
        }
        else
            System.err.println("INVALID video id.");
    }


}
