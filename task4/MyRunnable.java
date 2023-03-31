import tcpclient.TCPClient;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MyRunnable implements Runnable{

    Socket clientSocket;
    public MyRunnable(Socket socket){
        this.clientSocket = socket;
    }

    /* 
    private static boolean shutdown = false; // True if client should shutdown connection
    private static Integer timeout = null; // Max time to wait for data from server (null if no limit)
    private static Integer limit = null; // Max no. of bytes to receive from server (null if no limit)
    private static String hostname = null; // Domain name of server
    private static Integer port = null; // Server port number
    private static String toServer = "";
    private static String HTTP200 = "HTTP/1.1 200 OK\r\n\r\n";
    private static String HTTP404 = "HTTP/1.1 404 Not Found\r\n";
    private static String HTTP400 = "HTTP/1.1 400 Bad Request\r\n";
    private static String ACCEPTED = "ACCEPTED";
    private static int BUFFERSIZE = 1024; 
    */

    
    public void run(){

        boolean shutdown = false; // True if client should shutdown connection
        Integer timeout = null; // Max time to wait for data from server (null if no limit)
        Integer limit = null; // Max no. of bytes to receive from server (null if no limit)
        String hostname = null; // Domain name of server
        Integer port = null; // Server port number
        String toServer = "";
        boolean http_version = false;
        String HTTP200 = "HTTP/1.1 200 OK\r\n\r\n";
        String HTTP404 = "HTTP/1.1 404 Not Found\r\n";
        String HTTP400 = "HTTP/1.1 400 Bad Request\r\n";
        int BUFFERSIZE = 1024;
        
        try{

            byte[] fromClientBuffer = new byte[BUFFERSIZE];
            ByteArrayOutputStream bytesFromClient = new ByteArrayOutputStream();

            // for reading and writing
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            int outputBytes = input.read(fromClientBuffer);

            while (outputBytes != -1) {

                bytesFromClient.write(fromClientBuffer, 0, outputBytes);
                if (new String(fromClientBuffer).contains("HTTP/1.1"))break;

            }
            String serverOutput = new String(bytesFromClient.toByteArray());

            // for example ask?hostname=time.nist.gov&limit=1200&port=13 becomes
            // ["ask", "hostname", "time.nist.gov", "limit", "1200", "port", "13"]
            String[] request = serverOutput.split("[?&=+ ]");

            for (int i = 0; i < request.length; i++) {
                if (request[i].equals("hostname")) {
                    hostname = request[++i];}
                if (request[i].equals("port")) {
                    port = Integer.parseInt(request[++i]);}
                if (request[i].equals("string")) {
                    toServer = request[++i];}
                if (request[i].equals("shutdown")) {
                    shutdown = (request[++i].equals("true"));}
                if (request[i].equals("timeout")) {
                    timeout = Integer.parseInt(request[++i]);}
                if (request[i].equals("limit")) {
                    limit = Integer.parseInt(request[++i]);}
                if(request[i].contains("HTTP/1.1")){
                    http_version = true;
                }
            }

            // for the terminal

            System.out.println("| Port: " + port + " | Hostname: " + hostname + " |Server message: " + toServer);
            System.out.println("| Timeout: " + timeout + " | Shutdown: " + "| Limit: " + limit + "\n");


            if (request[0].equals("GET") && request[1].equals("/ask") && hostname != null && port != 0 && http_version) {
                try{
                    TCPClient tcpClient = new tcpclient.TCPClient(shutdown, timeout, limit);
                    byte[] toServerBytes = toServer.getBytes(StandardCharsets.UTF_8);
                    byte[] serverBytes = tcpClient.askServer(hostname, port, toServerBytes);
                    String serverReturn = new String(serverBytes);

                    System.out.printf("%s:%d says:\n%s", hostname, port, serverReturn);
                    // For non-empty strings, make a linebreak if there isn't one at the end of the
                    // string
                    if (serverReturn.length() > 0 && !serverReturn.endsWith("\n"))
                        System.out.println();

                    System.out.println(HTTP200);
                    output.write(HTTP200.getBytes(StandardCharsets.UTF_8));
                    output.write(serverReturn.getBytes(StandardCharsets.UTF_8));

                } catch (IOException ex) {

                    System.out.println(HTTP404);
                    output.write(HTTP404.getBytes(StandardCharsets.UTF_8));
            }
            }else{
                System.out.println(HTTP400);
                output.write(HTTP400.getBytes(StandardCharsets.UTF_8));

            }
            output.flush();
            clientSocket.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

/* 
    private static String getStatus(String hostname, Integer port, String[] request) {

        if (hostname == null || port == 0)return HTTP400;

        for (int i = 0; i < request.length; i++) {

            if (request[i].equals("GET"))
                break;
            else if (i == (request.length - 1)) {
                return HTTP400;
            }
        }

        for (int i = 0; i < request.length; i++) {

            if (request[i].equals("/ask")) {
                break;
            } else if (i == (request.length - 1)) {
                return HTTP404;
            }
        }
        for (int i = 0; i < request.length; i++) {

            if (request[i].equals("hostname"))
                break;
            else if (i == (request.length - 1)) {
                return HTTP400;
            }
        }
        
        for (int i = 0; i < request.length; i++) {

            if (request[i].equals("port"))
                break;
            else if (i == (request.length - 1)) {
                return HTTP400;
            }
        }

        for (int i = 0; i < request.length; i++) {

            if (request[i].contains("HTTP/1.1"))
                break;
            else if (i == (request.length - 1))
                return HTTP400;
        }

        for (int i = 0; i < request.length; i++) {

            if (request[i].contains("Host:"))
                break;
            else if (i == (request.length - 1)) {
                return HTTP400;
            }
        }

        return ACCEPTED;
    }

    private static int getLimit(String[] request) {

        for (int i = 0; i < request.length; i++)
            if (request[i].equals("limit"))
                limit = Integer.parseInt(request[++i]);

        return limit;
    }

    private static int getTimeout(String[] request) {

        for (int i = 0; i < request.length; i++)
            if (request[i].equals("timeout"))
                timeout = Integer.parseInt(request[++i]);

        return timeout;
    }

    private static boolean getShutdown(String[] request) {

        for (int i = 0; i < request.length; i++)
            if (request[i].equals("shutdown")) {
                shutdown = request[++i].equals("true");
            }

        return shutdown;
    }

    private static String getDataToServer(String[] request) {

        for (int i = 0; i < request.length; i++)
            if (request[i].contains("string"))
                toServer = request[++i];

        return toServer;
    }

    private static int getPort(String[] request) {

        for (int i = 0; i < request.length; i++)
            if (request[i].equals("port"))
                port = Integer.parseInt(request[++i]);

        return port;
    }

    private static String getHost(String[] request) {

        for (int i = 0; i < request.length; i++)
            if (request[i].equals("hostname"))
                hostname = request[++i];

        return hostname;
    }
    */
}
