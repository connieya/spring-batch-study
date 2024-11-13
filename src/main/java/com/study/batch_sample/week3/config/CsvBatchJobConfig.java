package com.study.batch_sample.week3.config;

import com.study.batch_sample.week3.batch.UserItemProcessor;
import com.study.batch_sample.week3.batch.UserItemWriter;
import com.study.batch_sample.common.User;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.BindException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

//@Slf4j
//@Configuration
public class CsvBatchJobConfig {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JobRepository jobRepository;


    private LineMapper<User> userLineMapper() {
        DefaultLineMapper<User> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("id", "name", "lastUpdate");

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(new FieldSetMapper<User>() {
            @Override
            public User mapFieldSet(FieldSet fieldSet) throws BindException {
                User user = new User();
                user.setId(fieldSet.readLong("id")); // id를 Long으로 읽어옴
                user.setName(fieldSet.readString("name")); // name을 String으로 읽어옴
                user.setLastUpdate(LocalDate.parse(fieldSet.readString("lastUpdate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))); // lastUpdate를 LocalDate로 파싱
                return user;
            }
        });

        return lineMapper;
    }

    @Bean
    public FlatFileItemReader<User> csvReader() {
        return new FlatFileItemReaderBuilder<User>()
                .name("userCsvReader")
                .resource(new FileSystemResource("src/main/resources/week3/users.csv"))
                .linesToSkip(1)
                .delimited()
                .names(new String[] { "id", "name", "lastUpdate" })
                .lineMapper(userLineMapper()) // lineMapper 메서드 호출
                .build();
    }


    @Bean
    public Job csvJob(Step csvStep) {
        return new JobBuilder("csvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(csvStep)
                .build();
    }

    @Bean
    public Step csvStep() {
        return new StepBuilder("csvStep", jobRepository)
                .<User, User>chunk(10 ,transactionManager)
                .reader(csvReader())
                .processor(new UserItemProcessor())
                .writer(new UserItemWriter())
                .build();
    }
}
