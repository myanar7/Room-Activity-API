import java.io.*;
import java.net.*;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class NewTest3 {

    public static void main(String[] args) throws Exception {
        // Set the port number
        int port = 8083;

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

            if (endpoint.equals("reserve")) {
                String name = "";
                String activity = "";
                String day = "";
                String hour = "";
                String duration = "";
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair[0].equals("room")) {
                        name = pair[1];
                    } else if (pair[0].equals("activity")) {
                        activity = pair[1];
                    } else if (pair[0].equals("day")) {
                        day = pair[1];
                    } else if (pair[0].equals("hour")) {
                        hour = pair[1];
                    } else if (pair[0].equals("duration")) {
                        duration = pair[1];
                        break;
                    }
                }
                int statusCode = 500;
                String response = "Received request to: " + activity;
                Socket activitySocket = new Socket("localhost", 8082);
                BufferedReader activityIn = new BufferedReader(new InputStreamReader(activitySocket.getInputStream()));
                PrintWriter activityOut = new PrintWriter(activitySocket.getOutputStream(), true);
                activityOut.println("GET /check?name=" + activity + " HTTP/1.1");
                // activityResponses[0] = "HTTP/1.1" activityResponses[1] = "200" etc.
                String[] activityResponse = activityIn.readLine().split(" ");

                if (activityResponse.length == 2) {
                    if (activityResponse[1].equals("200")) {
                        Socket roomSocket = new Socket("localhost", 8081);
                        BufferedReader roomIn = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));
                        PrintWriter roomOut = new PrintWriter(roomSocket.getOutputStream(), true);
                        roomOut.println("GET /reserve?name=" + name + "&day=" + day + "&hour=" + hour + "&duration="
                                + duration + " HTTP/1.1");
                        String[] roomResponse = roomIn.readLine().split(" ");

                        if (roomResponse.length == 2) {
                            if (roomResponse[1].equals("200")) {
                                statusCode = 200;
                                response = "Reservation successful";
                            } else if (roomResponse[1].equals("403")) {
                                System.out.println("Room is already reserved");
                                statusCode = 403;
                                response = "Room does not exist";
                            } else if (roomResponse[1].equals("400")) {
                                System.out.println("Invalid Input");
                                statusCode = 400;
                                response = "Invalid Input";
                            }
                        } else {
                            System.out.println("Something went wrong with the room server");
                            statusCode = 500;
                            response = "Server Error Line:139";
                        }
                        roomSocket.close();
                    } else if (activityResponse[1].equals("404")) {
                        statusCode = 404;
                        response = "Activity does not exist";
                    }
                } else {
                    System.out.println("Something went wrong with the activity server");
                    statusCode = 500;
                    response = "Server Error Line:149";
                }
                activitySocket.close();

                // Send the response to the client
                out.println("HTTP/1.1 " + statusCode);
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
            } else if (endpoint.equals("listavailability")) {
                String room = "";
                String day = "";
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair[0].equals("room")) {
                        room = pair[1];
                    } else if (pair[0].equals("day")) {
                        day = pair[1];
                        break;
                    }
                }
                int statusCode = 500;
                String response = "ERROR";

                if (!day.equals("")) {
                    Socket roomSocket = new Socket("localhost", 8081);
                    BufferedReader roomIn = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));
                    PrintWriter roomOut = new PrintWriter(roomSocket.getOutputStream(), true);
                    roomOut.println("GET /checkavailability?name=" + room + "&day=" + day + " HTTP/1.1");
                    String[] roomResponse = roomIn.readLine().split(" ");

                    System.out.println("Received request to list availability for: " + room);
                    if (roomResponse.length == 2) {
                        if (roomResponse[1].equals("200")) {
                            statusCode = 200;
                            roomIn.readLine();
                            roomIn.readLine();
                            roomIn.readLine();
                            response = roomIn.readLine();
                        } else if (roomResponse[1].equals("404")) {
                            System.out.println("No Such Room Exists");
                            statusCode = 404;
                            response = "No Such Room Exists";
                        } else if (roomResponse[1].equals("400")) {
                            System.out.println("Invalid Input");
                            statusCode = 400;
                            response = "Invalid Input";
                        }
                    } else {
                        System.out.println("Something went wrong with the room server");
                        statusCode = 500;
                        response = "Server Error Line:201";
                    }
                    roomSocket.close();
                } else {
                    for (int i = 1; i < 8; i++) {
                        day = Integer.toString(i);
                        Socket roomSocket = new Socket("localhost", 8081);
                        BufferedReader roomIn = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));
                        PrintWriter roomOut = new PrintWriter(roomSocket.getOutputStream(), true);
                        roomOut.println("GET /checkavailability?name=" + room + "&day=" + day + " HTTP/1.1");
                        String[] roomResponse = roomIn.readLine().split(" ");

                        System.out.println("Received request to list availability for: " + room);

                        if (roomResponse.length == 2) {
                            if (roomResponse[1].equals("200")) {
                                statusCode = 200;
                                roomIn.readLine();
                                roomIn.readLine();
                                roomIn.readLine();
                                response += roomIn.readLine() + "\n";
                            } else if (roomResponse[1].equals("404")) {
                                System.out.println("No Such Room Exists");
                                statusCode = 404;
                                response = "No Such Room Exists";
                            } else if (roomResponse[1].equals("400")) {
                                System.out.println("Invalid Input");
                                statusCode = 400;
                                response = "Invalid Input";
                            }
                        } else {
                            System.out.println("Something went wrong with the room server");
                            statusCode = 500;
                            response = "Server Error Line:234";
                        }
                        roomSocket.close();
                    }
                }
                // Send the response to the client
                out.println("HTTP/1.1 " + statusCode);
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + response.length());
                out.println();
                out.println(response);
            } else if (endpoint.equals("display")) {
                String id = "";
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair[0].equals("id")) {
                        id = pair[1];
                    }
                }
                int statusCode = 0;
                String response = "";
                System.out.println("Connected to the MongoClient successfully");
                MongoCollection<Document> collection = database.getCollection("reservation");
                ObjectId objectId = new ObjectId(id);
                Document document = new Document("reservation_id", objectId);
                if (!id.equals("")) {
                    if (collection.find(document).first() != null) {
                        System.out.println("Document found successfully");
                        statusCode = 200;
                        response = collection.find(document).first().toJson();
                    } else {
                        System.out.println("Document not found");
                        statusCode = 404;
                        response = "Reservation does not exist";
                    }
                } else {
                    statusCode = 500;
                    response = "Error";
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
