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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@SpringBootApplication
@Configuration
@EnableAsync(proxyTargetClass=true)
public class AmstubeApplication  {


	@Autowired
	private QueueClient queueClient;
	@Autowired
	private TopicClient topicClient;
	@Autowired
	private SubscriptionClient subscriptionClient;

	@Autowired
	private ContentModerator moderator;

	public static void main(String[] args) throws InterruptedException, ServiceBusException{
		SpringApplication.run(AmstubeApplication.class, args);
		/*Thread moderatorThread = new ContentModerator();
		moderatorThread.setDaemon(true);
		moderatorThread.start();
		ModeratorQueue.INSTANCE.enqueue(ModeratorMessage.of("ireland.mp4", "DB->idTest"));*/
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initAfterStartup(){
		System.out.println("RUNNING STARTUP CODE");
		try {
			this.receiveSubscriptionMessage();
			//this.sendTopicMessage("ireland.mp4", "video->id");
		} catch (ServiceBusException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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





	private void sendTopicMessage(String file, String msg) throws ServiceBusException, InterruptedException {
		final String messageBody = file + "," + msg;
		System.out.println("Sending message: " + messageBody);
		final Message message = new Message(messageBody.getBytes(StandardCharsets.UTF_8));
		topicClient.send(message);
	}

	private void receiveSubscriptionMessage() throws ServiceBusException, InterruptedException {
		subscriptionClient.registerMessageHandler(moderator, new MessageHandlerOptions());

		//TimeUnit.SECONDS.sleep(5);
		//subscriptionClient.close();
	}


}
