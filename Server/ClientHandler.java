import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable{
    private Socket socket;
    private Board board;

    public ClientHandler (Socket socket, Board board){
        this.socket = socket;
        this.board = board;
    }

    @Override
    public void run(){
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)         
        ){
            String line;
            // Loop keeps the connection alive for the GUI
            while ((line = in.readLine()) != null){
                // Process server commands here
                String[] parts = line.split(" ");
                if (parts.length == 0){
                    continue;
                }

                String command = parts[0].toUpperCase();

                try {
                    switch (command){
                        case "POST":
                            handlePost(parts, line, out);
                            break;
                        case "GET":
                            handleGet(out);
                            break;
                        case "PIN":
                            handlePin(parts, out);
                            break; 
                        case "UNPIN":
                            handleUnpin(parts, out);
                            break;
                        case "CLEAR":
                            handleClear(out);
                            break;
                        case "SHAKE":
                            handleShake(out);
                            break;
                        case "DISCONNECT":
                            socket.close();
                            return;
                        default:
                            out.println("ERROR UNKONW_COMMAND");
                    }
                    
                } catch (Exception e) {
                    // Sends error message to GUI log
                    out.println("ERROR " + e.getMessage());
                }

            }
            
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }

    // Handle the POST command
    private void handlePost(String[] parts, String rawLine, PrintWriter out){
        // Make sure the GUI sent enough parameters
        if (parts.length < 5){
            out.println("ERROR INVALID_FORMAT");
            return;
        }

        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        String color = parts[3];

        // TODO: Extract the message for the note (After color)
        int colorPos = rawLine.indexOf(color);
        String text = rawLine.substring(colorPos + color.length()).trim();

        // TODO: Make sure addNote in Board.javahandles coordinate and colors correctly.
        board.addNote(x, y, color, text);
        out.println("OK NOTE_ADDED");
    }

    private void handleGet(PrintWriter out){
        // TODO: CHRISTIAN: Make usre getNotes() returns a new lsit of current notes
        List<Board.Note> notes = board.getNotes();

        // TODO: GABRIEL: Send count first so the GUI knows how many to expect
        out.println("OK "+ notes.size());

        for (Board.Note n : notes){
            // Format matches the GUIs handleServerResponse parser
            out.println(String.format("NOTE %d %d %s %s", 
                n.getX(), n.getY(), n.getColor(), n.getText()));
        }
    }

    private void handlePin(String[] parts, PrintWriter out){
        if (parts.length < 3){
            out.println("ERROR INVALID_PIN_FORMAT");
            return;
        }
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);

        // TODO: Implement addPin(x, y) in Board.java
        board.addPin(x, y);
        out.println("OK PINNED");
    }

    private void handleUnpin(String[] parts, PrintWriter out){
        if (parts.length < 3){
            out.println("ERROR INVALID_UNPIN_FORMAT");
            return;
        }
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);

        // TODO: Implement unpin(x, y) in Board.java
        //board.unpin(x, y);
        out.println("OK UNPINNED");
    }
    // TODO: Implement CLEAR command
    private void handleClear(PrintWriter out){

    }

    private void handleShake(PrintWriter out){
        board.shake();
        out.println("OK SHAKE_COMPLETE");
    }


}
