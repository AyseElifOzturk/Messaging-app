import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{
    //Client'ları tutmak ve yeni client geldiğinde yeni thread spawnlama görevini yapar

    private ServerSocket serverSocket; //Yeni gelen bağlantıları dinlemek için kullanılır

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try{
            while(! serverSocket.isClosed()){

                //serverSocket.accept(); 
                //Yeni client gelene kadar program duracak
                //Yeni client bağlanırsa socket objesi return edilir bu sayede client ile iletişim kurulur
                Socket socekt = serverSocket.accept();
                System.out.println("A new client connected!");

                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch(IOException a){

        }
    }

    public void closeServerSocket(){
        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch(IOException a){
            a.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket  = new ServerSocket(1234); //Server, clientları bu porttan dinliyor
        Server server = new Server(serverSocket);
        server.startServer();   
    }
}