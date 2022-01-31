package com.liu.nkcommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan("com.liu.nkcommunity.mapper")
public class NkcommunityApplication {

	public static void main(String[] args) {
		SpringApplication.run(NkcommunityApplication.class, args);
	}

}
