package com.study.batch_sample.mapper;

import com.study.batch_sample.model.Customer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomerMapper {

    List<Customer> selectCustomers();

    void insertCustomers();
}
