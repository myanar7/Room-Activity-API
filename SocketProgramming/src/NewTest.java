import java.io.*;
import java.net.*;
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

public class NewTest {
    private static boolean isBetween(String x1, String y1, String z1) {
        int x = Integer.parseInt(x1);
        int y = Integer.parseInt(y1);
        int z = Integer.parseInt(z1);
        return x >= y && x <= y + z;
    }

    private static boolean isBetweenWithDuration(String x1, String x2, String y1, String z1) {
        int x = Integer.parseInt(x1) + Integer.parseInt(x2);
        int y = Integer.parseInt(y1);
        int z = Integer.parseInt(z1);
        return x >= y && x <= y + z;
    }

    public static void main(String[] args) throws Exception {
        // Set the port number
        int port = 8081;

        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Listening for connections on port " + port);

        while (true) {
            // Accept a client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connection established");

            // Get the input and output streams for reading and writing data to the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read the HTTP request from the client
            String request = in.readLine();
            System.out.println("Request: " + request);

            // Split the request into its individual components
            String method, path, httpVersion;
            try {
                String[] requestParts = request.split(" ");
                method = requestParts[0];
                path = requestParts[1];
                httpVersion = requestParts[2];
            } catch (Exception e) {
                continue;
            }
            

            // Print out the request details
            System.out.println("Received request:");
            System.out.println("Method: " + method);
            System.out.println("Path: " + path);
            System.out.println("HTTP Version: " + httpVersion);

            // Parse the endpoint path to determine which action to take
            String endpoint = "";
            try {
                endpoint = path.split("/")[1];
                endpoint = endpoint.substring(endpoint.indexOf("/") + 1);
                endpoint = endpoint.substring(0, endpoint.indexOf("?"));
            } catch (Exception e) {
                continue;
            }

            // Parse the query string to get the request parameters
            String query = "";
            int queryIndex = path.indexOf('?');
            if (queryIndex >= 0) {
                query = path.substring(queryIndex + 1);
            }
            String[] params = query.split("&");

            // Connect to the MongoDB database
            ConnectionString connectionString = new ConnectionString(
                    "mongodb+srv://mustafayanar:4431082@cluster0.mzkjiuf.mongodb.net/?retryWrites=true&w=majority");
            System.out.println("Connected to the connectionString successfully");
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
                    .build();
            System.out.println("Connected to the settings successfully");
            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("network");
            System.out.println("Connected to the MongoDatabase successfully");

            if (endpoint.equals("add")) {
                String name = "";
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair[0].equals("name")) {
                        name = pair[1];
                        break;
                    }
                }

                MongoCollection<Document> collection = database.getCollection("room");
                Document document = new Document("name", name);
                System.out.println("Document inserted successfully");
                int statusCode = 500;
                String response = "Received request to add room: " + name;
                if (collection.find(document).first() != null) {
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
                // Send the response to the client
                out.println("HTTP/1.1 " + statusCode);
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
            } else if (endpoint.equals("remove")) {
                String name = "";
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair[0].equals("name")) {
                        name = pair[1];
                        break;
                    }
                }
                MongoCollection<Document> collection = database.getCollection("room");
                Document document = new Document("name", name);
                System.out.println("Document inserted successfully");
                int statusCode = 500;
                String response = "Received request to add room: " + name;
                if (collection.find(document).first() != null) {
                    System.out.println("Document found successfully");
                    statusCode = 200;
                    collection.deleteOne(document);
                    response = "Room has been removed : " + name;
                } else {
                    System.out.println("Document not found");
                    statusCode = 403;
                    response = "Room does not exist";
                }

                // Send the response to the client
                out.println("HTTP/1.1 " + statusCode);
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
            } else if (endpoint.equals("reserve")) {
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
                String response = "Received request to reserve room " + name + " on day " + day + " at hour " + hour
                        + " for duration " + duration + ".";
                System.out.println("Connected to the MongoClient successfully");
                MongoCollection<Document> collection = database.getCollection("room");
                Document document = new Document("name", name);
                int statusCode = 500;
                if (name.equals("") || day.equals("") || hour.equals("") || duration.equals("")) {
                    statusCode = 400;
                    response = "Invalid input";
                } else {
                    if (collection.find(document).first() != null) {
                        collection = database.getCollection("reservation");

                        Document filter = new Document("name", name).append("day", day);

                        Document sort = new Document("hour", 1);
                        // Find the documents that match the filter
                        List<Document> results = collection.find(filter).sort(sort).into(new ArrayList<>());

                        for (Document doc : results) {
                            if (isBetween(doc.getString("hour"), hour, duration) || isBetweenWithDuration(
                                    doc.getString("hour"), doc.getString("duration"), hour, duration)) {
                                statusCode = 403;
                                response = "Room is not available"; // Burası biraz daha düzenlenecek
                                break;
                            }
                        }
                        if (statusCode != 403) {
                            System.out.println("Document found successfully");
                            statusCode = 200;
                            Document document1 = new Document("name", name).append("day", day).append("hour", hour)
                                    .append("duration", duration);
                            collection.insertOne(document1);
                            response = "Room has been reserved : " + name;
                        }
                    } else {
                        System.out.println("Document not found");
                        statusCode = 403;
                        response = "Room does not exist";
                    }
                }
                // Send the response to the client
                out.println("HTTP/1.1 " + statusCode);
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
            } else if (endpoint.equals("checkavailability")) {
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
                System.out.println("Connected to the MongoClient successfully");
                MongoCollection<Document> collection = database.getCollection("room");
                Document document = new Document("name", name);
                if(collection.find(document).first() == null) {
                    String response = "Room does not exist";
                    int statusCode = 404;
                    out.println("HTTP/1.1 " + statusCode);
                    out.println("Content-Type: text/html");
                    out.println("Content-Length: " + response.length());
                    out.println();
                    out.println(response);
                } else {
                    collection = database.getCollection("reservation");

                Document filter = new Document("name", name).append("day", day);
                Document sort = new Document("hour", 1);
                // Find the documents that match the filter
                List<Document> results = collection.find(filter).sort(sort).into(new ArrayList<>());
                HashSet<Integer> hours = new HashSet<Integer>();
                for (Document doc : results) {
                    for (int i = Integer.parseInt(doc.getString("hour")); i < Integer.parseInt(doc.getString("hour"))
                            + Integer.parseInt(doc.getString("duration")); i++) {
                        hours.add(i);
                    }
                }
                //String response = (results.size() == 0) ? "" : "Available hours: ";
                String response = "Available hours: ";
                for (int i = 9; i < 18; i++) {
                    if (!hours.contains(i)) {
                        response += i + " ";
                    }
                }
                int statusCode = (response.equals("")) ? 404 : 200;

                // Send the response to the client
                out.println("HTTP/1.1 " + statusCode);
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
                }
            }
            clientSocket.close();
        }
    }
}
