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

/*
@Component
@Slf4j
*/
public class BatchJobRunner
//        implements ApplicationRunner
{

//    private final JobLauncher jobLauncher;

    /*
    @Qualifier("accountInsertJob")
    private final Job accountInsertJob;

    @Qualifier("itemInsertJob")
    private final Job itemInsertJob;

    private final Job cartItemInsertJob;

    */
//    private final Job accountToMongoCartJob;

    /*
    public BatchJobRunner(JobLauncher jobLauncher,
                          @Qualifier("accountToMongoCartJob") Job accountToMongoCartJob) {
        this.jobLauncher = jobLauncher;
        this.accountToMongoCartJob = accountToMongoCartJob;
    }
*/
    /*
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("===== Starting accountToMongoCartJob Insert Job =====");
        JobExecution accountToMongoCartJobExecution = jobLauncher.run(accountToMongoCartJob,
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );
         log.info("accountToMongoCartJob Insert Job Status: " + accountToMongoCartJobExecution.getStatus());

        log.info("===== All Jobs Completed =====");
    }

*/
}