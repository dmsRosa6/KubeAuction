package com.dmsrosa.kubeauction.database.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("com.dmsrosa.kubeauction.database.dao.repository")
public class ApplicationConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "KubeAuction";
    }

}
