package com.study.batch_sample.week4.batch;

import org.springframework.batch.item.file.FlatFileFooterCallback;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;

public class CustomerFooter implements FlatFileFooterCallback {

    ConcurrentHashMap<String, Integer> aggregateCustomers;

    public CustomerFooter(ConcurrentHashMap<String, Integer> aggregateCustomers) {
        this.aggregateCustomers = aggregateCustomers;
    }

    @Override
    public void writeFooter(Writer writer) throws IOException {
        writer.write("총 고객 수: " + aggregateCustomers.get("TOTAL_CUSTOMERS"));

        writer.write(System.lineSeparator());
        writer.write("총 나이: " + aggregateCustomers.get("TOTAL_AGES"));
        writer.write("\n"); // 줄바꿈 추가
    }
}