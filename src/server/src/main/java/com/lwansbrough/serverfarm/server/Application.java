package com.lwansbrough.serverfarm.server;

import java.util.concurrent.Executor;

import com.lwansbrough.serverfarm.core.services.TelemetryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
@ComponentScan({ "com.lwansbrough.serverfarm.core", "com.lwansbrough.serverfarm.server" })
public class Application {

    @Autowired
    private TelemetryService telemetryService;

	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
	}

	@Bean
	public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void configureServices() throws Exception {
        taskExecutor().execute(telemetryService);
    }
}
