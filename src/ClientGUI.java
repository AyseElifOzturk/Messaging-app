import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ClientGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    private JPanel messagePanel;
    private JTextField messageField;
    private JButton sendButton;
    private JButton imageButton;

    public ClientGUI(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

            // Set up the JFrame
            setTitle("Chat Client");
            setLayout(new BorderLayout());

            // Message display area (custom JPanel for chat bubbles)
            messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setBackground(Color.WHITE);

            JScrollPane scrollPane = new JScrollPane(messagePanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(scrollPane, BorderLayout.CENTER);

            // Message input field and send button
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BorderLayout());
            messageField = new JTextField();
            sendButton = new JButton("Send");
            inputPanel.add(messageField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            imageButton = new JButton("Send Image");
            inputPanel.add(imageButton, BorderLayout.WEST);
            add(inputPanel, BorderLayout.SOUTH);

            // Send message action
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage();
                }
            });

            // Enter tuşuna basıldığında da mesaj gönder
            messageField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        sendMessage();
                    }
                }
            });

            // Image send action
            imageButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendImage();
                }
            });

            // Set up the JFrame settings
            setSize(400, 500);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String messageToSend = messageField.getText().trim();
        if (!messageToSend.isEmpty()) {
            try {
                // Mesajı "username: message" formatında gönder
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();  // Yeni satır ekleyelim
                bufferedWriter.flush();    // Veriyi gönderelim

                // Debugging: Mesajın başarıyla gönderildiğini yazdır
                System.out.println("Mesaj gönderildi: " + messageToSend);

                // Kullanıcı mesajını ekleyelim
                addMessageBubble(messageToSend, true);
                messageField.setText("");  // Mesaj gönderildikten sonra text kutusunu temizleyelim
            } catch (IOException e) {
                e.printStackTrace(); // Hata durumunda hata yazdıralım
                JOptionPane.showMessageDialog(this, "Mesaj gönderilirken hata oluştu", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Boş mesaj gönderilmeye çalışılırsa uyarı verelim
            System.out.println("Mesaj boş olamaz.");
        }
    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Bir resim seçin");
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                // Resmi uygun boyuta getir
                Image scaledImage = image.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write((BufferedImage) scaledImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                // Resim mesajını sunucuya gönder
                bufferedWriter.write("IMAGE:" + username + " : Resim gönderildi");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                socket.getOutputStream().write(imageBytes); // Resim verisini gönder
                socket.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Resim gönderilirken hata oluştu", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void listenForMessages() {
        new Thread(() -> {
            String msgFromGroupChat;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    if (msgFromGroupChat == null) {
                        throw new IOException("Server bağlantısı kesildi");
                    }

                    // Check for server join messages
                    if (msgFromGroupChat.startsWith("SERVER:")) {
                        // Handle the server message (e.g., "SERVER: ChatGPT joined.")
                        addMessageBubble(msgFromGroupChat, false);
                    } else if (msgFromGroupChat.startsWith("IMAGE:")) {
                        // Handle image messages as before
                        byte[] imageBytes = new byte[1024 * 1024]; // 1MB buffer
                        InputStream inputStream = socket.getInputStream();
                        int bytesRead;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while ((bytesRead = inputStream.read(imageBytes)) != -1) {
                            baos.write(imageBytes, 0, bytesRead);
                        }
                        byte[] imageData = baos.toByteArray();
                        ImageIcon imageIcon = new ImageIcon(imageData);
                        JLabel imageLabel = new JLabel(imageIcon);
                        addImageBubble(imageLabel);
                    } else {
                        // Otherwise, handle normal chat messages
                        addMessageBubble(msgFromGroupChat, false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    private void addImageBubble(JLabel imageLabel) {
        // Resize image (max width and height 200px)
        ImageIcon originalIcon = (ImageIcon) imageLabel.getIcon();
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaledImage));

        // Add image to message bubble
        JPanel messageBubble = new JPanel();
        messageBubble.setLayout(new BorderLayout());
        messageBubble.add(imageLabel, BorderLayout.CENTER);
        messagePanel.add(messageBubble);
        messagePanel.revalidate();
        messagePanel.repaint();

        // Scroll to the bottom to show the latest message/image
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) messagePanel.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void addMessageBubble(String message, boolean isUserMessage) {
        JPanel messageBubble = new JPanel();
        messageBubble.setLayout(new BorderLayout());

        String sender = username;
        String messageContent = message;

        // Eğer mesajda bir ' : ' var ise, mesajı parçalayalım
        if (message.contains(" : ")) {
            String[] messageParts = message.split(" : ", 2);  
            sender = messageParts[0];  
            messageContent = messageParts[1];  
        }

        JLabel messageLabel = new JLabel("<html><p style='width: 150px;'><b>" + sender + "</b>: " + messageContent + "</p></html>");
        messageLabel.setOpaque(true);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Mesajı kullanıcı mesajı mı yoksa alıcıdan mı geldiğine göre hizalayalım
        if (isUserMessage) {
            messageLabel.setBackground(new Color(173, 216, 230)); // Kullanıcı için açık mavi
            messageLabel.setForeground(Color.BLACK);
            messageBubble.add(messageLabel, BorderLayout.EAST); // Sağ tarafa ekleyelim
        } else {
            messageLabel.setBackground(new Color(211, 211, 211)); // Başkası için açık gri
            messageLabel.setForeground(Color.BLACK);
            messageBubble.add(messageLabel, BorderLayout.WEST); // Sol tarafa ekleyelim
        }

        messagePanel.add(messageBubble);
        messagePanel.revalidate();
        messagePanel.repaint();

        // Yeni eklenen mesajı aşağıya kaydırmak için
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) messagePanel.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
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
            e.printStackTrace();
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
            ClientGUI clientGUI = new ClientGUI(socket, username);
            clientGUI.listenForMessages();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
