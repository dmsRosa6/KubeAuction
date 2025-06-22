package com.dmsrosa.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.dmsrosa.kubeauction.shared",
        "com.dmsrosa.notifications"
})
@EnableScheduling
public class AuctionWorkerApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AuctionWorkerApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
