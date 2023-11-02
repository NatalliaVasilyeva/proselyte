package com.proselyteapi.stocksreceiver;

import com.proselyteapi.stocksreceiver.dto.CompanyDto;
import com.proselyteapi.stocksreceiver.mapper.CompanyMapper;
import com.proselyteapi.stocksreceiver.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StocksReceiverApplication {

	CompanyRepository companyRepository;

	public static void main(String[] args) {

		SpringApplication.run(StocksReceiverApplication.class, args);

	}
}