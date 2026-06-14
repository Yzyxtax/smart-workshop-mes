package com.xtax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class SmartWorkshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartWorkshopApplication.class, args);
    }

}
