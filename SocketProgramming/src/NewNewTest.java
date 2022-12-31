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

public class NewNewTest {
    private static final int PORT = 8081;
    private static final String MONGO_DB_URL = "mongodb+srv://mustafayanar:4431082@cluster0.mzkjiuf.mongodb.net/?retryWrites=true&w=majority";

    public static void main(String[] args) throws Exception {
        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Listening for connections on port " + PORT);

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

            // Parse the request
            HttpRequestParser parser = new HttpRequestParser();
            HttpRequest httpRequest = parser.parse(request);
            if (httpRequest == null) {
                continue;
            }

            // Print out the request details
            System.out.println("Received request:");
            System.out.println("Method: " + httpRequest.getMethod());
            System.out.println("Path: " + httpRequest.getPath());
            System.out.println("HTTP Version: " + httpRequest.getHttpVersion());
            System.out.println("Endpoint: " + httpRequest.getEndpoint());
            System.out.println("Params: " + httpRequest.getParams());

            // Connect to the MongoDB database
            MongoClient mongoClient = MongoClientProvider.getMongoClient(MONGO_DB_URL);
            MongoDatabase database = mongoClient.getDatabase("network");
            System.out.println("Connected to the MongoDatabase successfully");

            // Handle the request based on the endpoint
            HttpResponse httpResponse = null;
            switch (httpRequest.getEndpoint()) {
                case "add":
                    httpResponse = handleAddEndpoint(httpRequest, database);
                    break;
                case "get":
                    httpResponse = handleRemoveEndpoint(httpRequest, database);
                    break;
                case "increment":
                    httpResponse = handleIncrementEndpoint(httpRequest, database);
                    break;
                default:
                // Return a 404 Not Found response if the endpoint is not recognized
                httpResponse = new HttpResponse("404 Not Found", "text/html", "Endpoint not found.");
            }

            // Send the response to the client
            out.println(httpResponse.toString());

            // Close the streams and socket
            in.close();
            out.close();
            clientSocket.close();
        }
    }

    private static HttpResponse handleAddEndpoint(HttpRequest request, MongoDatabase database) {
        // Get the request parameters
        String name = request.getParam("name");;

        // Validate the request parameters
        if (name == null) {
            return new HttpResponse("400 Bad Request", "text/html", "Missing required parameters.");
        }

        // Add the event to the database
        MongoCollection<Document> collection = database.getCollection("room");
        Document room = new Document("name", name);
        collection.insertOne(room);

        // Return a 200 OK response
        return new HttpResponse("200 OK", "text/plain", "Room added successfully." + room);
    }

    private static HttpResponse handleRemoveEndpoint(HttpRequest request, MongoDatabase database) {
        // Get the name parameter from the request
        String name = request.getParam("name");
        if (name == null) {
            return new HttpResponse("400 Bad Request", "text/plain", "Missing required parameter: name");
        }
    
        // Get the room collection from the database
        MongoCollection<Document> collection = database.getCollection("room");
    
        // Check if a room with the specified name exists
        Document document = new Document("name", name);
        if (collection.find(document).first() == null) {
            return new HttpResponse("403 Forbidden", "text/plain", "Room does not exist.");
        }
    
        // Remove the room with the specified name
        collection.deleteOne(document);
    
        // Return a 200 OK response with a success message
        return new HttpResponse("200 OK", "text/plain", "Room has been removed: " + name);
    }
    

    private static HttpResponse handleReserveEndpoint(HttpRequest request, MongoDatabase database) {
        // Get the name, day, hour, and duration parameters from the request
        String name = request.getParam("name");
        String day = request.getParam("day");
        String hour = request.getParam("hour");
        String duration = request.getParam("duration");
        if (name == null || day == null || hour == null || duration == null) {
            return new HttpResponse("400 Bad Request", "text/plain", "Missing required parameter.");
        }
    
        // Validate the input values
        if (!isValidDay(day) || !isValidHour(hour) || !isValidDuration(hour, duration)) {
            return new HttpResponse("400 Bad Request", "text/plain", "Invalid input.");
        }
    
        // Get the room collection from the database
        MongoCollection<Document> roomCollection = database.getCollection("room");
    
        // Check if a room with the specified name exists
        Document document = new Document("name", name);
        if (roomCollection.find(document).first() == null) {
            return new HttpResponse("403 Forbidden", "text/plain", "Room does not exist.");
        }
    
        // Check if the room is available at the specified time
        MongoCollection<Document> reservationCollection = database.getCollection("reservation");
        Document filter = new Document("name", name).append("day", day);
        Document sort = new Document("hour", 1);
        List<Document> results = reservationCollection.find(filter).sort(sort).into(new ArrayList<>());
        for (Document doc : results) {
            if (isOverlapping(doc.getString("hour"), doc.getString("duration"), hour, duration)) {
                return new HttpResponse("403 Forbidden", "text/plain", "Room is not available.");
            }
        }
    
        // Reserve the room
        Document reservation = new Document("name", name).append("day", day).append("hour", hour).append("duration", duration);
        reservationCollection.insertOne(reservation);
    
        // Return a 200 OK response with a success message
        return new HttpResponse("200 OK", "text/plain", "Room has been reserved: " + name);
    }

    private static boolean isOverlapping(String start1, String duration1, int start2, int duration2) {
        return start1 <= start2 + duration2 && start2 <= start1 + duration1;
    }
    
    private static boolean isValidDay(String day) {
        try {
            int d = Integer.parseInt(day);
            return d > 0 && d < 8;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private static boolean isValidHour(String hour) {
        try {
            int h = Integer.parseInt(hour);
            return h > 8 && h < 18;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private static boolean isValidDuration(String hour, String duration) {
        try {
            int h = Integer.parseInt(hour);
            int d = Integer.parseInt(duration);
            return h + d < 18;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private static HttpResponse handleCheckAvailabilityEndpoint(HttpRequest request, MongoDatabase database) {
        // Get the name and day parameters from the request
        String name = request.getParam("name");
        String day = request.getParam("day");
        if (name == null || day == null) {
            return new HttpResponse("400 Bad Request", "text/plain", "Missing required parameter.");
        }
    
        // Validate the day parameter
        if (!isValidDay(day)) {
            return new HttpResponse("400 Bad Request", "text/plain", "Invalid day.");
        }
    
        // Get the reservation collection from the database
        MongoCollection<Document> collection = database.getCollection("reservation");
    
        // Find the reservations for the specified room and day
        Document filter = new Document("name", name).append("day", day);
        Document sort = new Document("hour", 1);
        List<Document> results = collection.find(filter).sort(sort).into(new ArrayList<>());
    
        // Create a set of hours that are already reserved
        HashSet<Integer> reservedHours = new HashSet<>();
        for (Document doc : results) {
            int startHour = Integer.parseInt(doc.getString("hour"));
            int duration = Integer.parseInt(doc.getString("duration"));
            for (int i = startHour; i < startHour + duration; i++) {
                reservedHours.add(i);
            }
        }
    
        // Build the response message with the list of available hours
        StringBuilder response = new StringBuilder("Available hours: ");
        for (int i = 9; i < 18; i++) {
            if (!reservedHours.contains(i)) {
                response.append(i).append(" ");
            }
        }
    
        // Return a 200 OK response with the availability message
        int statusCode = (response.length() > 19) ? 200 : 404;
        return new HttpResponse("200 OK", "text/plain", response.toString());
    }    

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
}

class HttpRequest {
    private String method;
    private String path;
    private String httpVersion;
    private String endpoint;
    private List<NameValuePair> params;

    public HttpRequest(String method, String path, String httpVersion, String endpoint, List<NameValuePair> params) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.endpoint = endpoint;
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public List<NameValuePair> getParams() {
        return params;
    }

    public String getParam(String name) {
        for (NameValuePair param : params) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }
}

class HttpRequestParser {
    public HttpRequest parse(String request) {
        // Split the request into its individual components
        String method, path, httpVersion;
        try {
            String[] requestParts = request.split(" ");
            method = requestParts[0];
            path = requestParts[1];
            httpVersion = requestParts[2];
        } catch (Exception e) {
            return null;
        }

        // Parse the endpoint path to determine which action to take
        String endpoint = "";
        try {
            endpoint = path.split("/")[1];
            endpoint = endpoint.substring(endpoint.indexOf("/") + 1);
            endpoint = endpoint.substring(0, endpoint.indexOf("?"));
        } catch (Exception e) {
            return null;
        }

        // Parse the query string to get the request parameters
        String query = "";
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            query = path.substring(queryIndex + 1);
        }
        List<NameValuePair> params = new ArrayList<>();
        String[] paramPairs = query.split("&");
        for (String paramPair : paramPairs) {
            String[] param = paramPair.split("=");
            if (param.length == 2) {
                params.add(new NameValuePair(param[0], param[1]));
            }
        }

        return new HttpRequest(method, path, httpVersion, endpoint, params);
    }
}

class HttpResponse {
    private String status;
    private String contentType;
    private String body;

    public HttpResponse(String status, String contentType, String body) {
        this.status = status;
        this.contentType = contentType;
        this.body = body;
    }

    @Override
    public String toString() {
        return "HTTP/1.1 " + status + "\r\n" +
               "Content-Type: " + contentType + "\r\n" +
               "Content-Length: " + body.length() + "\r\n" +
               "\r\n" +
               body;
    }
}

class NameValuePair {
    private String name;
    private String value;

    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}

class MongoClientProvider {
    private static MongoClient mongoClient;

    public static MongoClient getMongoClient(String connectionString) {
        if (mongoClient == null) {
            ConnectionString connString = new ConnectionString(connectionString);
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connString).build();
            mongoClient = MongoClients.create(settings);
        }
        return mongoClient;
    }
}
