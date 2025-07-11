package com.dmsrosa.kubeauction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.dmsrosa.kubeauction.shared",
		"com.dmsrosa.kubeauction"
})
@EnableMongoRepositories(basePackages = "com.dmsrosa.kubeauction.shared.database.dao.repository")
@EnableRedisRepositories(basePackages = "com.dmsrosa.kubeauction.shared.redis")
public class KubeauctionApplication {

	public static void main(String[] args) {
		SpringApplication.run(KubeauctionApplication.class, args);
	}

}
