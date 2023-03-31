package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {
    
    private boolean shutdown;
    private Integer timeout;
    private Integer limit;
    private static int BUFFERSIZE = 1024;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {

        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;

    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {

        
            //pre-allocate byte buffers for reading/receiving
            byte[] fromServerBuffer = new byte[BUFFERSIZE];
            ByteArrayOutputStream bytesFromServer = new ByteArrayOutputStream();

            //create a client socket method
            Socket clientSocket = new Socket(hostname, port);
        try{

            //send bytes over the open socket
            clientSocket.getOutputStream().write(toServerBytes, 0 , toServerBytes.length);

            //askServer has received a specified max limit of bytes
            //max size is fixed 1024, can be changed
            if(limit == null)limit = BUFFERSIZE;

            //if askServer has not received any data from the server during a specified period or up
            //to a certain time (milliseconds)
            if(timeout != null)clientSocket.setSoTimeout(timeout);

            //askServer initiates a shutdown
            if(shutdown)clientSocket.shutdownOutput();

            //reading from server
            int readBytes;
            while((readBytes = clientSocket.getInputStream().read(fromServerBuffer)) != -1){
                //when server has recived a certain amount of bytes from the server
                if(limit < readBytes){
                    bytesFromServer.write(fromServerBuffer, 0 , limit);
                    clientSocket.close();
                    return bytesFromServer.toByteArray();
                }
                bytesFromServer.write(fromServerBuffer, 0 , readBytes);
            }
            //closing socket and return bytes from server.
            clientSocket.close();
            return bytesFromServer.toByteArray();

        }catch(SocketTimeoutException ex){

            System.err.println("Timeout reached");
            clientSocket.close();
            return bytesFromServer.toByteArray();

        }catch(IOException ex){
            System.err.println("Connection error!");
            throw new IOException(); 
        }

    }
}
