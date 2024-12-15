<<<<<<< HEAD
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            // Send username to the server
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error initializing client", e);
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try (Scanner keyboard = new Scanner(System.in)) {
            while (socket.isConnected()) {
                String messageToSend = keyboard.nextLine().trim();
                if (messageToSend.equalsIgnoreCase("/quit")) {
                    System.out.println("Exiting chat...");
                    bufferedWriter.write("/quit");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                } else if (messageToSend.equalsIgnoreCase("/image")) {
                    sendImage();
                } else {
                    if (messageToSend.isEmpty()) {
                        System.out.println("Cannot send an empty message.");
                        continue;
                    }
                    bufferedWriter.write(username + " : " + messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending message", e);
            closeEverything(socket, bufferedReader, bufferedWriter);
            System.exit(0);
        }
    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif"));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
                // Send the IMAGE message type
                bufferedWriter.write("IMAGE:");
                bufferedWriter.newLine();
                bufferedWriter.flush();

                byte[] imageBytes = new byte[(int) selectedFile.length()];
                fileInputStream.read(imageBytes);

                // Send the image size
                bufferedWriter.write(String.valueOf(imageBytes.length));
                bufferedWriter.newLine();
                bufferedWriter.flush();

                // Send the image content
                socket.getOutputStream().write(imageBytes);
                socket.getOutputStream().flush();
                LOGGER.info("Image sent: " + selectedFile.getName());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error sending image", e);
            }
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String msgFromGroupChat;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    if (msgFromGroupChat == null) {
                        throw new IOException("Server connection lost");
                    }
                    if (msgFromGroupChat.startsWith("IMAGE:")) {
                        int imageSize = Integer.parseInt(bufferedReader.readLine());
                        byte[] imageBytes = new byte[imageSize];
                        InputStream inputStream = socket.getInputStream();
                        int bytesRead = 0;
                        while (bytesRead < imageSize) {
                            bytesRead += inputStream.read(imageBytes, bytesRead, imageSize - bytesRead);
                        }
                        // Handle received image (e.g., display or save it)
                        System.out.println("Received an image of size " + imageSize + " bytes.");
                    } else {
                        System.out.println(msgFromGroupChat);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error receiving message", e);
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to close resources", e);
        }
    }

    public static void main(String[] args) {
        try (Scanner keyboard = new Scanner(System.in)) {
            System.out.println("Enter your username: ");
            String username = keyboard.nextLine().trim();
            while (username.isEmpty()) {
                System.out.println("Username cannot be empty. Please enter a valid username: ");
                username = keyboard.nextLine().trim();
            }

            Socket socket = new Socket(HOST, PORT);
            Client client = new Client(socket, username);
            client.listenForMessage();
            client.sendMessage();
        } catch (UnknownHostException e) {
            LOGGER.log(Level.SEVERE, "Host not found", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to connect to the server", e);
        }
    }
}
=======
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            // Send username to the server
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error initializing client", e);
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try (Scanner keyboard = new Scanner(System.in)) {
            while (socket.isConnected()) {
                String messageToSend = keyboard.nextLine().trim();
                if (messageToSend.equalsIgnoreCase("/quit")) {
                    System.out.println("Exiting chat...");
                    bufferedWriter.write("/quit");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                } else if (messageToSend.equalsIgnoreCase("/image")) {
                    sendImage();
                } else {
                    if (messageToSend.isEmpty()) {
                        System.out.println("Cannot send an empty message.");
                        continue;
                    }
                    bufferedWriter.write(username + " : " + messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending message", e);
            closeEverything(socket, bufferedReader, bufferedWriter);
            System.exit(0);
        }
    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif"));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
                // Send the IMAGE message type
                bufferedWriter.write("IMAGE:");
                bufferedWriter.newLine();
                bufferedWriter.flush();

                byte[] imageBytes = new byte[(int) selectedFile.length()];
                fileInputStream.read(imageBytes);

                // Send the image size
                bufferedWriter.write(String.valueOf(imageBytes.length));
                bufferedWriter.newLine();
                bufferedWriter.flush();

                // Send the image content
                socket.getOutputStream().write(imageBytes);
                socket.getOutputStream().flush();
                LOGGER.info("Image sent: " + selectedFile.getName());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error sending image", e);
            }
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String msgFromGroupChat;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    if (msgFromGroupChat == null) {
                        throw new IOException("Server connection lost");
                    }
                    if (msgFromGroupChat.startsWith("IMAGE:")) {
                        int imageSize = Integer.parseInt(bufferedReader.readLine());
                        byte[] imageBytes = new byte[imageSize];
                        InputStream inputStream = socket.getInputStream();
                        int bytesRead = 0;
                        while (bytesRead < imageSize) {
                            bytesRead += inputStream.read(imageBytes, bytesRead, imageSize - bytesRead);
                        }
                        // Handle received image (e.g., display or save it)
                        System.out.println("Received an image of size " + imageSize + " bytes.");
                    } else {
                        System.out.println(msgFromGroupChat);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error receiving message", e);
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to close resources", e);
        }
    }

    public static void main(String[] args) {
        try (Scanner keyboard = new Scanner(System.in)) {
            System.out.println("Enter your username: ");
            String username = keyboard.nextLine().trim();
            while (username.isEmpty()) {
                System.out.println("Username cannot be empty. Please enter a valid username: ");
                username = keyboard.nextLine().trim();
            }

            Socket socket = new Socket(HOST, PORT);
            Client client = new Client(socket, username);
            client.listenForMessage();
            client.sendMessage();
        } catch (UnknownHostException e) {
            LOGGER.log(Level.SEVERE, "Host not found", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to connect to the server", e);
        }
    }
}
>>>>>>> e4335d79664ba83e94294e2af9a017f7e6f1896d
