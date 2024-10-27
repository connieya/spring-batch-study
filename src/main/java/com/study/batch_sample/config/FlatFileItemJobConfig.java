package com.study.batch_sample.config;

import com.study.batch_sample.batch.CustomerFooter;
import com.study.batch_sample.batch.CustomerHeader;
import com.study.batch_sample.batch.CustomerLineAggregator;
import com.study.batch_sample.job.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@Configuration
public class FlatFileItemJobConfig {

    public static final int CHUNK_SIZE = 100;
    public static final String ENCODING = "UTF-8";
    public static final String FLAT_FILE_CHUNK_JOB = "FLAT_FILE_CHUNK_JOB";

    @Bean
    public FlatFileItemReader<Customer> fileFileItemReader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("FlayFileItemReader")
                .resource(new ClassPathResource("./customers.csv"))
                .encoding(ENCODING)
                .delimited().delimiter(",")
                .names("name","age","gender")
                .targetType(Customer.class)
                .build();
    }

    @Bean
    public FlatFileItemWriter<Customer> flatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("flatFileItemWriter")
                .resource(new FileSystemResource("./output/customer_new.csv"))
                .encoding(ENCODING)
                .delimited().delimiter("\t")
                .names("Name","Age","Gender")
                .append(false)
                .lineAggregator(new CustomerLineAggregator())
                .headerCallback(new CustomerHeader())
                .footerCallback(new CustomerFooter(aggregateInfos))
                .build();
    }
}
