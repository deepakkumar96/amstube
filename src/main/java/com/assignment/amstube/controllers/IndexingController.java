package com.assignment.amstube.controllers;

import com.assignment.amstube.indexing.*;
import com.assignment.amstube.repo.StreamingVideoRepo;
import com.assignment.amstube.storage.FileSystemStorageService;
import com.assignment.amstube.storage.StorageFileNotFoundException;
import com.assignment.amstube.storage.StorageProperties;
import com.assignment.amstube.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class IndexingController {

	
	@Autowired
	StreamingVideoRepo repository;
	
    private final StorageService storageService = new FileSystemStorageService();



    @GetMapping("/get_indexing_logs")
    @ResponseBody
    public String getIndexingLog(Model model) throws IOException {
        System.out.println("LOG API CALLED");
        return IndexingLogQueue.INSTANCE.dequeue();
    }

    @GetMapping("/analytics_caption")
    public String indexCaptionGet(Model model) throws IOException {
        model.addAttribute("files", getAllFiles("caption"));
        model.addAttribute("service_url", "analytics_caption");
        return "index/index_files";
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
        model.addAttribute("files", getAllFiles("thumbnail"));
        model.addAttribute("service_url", "analytics_thumbnail");
        return "index/index_files";
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
        model.addAttribute("files", getAllFiles("ocr"));
        model.addAttribute("service_url", "analytics_ocr");
        return "index/index_files";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }



    @PostMapping("/analytics_ocr")
    public String indexOcrPost(@RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) {
        FileSystemStorageService.rootLocation = Paths.get( "uploads");
        String filename = storageService.store(file);
        IndexingResult indxRes = IndexingServiceUtil.submitTask(filename, "Azure Media OCR");
        return "redirect:/";
    }



    @GetMapping("/analytics_hyperlapse")
    public String indexHyperlapseGet(Model model) throws IOException {
        model.addAttribute("files", getAllFiles("hyperlapse"));
        model.addAttribute("service_url", "analytics_hyperlapse");
        return "index/index_files";
    }

    @PostMapping("/analytics_hyperlapse")
    public String indexHyperlapsePost(@RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        FileSystemStorageService.rootLocation = Paths.get( "uploads");
        String filename = storageService.store(file);
        IndexingResult indxRes = IndexingServiceUtil.submitTask(filename, "Azure Media Hyperlapse");
        return "redirect:/";
    }



    @GetMapping("/analytics_face")
    public String indexFaceGet(Model model) throws IOException {
        model.addAttribute("files", getAllFiles("thumbnail"));
        model.addAttribute("service_url", "analytics_thumbnail");
        return "index/indexFace";
    }

    @PostMapping("/analytics_face")
    public String indexFaceePost(@RequestParam("file") MultipartFile file,
                                      RedirectAttributes redirectAttributes) {

        String filename = storageService.store(file);
        IndexingResult indxRes = IndexingServiceUtil.submitTask(filename, "Azure Media Hyperlapse");
        return "redirect:/";
    }




    @GetMapping("/analytics_motion")
    public String indexMotionGet(Model model) throws IOException {
        model.addAttribute("files", getAllFiles("thumbnail"));
        model.addAttribute("service_url", "analytics_thumbnail");
        return "index/indexMotion";
    }

    @PostMapping("/analytics_motion")
    public String indexMotionPost(@RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {

        String filename = storageService.store(file);
        IndexingResult indxRes = IndexingServiceUtil.submitTask(filename, "Azure Media Motion");
        return "redirect:/";
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    public List<String> getAllFiles(String dir){
        FileSystemStorageService.rootLocation = Paths.get( "IndexerOutput/"+dir);
        return storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(IndexingController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList());
    }

}
