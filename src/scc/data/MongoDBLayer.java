package scc.data;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.ws.rs.WebApplicationException;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBLayer {

    private static final String DB_HOSTNAME = System.getenv("ME_CONFIG_MONGODB_SERVER");
    private static final String DB_NAME = "scc23dbrosatiago";
    private static final int DB_PORT = 27017;

    private static CodecRegistry pojoCodecRegistry;

    private static MongoDBLayer instance;

    public static synchronized MongoDBLayer getInstance() {
        if( instance != null) {
            return instance;
        }

        System.out.println("MONGO HOSTNAME: " + DB_HOSTNAME);

        pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));


        //é preciso o url -> hostname::port
        MongoClient client = new MongoClient(DB_HOSTNAME+":"+DB_PORT, MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());

        //MongoClient client = new MongoClient(DB_HOSTNAME,DB_PORT);

        instance = new MongoDBLayer(client);
        return instance;
    }

    private MongoClient client;
    private MongoDatabase db;

    private MongoCollection<User> users;
    private MongoCollection<Document> auctions;
    private MongoCollection<Document> bids;
    private MongoCollection<Document> questions;


    public MongoDBLayer(MongoClient client) { this.client = client;}

    private synchronized void init() {
        if( db != null)
            return;
        db = client.getDatabase(DB_NAME);
        users = db.getCollection("users", User.class);
        auctions = db.getCollection("auctions");
        bids = db.getCollection("bids");
        questions = db.getCollection("questions");
    }

    private void checkError(int error){
        switch ( error ){
            case 11000:

            case 12582:

            case 11001:
                throw new WebApplicationException("DUPLICATE_KEY");
            case 50:
                throw new WebApplicationException("EXECUTION_TIMEOUT");
            default:
                throw new WebApplicationException("UNCATEGORIZED");
        }
    }

    public User putUser(User user){
        init();
        try{
            users.insertOne(user);
        }catch (MongoException e) {
            System.out.println(e.getMessage());
            checkError(e.getCode());
        }
        return user;
    }

    public User getUser(String userId){
        init();
        User user = null;
        try{
            Bson query = eq("_id", userId);
            System.out.println("PASSOU");
            user = users.find(query).first();
        }catch (MongoException e) {
            System.out.println(e.getMessage());
            checkError(e.getCode());
        }
        return user;
    }

    public User updateUser(String id, User user){
        init();
        try{
            Bson query = eq("_id",id);
            users.replaceOne(query,user);
        }catch (MongoException e) {
            checkError(e.getCode());
        }
        return user;
    }

    public User deleteUser(String id,User user){
        init();
        try{
            Bson query = eq("_id",id);
            users.deleteOne(query);
        }catch (MongoException e) {
            checkError(e.getCode());
        }
        return user;
    }
}
