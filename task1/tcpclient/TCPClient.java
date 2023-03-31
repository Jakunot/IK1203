package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {

    private static int BUFFERSIZE = 1024;
    
    public TCPClient() {
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {

        try{

            //pre-allocate byte buffers for reading/receiving
            byte[] fromServerBuffer = new byte[BUFFERSIZE];
            ByteArrayOutputStream bytesFromServer = new ByteArrayOutputStream();
            
            //open socket
            Socket clientSocket = new Socket(hostname, port);
            //send bytes over the open socket
            clientSocket.getOutputStream().write(toServerBytes, 0 , toServerBytes.length);

            //reading from server
            int readBytes;
            while((readBytes = clientSocket.getInputStream().read(fromServerBuffer)) != -1){
                bytesFromServer.write(fromServerBuffer, 0 , readBytes);
            }
            //closing socket and return bytes from server.
            clientSocket.close();
            return bytesFromServer.toByteArray();

        }catch(IOException ex){
            System.err.println("Connection error!");
            throw new IOException();    
        }

    }
}
