import java.net.*;
import java.io.*;

public class ConcHTTPAsk {

    public static void main(String[] args) throws IOException {
        try{
            int port = Integer.parseInt(args[0]);
            ServerSocket socket = new ServerSocket(port);
            while(true){
                Socket connectionSocket = socket.accept();
                System.out.println("----- new client -----");
                MyRunnable runnable = new MyRunnable(connectionSocket);
                new Thread(runnable).start();
                System.out.println("----- client thread created -----");
            }

        } catch (IOException e){
            System.err.println("Thread connection error: " + e);
            e.printStackTrace();
        }

    }
    
}
