package scc.data;

import com.mongodb.MongoClient;

public class MongoDBLayer {

    private static MongoDBLayer instance;

    public static synchronized MongoDBLayer getInstance() {
        if( instance != null)
            return instance;



        instance = new MongoDBLayer( client);
        return instance;

    }

}
