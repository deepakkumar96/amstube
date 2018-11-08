package com.assignment.amstube.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
//@RequestMapping(value = "/test")
public class WelcomeController {



	@GetMapping("/test")
    public String getTestData() {
        //ModelAndView mv = new ModelAndView();
        //mv.setViewName("welcome");
        //mv.getModel().put("data", "Welcome home man");
 
        return "index";
    }
	
	@GetMapping("/socket")
    public String socket_view() {
        //ModelAndView mv = new ModelAndView();
        //mv.setViewName("welcome");
        //mv.getModel().put("data", "Welcome home man");
 
        return "socket";
    }
}
