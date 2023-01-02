import java.net.ServerSocket;
import java.net.Socket;

public class ActivityServer {
    public static void main(String[] args) throws Exception {
        // Set the port number
        int port = 8084;

        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Listening for connections on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();

            RequestHandler requestHandler = new RequestHandler();

            requestHandler.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        requestHandler.handleRequest(clientSocket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
