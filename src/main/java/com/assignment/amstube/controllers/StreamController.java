package com.assignment.amstube.controllers;

import com.assignment.amstube.models.LogQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.assignment.amstube.models.StreamingVideo;
import com.assignment.amstube.repo.StreamingVideoRepo;

@Controller
//@RequestMapping(value = "/test")
public class StreamController {
 
	@Autowired
	private StreamingVideoRepo repository;



    @GetMapping("/play")
    public String play(@RequestParam("id") String id, ModelMap model) {
        //ModelAndView mv = new ModelAndView();
		repository.findById(id).ifPresent(e -> model.addAttribute("video", e));
        
        return "video";
    }
    
    @GetMapping("/")
    public String home(ModelMap model) {
        model.addAttribute("videos", repository.findAll());
        String log = LogQueue.INSTANCE.dequeue();
        System.out.println("LOG: "+log);
        model.addAttribute("log", log);
        return "index";
    }

    @GetMapping("/movies")
    public String movies(ModelMap model) {
        //model.addAttribute("videos", repository.findAll());

        return "index/movies";
    }
	
}
