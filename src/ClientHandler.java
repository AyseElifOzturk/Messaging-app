import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.classfile.BufWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    
    public ClientHandler(Socket socket){
        try{
            this.socket = socket; //Server'dan gönderilen socket, buradaki socket'e eşitlendi
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: "+clientUsername + " has entered the chat!");
        } catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() { //Bu metoddaki her şey ayrı bir thread olarak çalışır
        String messageFromClient;
        while(socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine(); //Bunu ayrı thread olarak yapmamızın sebebi tüm programın durmasını önlemek
                broadcastMessage(messageFromClient);
            } catch(IOException e){
                closeEverything(socket,bufferedReader,bufferedWriter);
                break; //Client disconnect olduğunda çıkacak
            }
        }
    }
    
    public void broadcastMessage(String messageToSend){
        for(ClientHandler clientHandler : clientHandlers){  //for each clienthandler in ArrayList
            try{
                if(!clientHandler.clientUsername.equals(clientUsername)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch(IOException e){
                closeEverything(socket,bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: "+clientUsername+ " left the chat..");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
