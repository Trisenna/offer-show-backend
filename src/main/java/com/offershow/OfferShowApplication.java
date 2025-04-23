package com.offershow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Offer Show 应用程序入口
 */
@SpringBootApplication
@MapperScan("com.offershow.repository")
@EnableAsync
@EnableScheduling
public class OfferShowApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfferShowApplication.class, args);
    }
}