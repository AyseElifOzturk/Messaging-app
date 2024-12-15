<<<<<<< HEAD
public class ServerManager {
    private static ServerGUI serverGUI;

    public static void setServerGUI(ServerGUI gui) {
        serverGUI = gui;
    }

    public static void updateClientList(String clientUsername, boolean isAdding) {
        if (isAdding) {
            serverGUI.addClient(clientUsername);
        } else {
            serverGUI.removeClient(clientUsername);
        }
    }
}
=======
public class ServerManager {
    private static ServerGUI serverGUI;

    public static void setServerGUI(ServerGUI gui) {
        serverGUI = gui;
    }

    public static void updateClientList(String clientUsername, boolean isAdding) {
        if (isAdding) {
            serverGUI.addClient(clientUsername);
        } else {
            serverGUI.removeClient(clientUsername);
        }
    }
}
>>>>>>> e4335d79664ba83e94294e2af9a017f7e6f1896d
