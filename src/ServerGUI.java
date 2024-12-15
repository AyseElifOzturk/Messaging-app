import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;

public class ServerGUI {

    private JFrame frame;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private JList<String> clientList;
    private DefaultListModel<String> clientListModel;

    private Server server;
    private Thread serverThread;

    public ServerGUI() {
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);

        topPanel.add(startButton);
        topPanel.add(stopButton);
        frame.add(topPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        frame.add(logScrollPane, BorderLayout.CENTER);

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        JScrollPane clientScrollPane = new JScrollPane(clientList);
        clientScrollPane.setPreferredSize(new Dimension(150, 0));
        frame.add(clientScrollPane, BorderLayout.EAST);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        frame.setVisible(true);
    }

    private void startServer() {
        try {
            // Create a ServerSocket that listens on port 1234
            ServerSocket serverSocket = new ServerSocket(1234);
            
            // Create the Server instance with the created ServerSocket
            server = new Server(serverSocket);

            // Set up the server to update the GUI (through a ServerManager or direct method)
            ServerManager.setServerGUI(this);

            // Start the server in a separate thread
            serverThread = new Thread(() -> {
                log("Server started and listening on port 1234...");
                server.startServer();  // Start accepting client connections
            });
            serverThread.start();

            // Disable start button, enable stop button
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } catch (IOException e) {
            log("Error starting server: " + e.getMessage());
        }
    }


    private void stopServer() {
        if (server != null) {
            server.closeServerSocket();
            if (serverThread != null) {
                serverThread.interrupt();
            }
            log("Server stopped.");
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        clientListModel.clear();  // Clear the client list when server stops
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    // Method to add a client to the client list in the GUI
    public void addClient(String clientUsername) {
        SwingUtilities.invokeLater(() -> clientListModel.addElement(clientUsername));
    }

    // Method to remove a client from the client list in the GUI
    public void removeClient(String clientUsername) {
        SwingUtilities.invokeLater(() -> clientListModel.removeElement(clientUsername));
    }

    // Start the GUI application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}
