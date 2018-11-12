package com.assignment.amstube;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executor;

import com.assignment.amstube.moderator.ContentModerator;
import com.assignment.amstube.moderator.ModeratorMessage;
import com.assignment.amstube.moderator.ModeratorQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.assignment.amstube.models.StreamingVideo;
import com.assignment.amstube.repo.StreamingVideoRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@Configuration
@EnableAsync
public class AmstubeApplication {


	public static void main(String[] args) {
		SpringApplication.run(AmstubeApplication.class, args);
		Thread moderatorThread = new ContentModerator();
		moderatorThread.setDaemon(true);
		moderatorThread.start();
		ModeratorQueue.INSTANCE.enqueue(ModeratorMessage.of("uploads/fight.mp4", "DB->idTest"));
	}


	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("JDAsync-");
		executor.initialize();
		return executor;
	}


	@Autowired
	StreamingVideoRepo repository;

	private String connectionString;

	//@Override
	public void run(String... args) throws Exception {
		  final StreamingVideo vid = new StreamingVideo();
		  vid.setTitle("JumanJI");
		  vid.setId("12345six");
	      // For this example, remove all of the existing records.
	      //repository.deleteAll();

	      // Save the User class to the Azure database.
	      //repository.save(vid);
	      System.err.println("TEST : " + vid.getId());
	      // Retrieve the database record for the User class you just saved by ID.
	      // final User result = repository.findOne(testUser.getId());
	      final StreamingVideo result = repository.findById(vid.getId()).get();
	      
	      // Display the results of the database record retrieval.
	      System.out.printf("DB : \n\n%s\n\n",result.toString() + " : " + connectionString);
	   }
}
