/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.assignment.amstube.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author deepak
 */
public class ThumbnailGen {
    
    private static List<String> images = new ArrayList<>();
    public static String baseImgUrl = "images/";
    static{
        images = new ArrayList<>();
        images.add("t1.jpg");
        images.add("t2.jpg");
        images.add("t3.jpg");
        images.add("e1.jpg");
        images.add("e2.jpg");
        images.add("e3.jpg");
        images.add("eg1.jpg");
    }
    public static Random rand = new Random();
    public static String random(){
        return baseImgUrl+images.get(rand.nextInt(images.size()));
    }
    
}
