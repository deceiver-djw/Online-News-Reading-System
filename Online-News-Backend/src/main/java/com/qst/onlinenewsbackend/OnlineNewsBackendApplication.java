package com.qst.onlinenewsbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//http://localhost:8080/swagger-ui/index.html
@SpringBootApplication
@MapperScan("com.qst.onlinenewsbackend.mapper")
public class OnlineNewsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineNewsBackendApplication.class, args);
    }

}
