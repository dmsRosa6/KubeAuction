package scc.data;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBLayer {

    private static final String DB_HOSTNAME = System.getenv("MONGODB_ADDRESS");
    private static final String DB_NAME = "scc23dbrosatiago";
    private static final int DB_PORT = 27017;

    private static MongoDBLayer instance;

    public static synchronized MongoDBLayer getInstance() {
        if( instance != null)
            return instance;

        MongoClient client = new MongoClient(DB_HOSTNAME, DB_PORT);

        instance = new MongoDBLayer(client);
        return instance;
    }

    private MongoClient client;
    private MongoDatabase db;

    private MongoCollection<Document> users;
    private MongoCollection<Document> auctions;
    private MongoCollection<Document> bids;
    private MongoCollection<Document> questions;

    public MongoDBLayer(MongoClient client) { this.client = client; }

    private synchronized void init() {
        if( db != null)
            return;
        db = client.getDatabase(DB_NAME);
        users = db.getCollection("users");
        auctions = db.getCollection("auctions");
        bids = db.getCollection("bids");
        questions = db.getCollection("questions");
    }


}
