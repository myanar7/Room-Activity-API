import java.io.*;
import java.net.*;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class NewTest2 {

    public static void main(String[] args) throws Exception {
        // Set the port number
        int port = 8082;

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

                MongoCollection<Document> collection = database.getCollection("activity");
                Document document = new Document("name", name);
                System.out.println("Document inserted successfully");
                int statusCode = 500;
                String response = "Received request to add room: " + name;
                if (collection.find(document).first() != null) {
                    System.out.println("Document found successfully");
                    statusCode = 403;
                    response = "Activity already exists";
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
                MongoCollection<Document> collection = database.getCollection("activity");
                Document document = new Document("name", name);
                System.out.println("Document inserted successfully");
                int statusCode = 500;
                String response = "Received request to add room: " + name;
                if (collection.find(document).first() != null) {
                    System.out.println("Document found successfully");
                    statusCode = 200;
                    collection.deleteOne(document);
                    response = "Activity has been removed : " + name;
                } else {
                    System.out.println("Document not found");
                    statusCode = 403;
                    response = "Activity does not exist";
                }

                // Send the response to the client
                out.println("HTTP/1.1 " + statusCode);
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
            } else if (endpoint.equals("check")) {
                String name = "";
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair[0].equals("name")) {
                        name = pair[1];
                    }
                }
                String response = "Activity: " + name + " is available";
                System.out.println("Connected to the MongoClient successfully");
                MongoCollection<Document> collection = database.getCollection("activity");
                Document document = new Document("name", name);
                int statusCode = 500;
                if (name.equals("")) {
                    statusCode = 400;
                    response = "Invalid input";
                } else {
                    if (collection.find(document).first() != null) {
                        System.out.println("Document found successfully");
                        statusCode = 200;
                        response = "Activity " + name + " is available";
                    } else {
                        System.out.println("Document not found");
                        statusCode = 404;
                        response = "Activity does not exist";
                    }
                }
                // Send the response to the client
                out.println("HTTP/1.1 " + statusCode);
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
            }
            mongoClient.close();
            clientSocket.close();
        }
    }
}
