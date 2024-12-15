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
