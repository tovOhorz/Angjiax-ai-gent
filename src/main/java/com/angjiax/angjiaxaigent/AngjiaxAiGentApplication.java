package com.angjiax.angjiaxaigent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.angjiax.angjiaxaigent.mapper")
@SpringBootApplication
public class AngjiaxAiGentApplication {


    public static void main(String[] args) {
        SpringApplication.run(AngjiaxAiGentApplication.class, args);
    }

}
