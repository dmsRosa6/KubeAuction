package com.dmsrosa.kubeauction.shared.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import jakarta.annotation.PostConstruct;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    public static final String BIDS_DB = "bids";
    public static final String AUCTIONS_DB = "auctions";
    public static final String USERS_DB = "users";

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        return "kubeauction";
    }

    public String getBidsName() {
        return "bids";
    }

    public String getAuctionsName() {
        return "auctions";
    }

    public String getUsersName() {
        return "Users";
    }

    @PostConstruct
    public void printConfig() {
        System.out.println("=== MONGODB CONFIGURATION ===");
        System.out.println("MongoDB URI: " + mongoUri);
        System.out.println("==============================");
    }
}