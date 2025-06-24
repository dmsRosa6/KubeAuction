package com.dmsrosa.popularauctions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.dmsrosa.kubeauction.shared",
        "com.dmsrosa.popularauctions"
})
@EnableScheduling
public class PopularAuctionsWorker {
     public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PopularAuctionsWorker.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}

