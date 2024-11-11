package com.study.batch_sample.config;

import com.study.batch_sample.job.Customer;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;

@Slf4j
//@Configuration
public class JpaPagingReaderJobConfig {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String JPA_PAGING_CHUNK_JOB = "JPA_PAGING_CHUNK_JOB";

    @Autowired
    DataSource dataSource;

    @Autowired
    EntityManagerFactory entityManagerFactory;


//    @Bean
//    public JpaPagingItemReader<Customer> customerJpaPagingItemReader() {
//        JpaPagingItemReader<Customer> jpaPagingItemReader = new JpaPagingItemReader<>();
//        jpaPagingItemReader.setQueryString(
//                "Select c from CUSTOMER c where c.age > :age order by id desc"
//        );
//        jpaPagingItemReader.setEntityManagerFactory(entityManagerEntity);
//        jpaPagingItemReader.setPageSize(CHUNK_SIZE);
//        jpaPagingItemReader.setParameterValues(Collections.singletonMap("age",20));
//        return jpaPagingItemReader;
//    }

    @Bean
    public JpaPagingItemReader<Customer> customerJpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("customerJpaPagingItemReader")
                .queryString("select c from Customer c where c.age > :age order by id desc")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(entityManagerFactory)
                .parameterValues(Collections.singletonMap("age",20))
                .build();
    }




    @Bean
    public FlatFileItemWriter<Customer> customerJpaFlatFileItemWriter() {

        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerJpaFlatFileItemWriter")
                .resource(new FileSystemResource("./output/customer_new_v2.csv"))
                .encoding(ENCODING)
                .delimited().delimiter("\t")
                .names("Name", "Age", "Gender")
                .build();
    }

    @Bean
    public Step customerJpaPagingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        log.info("------------------ Init customerJpaPagingStep -----------------");

        return new StepBuilder("customerJpaPagingStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
                .reader(customerJpaPagingItemReader())
                .processor(new CustomerItemProcessor())
                .writer(customerJpaFlatFileItemWriter())
                .build();
    }

    @Bean
    public Job customerJpaPagingJob(Step customerJdbcPagingStep, JobRepository jobRepository) {
        log.info("------------------ Init customerJpaPagingJob -----------------");
        return new JobBuilder(JPA_PAGING_CHUNK_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(customerJdbcPagingStep)
                .build();
    }


}
