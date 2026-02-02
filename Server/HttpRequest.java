import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest implements Runnable{
    final static String CRLF = "\r\n";
    final static int BUFFER_SIZE = 1024;

    private Socket socket;
    private String myIP;

    public HttpRequest(Socket socket, String myIP){
        this.socket = socket;
        this.myIP = myIP;
    }

    //need for HttpRequest
    public void run(){
        try{
            processRequest();
        }
        catch(Exception e){
            System.err.println("[" + myIP + '] Error processing request: ' + e.getMessage());
        }
    }

    private void processRequest() throws Exception{
        try(InputStream inputStream = socket.getInputStream();
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        BufferedReader requestReader = new BufferedReader(new InputStreamReader(inputStream))
        ){
            String requestLine = requestReader.readLine();

            if(requestLine == null){
                return;
            }

            StringTokenizer tokens = new StringTokenizer(requestLine);
            String method = tokens.nextToken();
            String fileName = tokens.nextToken();
            String httpVersion = tokens.nextToken();

            System.out.println("[" + myIP + "] " + requestLine);

            String headerLine;
            while((headerLine = requestReader.readLine()) != null && !headerLine.isEmpty()){
                //consume headers
            }

            if(fileName.equals("/")){
                fileName = "/index.html";
            }
            fileName = "./www" + fileName;

            File file = new File(fileName);
            boolean fileExists = file.exists() && file.isFile();

            if(fileExists){
                //sendSuccessResponse
            }
        }
    }
}