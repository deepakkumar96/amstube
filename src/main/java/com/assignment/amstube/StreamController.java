package com.assignment.amstube;

import org.springframework.beans.factory.annotation.Autowired;
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
	
}
