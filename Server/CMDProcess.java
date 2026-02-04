import java.io.*;
import java.net.*;
import java.util.*;

public class CMDProcess implements Runnable{
    
    private Socket mySocket;
    private Board myBoard;
    private String clientIP;

    //CMDProcess constructor
    public CMDProcess(Socket mySocketT, Board myBoardT, String clientIP){
        //socket used for client connection
        this.mySocket = mySocketT;
        //Board object
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
            return "ERROR INVALID_FORMAT";
        }
        
        List<String> tokens = new ArrayList<>(Arrays.asList(pl.trim().split("\\s+")));
        String cmd = tokens.get(0).toUpperCase();

        try{
            if(cmd.equals("POST")){
                if (tokens.size() < 5){
                    return "ERROR INVALID_FORMAT Missing parameters or invalid data types";
                }

                int x, y;
                try{

                    x = Integer.parseInt(tokens.get(1));
                    y = Integer.parseInt(tokens.get(2));

                }
                catch(NumberFormatException e){
                    return "ERROR INVALID_FORMAT Command parameters must be integers";
                }

                String color = tokens.get(3).toLowerCase();
                String text = String.join(" ", tokens.subList(4, tokens.size()));

                myBoard.addNote(x, y, color, text);
                return "OK NOTE_POSTED";
            }
            else if(cmd.equals("PIN")){

                if(tokens.size() != 3){
                    return "ERROR INVALID_FORMAT Command is missing parameters or using invalid data types.";
                }

                int x, y;

                try{

                    x = Integer.parseInt(tokens.get(1));
                    y = Integer.parseInt(tokens.get(2));

                }
                catch(NumberFormatException e){
                    return "ERROR INVALID_FORMAT Command parameters must be integers";

                }

                myBoard.addPin(x, y);
                return "OK PIN_ADDED";

            }
            else if(cmd.equals("UNPIN")){
                if (tokens.size() != 3){
                    return "ERROR INVALID_FORMAT Command is missing parameters or using invalid data types.";
                }

                int x,y;

                try{                

                    x = Integer.parseInt(tokens.get(1));
                    y = Integer.parseInt(tokens.get(2));

                }
                catch(NumberFormatException e){
                    return "ERROR INVALID_FORMAT Command parameters must be integers";
                }

                myBoard.removePin(x, y);
                return "OK PIN_REMOVED";

            }
            else if(cmd.equals("SHAKE")){
                if(tokens.size() != 1){
                    return "ERROR INVALID_FORMAT SHAKE command takes no parameters";
                }

                myBoard.shake();
                return "OK SHAKE_COMPLETE";

            }
            else if(cmd.equals("CLEAR")){
                
                if(tokens.size() != 1){
                    return "ERROR INVALID_FORMAT CLEAR command takes no parameters";
                }
                
                myBoard.clear();
                return "OK BOARD_CLEARED";

            }
            else if(cmd.equals("GET")){
                if (tokens.size() == 1){
                    List<Board.Note> notes = myBoard.getNotes();

                    StringBuilder sb = new StringBuilder();

                    sb.append("OK ").append(notes.size()).append("\n");

                    for(int i = 0; i < notes.size(); i++){
                        Board.Note n = notes.get(i);

                        sb.append("NOTE ").append(n.getX()).append(" ").append(n.getY()).append(" ").append(n.getColor()).append(" ").append(n.getText()).append(" PINNED=").append(n.isPinned() ? "true" : "false").append("\n");

                    }
                    return sb.toString().trim();
                }
                else if(tokens.get(1).equalsIgnoreCase("PINS")){
                    List<Board.Pin> pins = myBoard.getPins();

                    StringBuilder sb = new StringBuilder();

                    sb.append("OK ").append(pins.size()).append("\n");

                    for(int i = 0; i < pins.size(); i++){
                        Board.Pin p = pins.get(i);

                        sb.append("PIN ").append(p.getX()).append(" ").append(p.getY()).append("\n");
                    }
                    return sb.toString().trim();
                }
                else{
                    return "ERROR INVALID_FORMAT GET command unknown filter";
                }

            }
            else if(cmd.equals("DISCONNECT")){
                if(tokens.size() != 1){
                    return "ERROR INVALID_FORMAT DISCONNECT command takes no parameters";
                }
                try{
                    mySocket.close();
                }
                catch(IOException e){}
                return null;
            }
            else{
                return "ERROR UNKNOWN_COMMAND";

            }
        }
        catch(IllegalArgumentException e){
            return e.getMessage();
        }
        catch(Exception e){
            return "ERROR " + e.getMessage();
        }

    }
}
