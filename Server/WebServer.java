import java.io.*;
import java.util.*;
import java.net.*;

public class WebServer{

    //example states it uses volatile for graceful shutdown
    public static volatile boolean run = true;

    //believe that argv is used to store command line arguments passed to the program
    public static void main(String argv[]) throws Exception{
        
        //we need 6 arguments in order to start the server. Port, board_width, board_height, note_width, note_height, color (min 1)
        if(argv.length < 6){
            //used to display an error message as seen in the code sample provided
            //serves as an instruction to the user on the correct way to use the command
            //match format and order to the requirement doc
            System.err.println("Usage: java WebServer <port> <board_width> <board_height> <note_width> <note_height <color1>  ... <colorN");
            System.exit(1);
        }

        //initalize a default port
        int port = 1738;

        if(argv.length > 6){
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
        //parse the commmand line into ints for the server
        int board_width = Integer.parseInt(argv[1]);
        int board_height = Integer.parseInt(argv[2]);
        int note_width = Integer.parseInt(argv[3]);
        int note_height = Integer.parseInt(argv[4]);

        //all else has been parsed, now parse the color(s), initalize an array to store
        List<String> color = new ArrayList<>();

        for(int i = 5; i < argv.length; i++){
            //convert the colors to lowercase to avoid weird errors and have consistency when assigning
            color.add(argv[i].toLowerCase());
        }

        //*****IMPLEMENT THE BOARD OBJECT LATER*****
        myBoard = new Board(board_width, board_height, note_width, note_height, color);

        System.out.println("Starting server on port " + port);

        ServerSocket mySocket = null;
        try{
            //create the server socket
            mySocket = new ServerSocket(port);

            while(run){

                try{
                    Socket clientConnect = mySocket.accept();

                    String myIP = clientConnect.getInetAddress().getHostAddress();

                    HttpRequest myRequest = new HttpRequest(clientConnect, myIP, myBoard);

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