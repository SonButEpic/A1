import java.io.*;
import java.util.*;
import java.net.*;

public class WebServer{

    //example states it uses volatile for graceful shutdown
    public static volatile boolean run = true;

    //believe that argv is used to store command line arguments passed to the program
    public static void main(String argv[]) throws Exception{
        
        //if no command line arguments are provided display an error message and end program
        if(argv.length == 0){
            //used to display an error message as seen in the code sample provided
            //serves as an instruction to the user on the correct way to use the command
            System.err.println("Usage: java WebServer <port>");
            System.exit(1);
        }

        //initalize a default port
        int port = 1738;

        if(argv.length > 0){
            try{
                port = Integer.parseInt(argv[0]);

                if(port < 1 || port > 65535){
                    System.err.println("Error: Port must be between 1 and 65535. Using default port 1738 instead.");
                        port = 1738;

                }
            }
            catch(NumberFormatException e){
                System.err.println("Invalid port number. using default port number 1738.");
                port = 1738;
            }
        }

        System.out.println("Starting server on port " + port);

        ServerSocket mySocket = null;
        try{
            //create the server socket
            mySocket = new ServerSocket(port);

            while(run){

                try{
                    Socket clientConnect = mySocket.accept();

                    String myIP = clientConnect.getInetAddress().getHostAddress();

                    HttpRequest myRequest = new HttpRequest(clientConnect, myIP);

                    Thread thread = new Thread(myRequest);

                    thread.start();
                }
                catch(SocketException e){
                    if(run){
                        System.err.println("Socket error: " + e.getMessage());
                    }
                }
            }
        
        }
        catch(BindException e){
            System.err.println("Error: Port " + port + " is already in use.");
            System.exit(1);
        }
        catch(IOException e){
            System.err.println("Error: creating socket: " + e.getMessage());
            System.exit(1);
        }
        finally{
            if(mySocket != null && !mySocket.isClosed()){
                try{
                    mySocket.close();
                    System.out.println("\nWebServer stopped");
                }
                catch(IOException e){
                    System.out.println("Error closing ServerSocket: " + e.getMessage());
                }
            }
        }

    }
    public static void shutdown(){
        run = false;
    }
}