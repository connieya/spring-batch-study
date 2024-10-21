package com.study.batch_sample.batch;

import com.study.batch_sample.job.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.FileSystemResource;

import java.time.format.DateTimeFormatter;

@Slf4j
public class UserItemWriter extends FlatFileItemWriter<User> {

    public UserItemWriter() {
        setResource(new FileSystemResource("src/main/resources/week3/output_users.csv"));
        setAppendAllowed(true); // 파일에 데이터를 추가할 수 있도록 설정
        setLineAggregator(new LineAggregator<User>() {
            @Override
            public String aggregate(User user) {
                return String.format("%d,%s,%s",
                        user.getId(),
                        user.getName(),
                        user.getLastUpdate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });
    }
}