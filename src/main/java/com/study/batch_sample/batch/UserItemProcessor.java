package com.study.batch_sample.batch;

import com.study.batch_sample.job.User;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;

public class UserItemProcessor implements ItemProcessor<User, User> {

    @Override
    public User process(User user) throws Exception {
        // 현재 날짜에서 1년 전 날짜를 계산
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        // user의 lastUpdate가 1년 전 날짜보다 이전인지 확인
        if (user.getLastUpdate().isBefore(oneYearAgo)) {
            user.setName(user.getName() + " (INACTIVE)");
        }
        return user;
    }
}
