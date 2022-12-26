import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class Test {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8002), 0);
        server.createContext("/add", new AddHandler());
        server.createContext("/remove", new RemoveHandler());
        server.createContext("/reserve", new ReserveHandler());
        server.createContext("/checkavailability", new CheckAvailabilityHandler());
        server.setExecutor(null); // creates a default executor
        server.start();    
    }

    // String response = "<HTML> <HEAD> <TITLE>" + "Error" + "</TITLE> </HEAD> <BODY>" + "Bu birinci test" + "</BODY> </HTML>";

    private static class AddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            String[] params = query.split("&");
            String name = "";
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair[0].equals("name")) {
                    name = pair[1];
                    break;
                }
            }
            ConnectionString connectionString = new ConnectionString("mongodb+srv://mustafayanar:4431082@cluster0.mzkjiuf.mongodb.net/?retryWrites=true&w=majority");
            System.out.println("Connected to the connectionString successfully");
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
            System.out.println("Connected to the MongoClientSettings successfully");
            MongoClient mongoClient = MongoClients.create(settings);
            System.out.println("Connected to the MongoClient successfully");
            MongoDatabase database = mongoClient.getDatabase("network");
            System.out.println("Connected to the MongoDatabase successfully");
            MongoCollection<Document> collection = database.getCollection("room");
            Document document = new Document("name", name);
            System.out.println("Document inserted successfully");
            int statusCode = 500;
            String response = "Received request to add room: " + name;
            if(collection.find(document).first() != null){
                System.out.println("Document found successfully");
                statusCode = 403;
                response = "Room already exists";
            } else {
                System.out.println("Document not found");
                statusCode = 200;
                System.out.println("Document created successfully");
                collection.insertOne(document);
                response = "Received request to add room: " + name;
            }

            t.sendResponseHeaders(statusCode, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class RemoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            String[] params = query.split("&");
            String name = "";
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair[0].equals("name")) {
                    name = pair[1];
                    break;
                }
            }
            ConnectionString connectionString = new ConnectionString("mongodb+srv://mustafayanar:4431082@cluster0.mzkjiuf.mongodb.net/?retryWrites=true&w=majority");
            System.out.println("Connected to the connectionString successfully");
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
            System.out.println("Connected to the MongoClientSettings successfully");
            MongoClient mongoClient = MongoClients.create(settings);
            System.out.println("Connected to the MongoClient successfully");
            MongoDatabase database = mongoClient.getDatabase("network");
            System.out.println("Connected to the MongoDatabase successfully");
            MongoCollection<Document> collection = database.getCollection("room");
            Document document = new Document("name", name);
            System.out.println("Document inserted successfully");
            int statusCode = 500;
            String response = "Received request to add room: " + name;
            if(collection.find(document).first() != null){
                System.out.println("Document found successfully");
                statusCode = 200;
                collection.deleteOne(document);
                response = "Room has been removed : " + name;
            } else {
                System.out.println("Document not found");
                statusCode = 403;
                response = "Room does not exist";
            }

            t.sendResponseHeaders(statusCode, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class ReserveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            String[] params = query.split("&");
            String name = "";
            String day = "";
            String hour = "";
            String duration = "";
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair[0].equals("name")) {
                    name = pair[1];
                } else if (pair[0].equals("day")) {
                    day = (Integer.parseInt(pair[1]) > 0 && Integer.parseInt(pair[1]) < 8) ? pair[1] : "";
                } else if (pair[0].equals("hour")) {
                    hour = (Integer.parseInt(pair[1]) > 8 && Integer.parseInt(pair[1]) < 18) ? pair[1] : "";
                } else if (pair[0].equals("duration")) {
                    duration = (Integer.parseInt(pair[1]) + Integer.parseInt(hour) < 18) ? pair[1] : "";
                }
            }
            String response = "Received request to reserve room " + name + " on day " + day + " at hour " + hour + " for duration " + duration + ".";

            ConnectionString connectionString = new ConnectionString("mongodb+srv://mustafayanar:4431082@cluster0.mzkjiuf.mongodb.net/?retryWrites=true&w=majority");
            System.out.println("Connected to the connectionString successfully");
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
            System.out.println("Connected to the MongoClientSettings successfully");
            MongoClient mongoClient = MongoClients.create(settings);
            System.out.println("Connected to the MongoClient successfully");
            MongoDatabase database = mongoClient.getDatabase("network");
            System.out.println("Connected to the MongoDatabase successfully");
            MongoCollection<Document> collection = database.getCollection("room");
            Document document = new Document("name", name);
            int statusCode = 500;
            if(name.equals("") || day.equals("") || hour.equals("") || duration.equals("")){
                statusCode = 400;
                response = "Invalid input";
            } else {
                if(collection.find(document).first() != null){
                    collection = database.getCollection("reservation");

                    Document filter = new Document("name", name).append("day", day);

                    Document sort = new Document("hour", 1);
                    // Find the documents that match the filter
                    List<Document> results = collection.find(filter).sort(sort).into(new ArrayList<>());

                    for (Document doc : results) {
                        if(isBetween(doc.getString("hour"), hour, duration) || isBetweenWithDuration(doc.getString("hour"), doc.getString("duration"), hour, duration)){
                            statusCode = 403;
                            response = "Room is not available";     // Burası biraz daha düzenlenecek
                            break;
                        }
                    }
                    if(statusCode != 403) {
                        System.out.println("Document found successfully");
                        statusCode = 200;
                        Document document1 = new Document("name", name).append("day", day).append("hour", hour).append("duration", duration);
                        collection.insertOne(document1);
                        response = "Room has been reserved : " + name;
                    }
                } else {
                    System.out.println("Document not found");
                    statusCode = 403;
                    response = "Room does not exist";
                }
            }
            
            t.sendResponseHeaders(statusCode, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean isBetween(String x1, String y1, String z1) {
            int x = Integer.parseInt(x1);
            int y = Integer.parseInt(y1);
            int z = Integer.parseInt(z1);
            return x >= y && x <= y+z;
        }

        private boolean isBetweenWithDuration(String x1, String x2, String y1, String z1) {
            int x = Integer.parseInt(x1) + Integer.parseInt(x2);
            int y = Integer.parseInt(y1);
            int z = Integer.parseInt(z1);
            return x >= y && x <= y+z;
        }
    }

    static class CheckAvailabilityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Get the query parameters from the request URL
            String query = t.getRequestURI().getQuery();
            String[] params = query.split("&");
            String name = "";
            String day = "";
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue[0].equals("name")) {
                    name = keyValue[1];
                } else if (keyValue[0].equals("day")) {
                    day = keyValue[1];
                }
            }
            ConnectionString connectionString = new ConnectionString("mongodb+srv://mustafayanar:4431082@cluster0.mzkjiuf.mongodb.net/?retryWrites=true&w=majority");
            System.out.println("Connected to the connectionString successfully");
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
            System.out.println("Connected to the MongoClientSettings successfully");
            MongoClient mongoClient = MongoClients.create(settings);
            System.out.println("Connected to the MongoClient successfully");
            MongoDatabase database = mongoClient.getDatabase("network");
            System.out.println("Connected to the MongoDatabase successfully");
            MongoCollection<Document> collection = database.getCollection("reservation");

            Document filter = new Document("name", name).append("day", day);
            Document sort = new Document("hour", 1);
            // Find the documents that match the filter
            List<Document> results = collection.find(filter).sort(sort).into(new ArrayList<>());
            HashSet<Integer> hours = new HashSet<Integer>();
            for (Document doc : results) {
                for (int i = Integer.parseInt(doc.getString("hour")); i < Integer.parseInt(doc.getString("hour")) + Integer.parseInt(doc.getString("duration")); i++) {
                    hours.add(i);
                }
            }
            String response = (results.size() == 0) ? "" : "Available hours: ";
            for (int i = 9; i < 18; i++) {
                if(!hours.contains(i)){
                    response += i + " ";
                }
            }
            int statusCode = (response.equals("")) ? 404 : 200;

            // Close the response body
            t.sendResponseHeaders(statusCode, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}