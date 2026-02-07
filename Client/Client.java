import java.io.*;
import java.net.*;
import java.util.Arrays;
import javax.swing.SwingUtilities; // [EDIT] Added for easier array handling

public class Client {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Start GUI in disconnected state
            new ClientGUI(null, 800, 600, 100, 80, new String[]{"yellow", "red", "blue"});
        });
    }

    public static void startConnection(String ip, int port, ClientGUI gui) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(ip, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // 1. Read the handshake
                String response = in.readLine();
                System.out.println("DEBUG: Handshake received: " + response);

                if (response != null && response.startsWith("BOARD")) {
                    try {
                        String[] parts = response.split(" ");
                        
                        // Parse response for board parameters
                        int boardWidth = Integer.parseInt(parts[1]);
                        int boardHeight = Integer.parseInt(parts[2]);
                        
                        int noteWidthIndex = 3;
                        int noteHeightIndex = 4;
                        

                        if (parts.length > 3 && parts[3].equalsIgnoreCase("NOTE")) {
                            noteWidthIndex = 4;
                            noteHeightIndex = 5;
                        }

                        int noteWidth = Integer.parseInt(parts[noteWidthIndex]);
                        int noteHeight = Integer.parseInt(parts[noteHeightIndex]);

                        int colorStartIndex = noteHeightIndex + 1;
                        

                        if (parts.length > colorStartIndex && parts[colorStartIndex].equalsIgnoreCase("COLORS")) {
                            colorStartIndex++;
                        }

                        // Default colors
                        String[] colors = new String[]{"yellow", "blue", "orange"};
                        if (parts.length > colorStartIndex) {
                            colors = Arrays.copyOfRange(parts, colorStartIndex, parts.length);
                        }


                        final int w=boardWidth, h=boardHeight, nw=noteWidth, nh=noteHeight;
                        final String[] c=colors;
                        
                        SwingUtilities.invokeLater(() -> {
                            gui.updateBoardConfig(w, h, nw, nh, c);
                            gui.setOut(out); 
                        });

                        // Start listening for responses
                        String line;
                        while ((line = in.readLine()) != null) {
                            gui.handleServerResponse(line);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        gui.handleServerResponse("ERROR: Parser Crash: " + e.getMessage());
                    }
                } else {
                    gui.handleServerResponse("ERROR: Invalid Handshake (Not BOARD)");
                    socket.close();
                }
            } catch (IOException e) {
                gui.handleServerResponse("ERROR: Connection Failed: " + e.getMessage());
            }
        }).start();
    }
}