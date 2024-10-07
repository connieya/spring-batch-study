package com.study.batch_sample.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class BatchConfig {

//    @Autowired
//    private JobBuilderFactory jobBuilderFactory;
//
//    @Autowired
//    private StepBuilderFactory stepBuilderFactory;
//
//    @Bean
//    public Job sampleJob() {
//        return jobBuilderFactory.get("sampleJob")
//                .start(sampleStep())
//                .build();
//    }
//
//    @Bean
//    public Step sampleStep() {
//        return stepBuilderFactory.get("sampleStep")
//                .tasklet((contribution, chunkContext) -> {
//                    System.out.println("Sample step executed");
//                    return RepeatStatus.FINISHED;
//                })
//                .build();
//    }
}
