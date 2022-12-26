import java.io.*;
import java.net.*;

public class BaskTest {
    public static void main(String[] args) throws IOException {
        // Set the port number
        int port = 8003;

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
            String[] requestParts = request.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];
            String httpVersion = requestParts[2];

            // Print out the request details
            System.out.println("Received request:");
            System.out.println("Method: " + method);
            System.out.println("Path: " + path);
            System.out.println("HTTP Version: " + httpVersion);

            // Send the HTTP response to the client
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println("");
            out.println("<h1>Hello, World!</h1>");

            // Close the connection
            clientSocket.close();
        }
    }
}
