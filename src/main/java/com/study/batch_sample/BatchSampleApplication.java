package com.study.batch_sample;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;

@SpringBootApplication
@MapperScan(basePackages = "com.study.batch_sample.mapper")
public class BatchSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchSampleApplication.class, args);
	}

}
