package com.study.batch_sample.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleJobConfiguration {

    private final JobRepository jobRepository;
    private final Step step;

    @Bean
    public Job simpleJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("simpleJob")
                .repository(jobRepository)
                .start(step)
                .build();
    }

    public JobBuilder get(String name) {
        return new JobBuilder(name, this.jobRepository);
    }
}
