package com.study.batch_sample.week7.mapper;

import com.study.batch_sample.week7.model.Customer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomerMapper {

    List<Customer> selectCustomers();
}
