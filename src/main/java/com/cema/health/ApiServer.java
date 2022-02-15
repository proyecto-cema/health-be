package com.cema.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@ServletComponentScan
public class ApiServer {

    public static void main(String[] args) {
        SpringApplication.run(ApiServer.class, args);
    }


    @PostConstruct
    public void init() {
        // Setting Spring Boot SetTimeZone
        TimeZone.setDefault(TimeZone.getTimeZone("America/Buenos_Aires"));
    }

}
