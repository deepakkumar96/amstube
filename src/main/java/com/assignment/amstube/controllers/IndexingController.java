package com.assignment.amstube.controllers;

import com.assignment.amstube.indexing.*;
import com.assignment.amstube.repo.StreamingVideoRepo;
import com.assignment.amstube.storage.FileSystemStorageService;
import com.assignment.amstube.storage.StorageFileNotFoundException;
import com.assignment.amstube.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;


@Controller
public class IndexingController {

	
	@Autowired
	StreamingVideoRepo repository;
	
    private final StorageService storageService = new FileSystemStorageService();

    

    @GetMapping("/analytics_caption")
    public String indexCaptionGet(Model model) throws IOException {

        System.out.println("CAPTION");
        return "index/indexCaption";
    }



    @PostMapping("/analytics_caption")
    public String indexCaptionPost(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        String filename = storageService.store(file);
        Indexer indexer;
        IndexingResult indxRes = IndexingServiceUtil.submitTask(filename, "Azure Media Indexer");
        //repository.save(vid);
        System.out.println(filename);
        System.out.println("Indexed : " + indxRes);
        return "redirect:/";
    }


    @GetMapping("/analytics_thumbnail")
    public String indexThumbnailGet(Model model) throws IOException {
        return "index/indexThumbnail";
    }



    @PostMapping("/analytics_thumbnail")
    public String indexThumbnailPost(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        String filename = storageService.store(file);
        IndexingResult indxRes = IndexingServiceUtil.submitTask(filename, "Azure Media Video Thumbnails");
        return "redirect:/";
    }


    @GetMapping("/analytics_ocr")
    public String indexOcrGet(Model model) throws IOException {
        return "index/indexOcr";
    }



    @PostMapping("/analytics_ocr")
    public String indexOcrPost(@RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) {

        String filename = storageService.store(file);
        IndexingResult indxRes = IndexingServiceUtil.submitTask(filename, "Azure Media OCR");
        return "redirect:/";
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
