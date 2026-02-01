package com.example.carsharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CarSharingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarSharingServiceApplication.class, args);
    }
}
