import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    private static final Logger logger = Logger.getLogger(ClientGUI.class.getName());

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    private JPanel messagePanel;
    private JTextField messageField;
    private JButton sendButton;
    private JButton imageButton;
    private boolean isRunning = true; // Thread kontrolü için bayrak

    public ClientGUI(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

            // Sunucuya kullanıcı adı gönder
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // JFrame Ayarları
            setTitle("Chat Client");
            setLayout(new BorderLayout());

            // Mesajları göstermek için panel
            messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setBackground(Color.WHITE);

            JScrollPane scrollPane = new JScrollPane(messagePanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(scrollPane, BorderLayout.CENTER);

            // Mesaj alanı ve butonlar
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BorderLayout());
            messageField = new JTextField();
            sendButton = new JButton("Send");
            inputPanel.add(messageField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            imageButton = new JButton("Send Image");
            inputPanel.add(imageButton, BorderLayout.WEST);
            add(inputPanel, BorderLayout.SOUTH);

            // Mesaj gönderme işlemi
            sendButton.addActionListener(e -> sendMessage());

            // Enter tuşuyla mesaj gönderme
            messageField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        sendMessage();
                    }
                }
            });

            // Görüntü gönderme işlemi
            imageButton.addActionListener(e -> sendImage());

            // JFrame ayarları
            setSize(400, 500);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setVisible(true);

        } catch (IOException e) {
            handleError("Bağlantı hatası: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String messageToSend = messageField.getText().trim();
        if (!messageToSend.isEmpty()) {
            try {
                bufferedWriter.write("MESSAGE:" + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                addMessageBubble(username + ": " + messageToSend, true);
                messageField.setText("");
            } catch (IOException e) {
                handleError("Mesaj gönderilirken hata oluştu.");
            }
        }
    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an image to send");
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // File size check (e.g., max 10 MB)
            if (selectedFile.length() > 10 * 1024 * 1024) {
                handleError("File too large. Please select a file smaller than 10 MB.");
                return;
            }
            
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                // Compress image and send it
                ImageIO.write(image, "jpg", baos); 
                byte[] imageBytes = baos.toByteArray();

                // Send metadata and image
                sendMetadata("IMAGE", imageBytes.length);
                sendImageBytes(imageBytes);
                
                // Update UI
                SwingUtilities.invokeLater(() -> addImageBubble(new JLabel(new ImageIcon(image)), username + ": " + selectedFile.getName()));
                
            } catch (IOException e) {
                handleError("Failed to send image: " + e.getMessage());
            }
        }
    }

    private void sendMetadata(String type, int size) throws IOException {
        bufferedWriter.write(type + ":");
        bufferedWriter.newLine();
        bufferedWriter.write(String.valueOf(size));
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void sendImageBytes(byte[] imageBytes) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
        bos.write(imageBytes);
        bos.flush();
    }

    public void listenForMessages() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    String msgFromGroupChat = bufferedReader.readLine();
                    if (msgFromGroupChat == null) throw new IOException("Server bağlantısı kesildi");

                    if (msgFromGroupChat.startsWith("MESSAGE:")) {
                        String messageContent = msgFromGroupChat.substring(8);
                        addMessageBubble(messageContent, false);
                    } else if (msgFromGroupChat.startsWith("IMAGE:")) {
                        int imageSize = Integer.parseInt(bufferedReader.readLine());
                        byte[] imageBytes = new byte[imageSize];
                        InputStream inputStream = socket.getInputStream();

                        int bytesRead = 0;
                        while (bytesRead < imageSize) {
                            int result = inputStream.read(imageBytes, bytesRead, imageSize - bytesRead);
                            if (result == -1) throw new IOException("Görüntü alımı tamamlanamadı.");
                            bytesRead += result;
                        }

                        ImageIcon imageIcon = new ImageIcon(imageBytes);
                        addImageBubble(new JLabel(imageIcon), "Received Image");
                    }
                } catch (IOException e) {
                    handleError("Sunucu bağlantısı kesildi.");
                    closeEverything();
                    break;
                }
            }
        }).start();
    }

    private void addImageBubble(JLabel imageLabel, String senderInfo) {
        ImageIcon originalIcon = (ImageIcon) imageLabel.getIcon();
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(200, 200, Image.SCALE_FAST);
        imageLabel.setIcon(new ImageIcon(scaledImage));

        JPanel messageBubble = new JPanel();
        messageBubble.setLayout(new BorderLayout());

        JLabel senderLabel = new JLabel(senderInfo);
        senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
        messageBubble.add(senderLabel, BorderLayout.NORTH);
        messageBubble.add(imageLabel, BorderLayout.CENTER);

        addToMessagePanel(messageBubble);
    }

    private void addMessageBubble(String message, boolean isUserMessage) {
        JPanel messageBubble = new JPanel();
        messageBubble.setLayout(new BorderLayout());

        JLabel messageLabel = new JLabel("<html><p style='width: 150px;'>" + message + "</p></html>");
        messageLabel.setOpaque(true);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (isUserMessage) {
            messageLabel.setBackground(new Color(173, 216, 230));
            messageBubble.add(messageLabel, BorderLayout.EAST);
        } else {
            messageLabel.setBackground(new Color(211, 211, 211));
            messageBubble.add(messageLabel, BorderLayout.WEST);
        }

        addToMessagePanel(messageBubble);
    }

    private void addToMessagePanel(JPanel messageBubble) {
        messagePanel.add(messageBubble);
        messagePanel.revalidate();
        messagePanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) messagePanel.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void handleError(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, "Hata", JOptionPane.ERROR_MESSAGE);
        System.out.println(errorMessage);
    }

    private void closeEverything() {
        isRunning = false;
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            handleError("Kaynaklar kapatılırken hata oluştu.");
        }
    }

    public static void main(String[] args) {
        String username = JOptionPane.showInputDialog("Kullanıcı adınızı girin:");
        if (username == null || username.trim().isEmpty()) {
            username = "User" + (int) (Math.random() * 1000);
        }
        try {
            Socket socket = new Socket(HOST, PORT);
            ClientGUI client = new ClientGUI(socket, username);
            client.listenForMessages();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Sunucuya bağlanılamadı: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
