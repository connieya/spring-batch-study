package com.study.batch_sample.week9.config;

import com.study.batch_sample.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class CustomerItemProcessor implements ItemProcessor<Customer, Customer> {
    @Override
    public Customer process(Customer item) throws Exception {
        log.info("Item Processor  --------------- {} ", item);
        return item;
    }
}
