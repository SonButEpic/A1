import java.io.*;
import java.net.*;
import javax.swing.SwingUtilities;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 8888; 

        try {
            Socket socket = new Socket(serverAddress, port);
            System.out.println("Connected to server.");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // read the first line
            String response = in.readLine();
            
            // Combine the logic checks
            if (response != null && response.startsWith("BOARD")) {
                String[] parts = response.split(" ");
                
                int boardWidth = Integer.parseInt(parts[1]);
                int boardHeight = Integer.parseInt(parts[2]);
                int colorIndexStart = 7; 
                
                String[] colors = new String[parts.length - colorIndexStart];
                System.arraycopy(parts, colorIndexStart, colors, 0, colors.length);

                System.out.println("Board configured: " + boardWidth + "x" + boardHeight);

                // Launch GUI on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    ClientGUI gui = new ClientGUI(out, boardWidth, boardHeight, colors);

                    // Start the background listener thread
                    new Thread(() -> {
                        try {
                            String line;
                            while ((line = in.readLine()) != null) {
                                gui.handleServerResponse(line);
                            }
                        } catch (IOException e) {
                            System.out.println("Connection lost: " + e.getMessage());
                        }
                    }).start(); 
                });
            } else {
                System.out.println("Invalid handshake from server: " + response);
                socket.close();
            }

        } catch (IOException e) {
            System.out.println("Could not connect: " + e.getMessage());
        }
    }
}