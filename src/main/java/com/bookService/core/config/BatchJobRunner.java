package com.bookService.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/*@Component
@RequiredArgsConstructor
@Slf4j*/
public class BatchJobRunner
        //implements ApplicationRunner
{

//    private final JobLauncher jobLauncher;

//    private final @Qualifier("cartItemInsertJob") Job cartItemInsertJob;

  /*
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("===== Starting cartItemInsertJob Insert Job =====");
        JobExecution itemJobExecution = jobLauncher.run(cartItemInsertJob,
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );
        log.info("cartItemInsertJob Insert Job Status: " + itemJobExecution.getStatus());

        log.info("===== All Jobs Completed =====");
    }
*/
}