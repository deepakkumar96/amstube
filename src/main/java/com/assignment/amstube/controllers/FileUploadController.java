package com.assignment.amstube.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import com.assignment.amstube.models.LogQueue;
import com.assignment.amstube.moderator.ModeratorMessage;
import com.assignment.amstube.moderator.ModeratorQueue;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assignment.amstube.ams.AzureAssetUploader;
import com.assignment.amstube.models.StreamingVideo;
import com.assignment.amstube.repo.StreamingVideoRepo;
import com.assignment.amstube.storage.*;


@Controller
public class FileUploadController {


    @Autowired
    StreamingVideoRepo repository;
    @Autowired
    private QueueClient queueClient;
    @Autowired
    private TopicClient topicClient;
    @Autowired
    private SubscriptionClient subscriptionClient;

    private final StorageService storageService = new FileSystemStorageService();
    ;



    @GetMapping("/search")
    public String searchVideo(@RequestParam("search") String searchText, Model model){
        List<StreamingVideo> videos;
        model.addAttribute("videos", repository.findAll());
        return "search";
    }


    @GetMapping("/log")
    public String serverLog(Model model){
        model.addAttribute("log", LogQueue.INSTANCE.dequeue());
        return "log";
    }

    @GetMapping("/upload")
    public String listUploadedFiles(Model model) throws IOException {

        //model.addAttribute("files", storageService.loadAll().map(
        //      path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
        //            "serveFile", path.getFileName().toString()).build().toString())
        //  .collect(Collectors.toList()));

        return "upload";
    }

    @GetMapping("/upload_vid")
    public String listUploadedFilesVid(Model model) throws IOException {

        //model.addAttribute("files", storageService.loadAll().map(
        //      path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
        //            "serveFile", path.getFileName().toString()).build().toString())
        //  .collect(Collectors.toList()));

        return "upload_vid";
    }
    /*
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }*/

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("preset") String preset, @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        String filename = storageService.store(file);
        //redirectAttributes.addFlashAttribute("message",
        //       "You successfully uploaded " + file.getOriginalFilename() + "!");
        System.err.println(preset + ", " + file);
        StreamingVideo vid = AzureAssetUploader.upload(filename, preset);
        repository.save(vid);

        //Adding video to moderator queue
        String newVideoId = vid.getId();
        System.out.println("Adding to moderator queue: " + filename + " - " + newVideoId);
        //ModeratorQueue.INSTANCE.enqueue(ModeratorMessage.of(filename, newVideoId));
        sendTopicMessage(filename, newVideoId);
        System.out.println(filename);
        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    private void sendTopicMessage(String file, String msg)  {
        final String messageBody = file + "," + msg;
        System.out.println("Sending message: " + messageBody);
        final Message message = new Message(messageBody.getBytes(StandardCharsets.UTF_8));
        try {
            topicClient.send(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ServiceBusException e) {
            e.printStackTrace();
        }
    }
}