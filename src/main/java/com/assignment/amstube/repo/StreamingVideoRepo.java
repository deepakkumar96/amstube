package com.assignment.amstube.repo;

import org.springframework.stereotype.Repository;

import com.assignment.amstube.models.StreamingVideo;
import com.microsoft.azure.spring.data.documentdb.repository.DocumentDbRepository;

@Repository
public interface StreamingVideoRepo extends DocumentDbRepository<StreamingVideo, String> {

}
