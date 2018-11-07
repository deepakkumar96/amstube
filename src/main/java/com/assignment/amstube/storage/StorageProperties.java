package com.assignment.amstube.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

//@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    public static String location = "uploads";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
