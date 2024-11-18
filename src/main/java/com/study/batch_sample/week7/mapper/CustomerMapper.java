package com.study.batch_sample.week7.mapper;

import com.study.batch_sample.week7.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerMapper {

    List<Customer> selectCustomers();
}
