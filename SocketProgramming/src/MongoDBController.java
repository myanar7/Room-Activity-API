import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
public class MongoDBController {
    private static MongoDBController mongoDBController;

    enum CollectionName {
        ROOM, RESERVATION
    }

    public static MongoDBController getInstance() {
        if (mongoDBController == null) {
            mongoDBController = new MongoDBController();
        }
        return mongoDBController;
    }

    private void writeDocument(String collectionName, Document document) {
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.insertOne(document);
    }
}
