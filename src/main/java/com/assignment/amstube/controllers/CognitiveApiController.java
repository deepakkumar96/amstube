package com.assignment.amstube.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
//@RequestMapping(value = "/test")
public class CognitiveApiController {



    @GetMapping("/{service:[a-zA-z0-9_]+}")
    public String analyze(@PathVariable String service
    ) {
        return "cognitive/"+ service;
    }


}
