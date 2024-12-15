<<<<<<< HEAD
import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.Random;

public class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private static final Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;
    private String clientUsername;
    private boolean isConnected = true;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;

        // Initialize readers and writers
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Assign username safely
        this.clientUsername = bufferedReader.readLine();
        if (clientUsername == null || clientUsername.trim().isEmpty()) {
            clientUsername = "User" + new Random().nextInt(1000); // Assign a random username
        }

        clientHandlers.add(this);
        broadcastMessage("SERVER: " + clientUsername + " sohbete katıldı!");
        LOGGER.info(clientUsername + " joined the chat.");
    }

    @Override
    public void run() {
        try {
            String receivedData;
            while (isConnected && (receivedData = bufferedReader.readLine()) != null) {
                if (receivedData.startsWith("MESSAGE:")) {
                    handleMessage(receivedData.substring(8));
                } else if (receivedData.startsWith("IMAGE:")) {
                    handleImage();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, clientUsername + " disconnected unexpectedly.", e);
        } finally {
            closeEverything();
        }
    }

    private void handleMessage(String message) {
        broadcastMessage(clientUsername + ": " + message);
        LOGGER.info(clientUsername + " sent a message: " + message);
    }

    private void handleImage() {
        try {
            int imageSize = Integer.parseInt(bufferedReader.readLine());
            byte[] imageBytes = new byte[imageSize];
            InputStream inputStream = socket.getInputStream();
            int bytesRead = 0;

            while (bytesRead < imageSize) {
                int result = inputStream.read(imageBytes, bytesRead, imageSize - bytesRead);
                if (result == -1) break;
                bytesRead += result;
            }

            broadcastImageToClients(imageBytes, imageSize);
            LOGGER.info(clientUsername + " sent an image.");
        } catch (IOException | NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Error receiving image from " + clientUsername, e);
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != this && clientHandler.isConnected) {
                try {
                    clientHandler.bufferedWriter.write("MESSAGE:" + message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to send message to " + clientHandler.clientUsername, e);
                    clientHandler.closeEverything();
                }
            }
        }
    }

    private void broadcastImageToClients(byte[] imageBytes, int imageSize) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != this && clientHandler.isConnected) {
                try {
                    clientHandler.bufferedWriter.write("IMAGE:");
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.write(String.valueOf(imageSize));
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                    clientHandler.socket.getOutputStream().write(imageBytes, 0, imageSize);
                    clientHandler.socket.getOutputStream().flush();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to send image to " + clientHandler.clientUsername, e);
                    clientHandler.closeEverything();
                }
            }
        }
    }

    private void closeEverything() {
        if (!isConnected) return;

        isConnected = false;
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " left the chat.");
        LOGGER.info(clientUsername + " has left the chat.");

        try {
            bufferedReader.close();
            bufferedWriter.close();
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error closing resources for client " + clientUsername, e);
        }
    }

    public static void startClientHandler(Socket socket) {
        try {
            ClientHandler clientHandler = new ClientHandler(socket);
            executorService.submit(clientHandler);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start ClientHandler", e);
        }
    }
}
=======
import java.io.*; // Import necessary classes for IO operations
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private static final Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static final ExecutorService executorService = Executors.newCachedThreadPool(); // Thread pool for handling multiple clients
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private boolean isConnected = true;

    // Constructor initializes the client handler with the socket
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Read the username from the first line sent by the client
            this.clientUsername = bufferedReader.readLine();
            
            // Validate the username
            if (clientUsername == null || clientUsername.trim().isEmpty()) {
                clientUsername = "Unknown User";
            }

            clientHandlers.add(this);

            // Notify others that the user has joined
            broadcastMessage("SERVER: " + clientUsername + " joined the chat!");
            LOGGER.info(clientUsername + " joined the chat.");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error initializing client handler", e);
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                String messageType = bufferedReader.readLine();
                if (messageType == null) {
                    break; // Client disconnected unexpectedly
                }

                if (messageType.startsWith("MESSAGE:")) {
                    String message = messageType.substring(8); // Extract message content
                    // Avoid the user receiving their own message
                    broadcastMessage(clientUsername + " : " + message);
                    LOGGER.info(clientUsername + " sent a message: " + message);
                } else if (messageType.startsWith("IMAGE:")) {
                    // Handle image message
                    int imageSize = Integer.parseInt(bufferedReader.readLine());
                    byte[] imageBytes = new byte[imageSize];
                    InputStream inputStream = socket.getInputStream();
                    int bytesRead = 0;
                    while (bytesRead < imageSize) {
                        bytesRead += inputStream.read(imageBytes, bytesRead, imageSize - bytesRead);
                    }

                    // Broadcast the image to all other clients
                    broadcastImageToClients(imageBytes, imageSize);
                    LOGGER.info(clientUsername + " sent an image.");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error reading data from client", e);
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    /**
     * Broadcast a message to all clients except the sender.
     *
     * @param messageToSend The message to send
     */
    public synchronized void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler != this && clientHandler.socket.isConnected()) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error sending message to client " + clientHandler.clientUsername, e);
                closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
            }
        }
    }

    /**
     * Broadcast an image to all clients except the sender.
     *
     * @param imageBytes The image data
     * @param imageSize  The size of the image
     */
    private synchronized void broadcastImageToClients(byte[] imageBytes, int imageSize) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler != this && clientHandler.socket.isConnected()) {
                    // Notify clients about the incoming image
                    clientHandler.bufferedWriter.write("IMAGE:");
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.write(String.valueOf(imageSize));
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();

                    // Send image data
                    clientHandler.socket.getOutputStream().write(imageBytes);
                    clientHandler.socket.getOutputStream().flush();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error sending image to client " + clientHandler.clientUsername, e);
                closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
            }
        }
    }

    /**
     * Remove this client handler from the set and notify others.
     */
    public synchronized void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " left the chat.");
        LOGGER.info(clientUsername + " left the chat.");
    }

    /**
     * Close the connection and streams.
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        if (!isConnected) {
            return;
        }
        isConnected = false;
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            LOGGER.info("Connection with " + clientUsername + " closed.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error closing resources for client " + clientUsername, e);
        }
    }

    /**
     * Start a new ClientHandler thread.
     */
    public static void startClientHandler(Socket socket) {
        ClientHandler clientHandler = new ClientHandler(socket);
        executorService.submit(clientHandler);
    }
}
>>>>>>> e4335d79664ba83e94294e2af9a017f7e6f1896d
