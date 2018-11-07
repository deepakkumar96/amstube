package com.assignment.amstube.models;

import org.springframework.data.annotation.Id;

import com.microsoft.azure.spring.data.documentdb.core.mapping.Document;

import lombok.Data;

public class StreamingVideo {
	@Id
	private String id;
	private String title;
	private String thumbnail;
	private int size;
	private String type;
	private boolean encrypted;
	
	private String streamingEndpoint;
	private boolean isStreamable;
	private boolean isActive;
	
	@Override
	public String toString(){
		return title + " -> " + streamingEndpoint;
	}
	
	public static void main(String...args){
		StreamingVideo v = new StreamingVideo();
		
	}



	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getThumbnail() {
		return thumbnail;
	}



	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}



	public int getSize() {
		return size;
	}



	public void setSize(int size) {
		this.size = size;
	}



	public String getType() {
		return type;
	}



	public void setType(String type) {
		this.type = type;
	}



	public boolean isEncrypted() {
		return encrypted;
	}



	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}



	public String getStreamingEndpoint() {
		return streamingEndpoint;
	}



	public void setStreamingEndpoint(String streamingEndpoint) {
		this.streamingEndpoint = streamingEndpoint;
	}



	public boolean isStreamable() {
		return isStreamable;
	}



	public void setStreamable(boolean isStreamable) {
		this.isStreamable = isStreamable;
	}



	public boolean isActive() {
		return isActive;
	}



	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
}
