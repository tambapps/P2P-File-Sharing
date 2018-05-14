package com.tambapps.file_sharing_app.configuration;

import com.tambapps.file_sharing_app.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {

    @Value("${threads.number}")
    private int numberThreads;

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(numberThreads);
    }

    @Bean
    public ConcurrentMap<Integer, FileService.FileTask> progressMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Timer timer() {
        return new Timer();
    }
}
