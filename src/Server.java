<<<<<<< HEAD
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private ServerSocket serverSocket;

    // Constructor expects a ServerSocket
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                LOGGER.info("A new client connected!");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while accepting client connection", e);
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                LOGGER.info("Server socket closed successfully.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while closing server socket", e);
        }
    }

    public static void main(String[] args) {
        try {
            // Create a ServerSocket with the desired port number
            ServerSocket serverSocket = new ServerSocket(12345);

            // Pass the created ServerSocket to the Server constructor
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error starting server", e);
        }
    }
}
=======
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private ServerSocket serverSocket;

    // Constructor expects a ServerSocket
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                LOGGER.info("A new client connected!");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while accepting client connection", e);
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                LOGGER.info("Server socket closed successfully.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while closing server socket", e);
        }
    }

    public static void main(String[] args) {
        try {
            // Create a ServerSocket with the desired port number
            ServerSocket serverSocket = new ServerSocket(12345);

            // Pass the created ServerSocket to the Server constructor
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error starting server", e);
        }
    }
}
>>>>>>> e4335d79664ba83e94294e2af9a017f7e6f1896d
