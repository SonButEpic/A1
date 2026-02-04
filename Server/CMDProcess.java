import java.io.*;
import java.net.*;
import java.util.*;

public class CMDProcess implements Runnable{
    
    private Socket mySocket;
    private Board myBoard;
    private String clientIP;

    public CMDProcess(Socket mySocketT, Board myBoardT, String clientIP){
        this.mySocket = mySocketT;
        this.myBoard = myBoardT;
        this.clientIP = clientIP;
    }


    public void run(){
        try(
            BufferedReader requestReader = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
            PrintWriter outputStream = new PrintWriter(mySocket.getOutputStream(), true);
        ){
            StringBuilder sb = new StringBuilder();

            sb.append("BOARD ").append(myBoard.getBoardWidth()).append(" ").append(myBoard.getBoardHeight()).append(" NOTE ").append(myBoard.getNoteWidth()).append(" ").append(myBoard.getNoteHeight()).append(" COLORS");

            List<String> colors = myBoard.getAcceptColours();

            for(int i = 0; i < colors.size(); i++){
                sb.append(" ").append(colors.get(i));
            }

            outputStream.println(sb.toString());

            String clientCMD;
            while((clientCMD = requestReader.readLine()) != null){
                String reply = processCMD(clientCMD);

                if(reply != null){
                    outputStream.println(reply);
                }
            }
        }catch(IOException e){
            System.err.println("[" + clientIP + "] I/O Error: " + e.getMessage());
        }
        finally{
            try{
                mySocket.close();
            }
            catch (IOException e){
                System.err.println("[" + clientIP + "] Error closing socket: " + e.getMessage());
            }
        }
    }

    private String processCMD(String pl) {
        if (pl == null || pl.trim().isEmpty()) {
            return "ERROR EMPTY_COMMAND";
        }
        
        List<String> tokens = new ArrayList<>(Arrays.asList(pl.trim().split("\\s+")));
        String cmd = tokens.get(0).toUpperCase();

        try{
            if(cmd.equals("ADD")){
                if (tokens.size() < 5){
                    return "ERROR INVALID_ADD_FORMAT";
                }

                int x = Integer.parseInt(tokens.get(1));
                int y = Integer.parseInt(tokens.get(2));

                String color = tokens.get(3).toLowerCase();
                String text = String.join(" ", tokens.subList(4, tokens.size()));

                myBoard.addNote(x, y, color, text);
                return "OK NOTE_ADDED";
            }
            else if(cmd.equals("PIN")){

                if(tokens.size() != 3){
                    return "ERROR INVALID_PIN_FORMAT";
                }

                int px = Integer.parseInt(tokens.get(1));
                int py = Integer.parseInt(tokens.get(2));

                myBoard.addPin(px, py);
                return "OK PIN_ADDED";

            }
            else if(cmd.equals("UNPIN")){
                if (tokens.size() != 3){
                    return "ERROR INVALID_UNPIN_FORMAT";
                }

                int px = Integer.parseInt(tokens.get(1));
                int py = Integer.parseInt(tokens.get(2));

                myBoard.removePin(px, py);
                return "OK PIN_REMOVED";

            }
            else if(cmd.equals("SHAKE")){
                myBoard.shake();

                return "OK BOARD_SHAKEN";

            }
            else if(cmd.equals("CLEAR")){
                myBoard.clear();
                return "OK BOARD_CLEARED";

            }
            else if(cmd.equals("GET")){
                if (tokens.size() == 1){
                    
                }

            }
        }
    
}
