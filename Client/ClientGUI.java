import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ClientGUI extends JFrame{
    // Instance variables
    private PrintWriter out;
    private int boardWidth;
    private int boardHeight;
    private int noteWidth;
    private int noteHeight;
    private DrawingPanel boardPanel;
    private JTextArea logArea;

    private List<ClientNote> notes = new ArrayList<>();
    private Timer refreshTimer;

    // Input fields
    private JTextField xField, yField, msgField;
    private JComboBox<String> colorBox;

    public ClientGUI(PrintWriter out, int width, int height, int noteWidth, int noteHeight, String[] colors){
        // Initialize instance variables
        this.out = out;
        this.boardWidth = width;
        this.boardHeight = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        //**CHANGED FROM "this.noteHeight = noteWidth;" TO "this.noteHeight = noteHeight;"**
        this.noteHeight = noteHeight; 

        //**CHANGED FROM "500" TO "1500" TO SLOW DOWN GETS TO MANUALLY SHOW GET COMMAND**
        refreshTimer = new Timer(1500, e -> sendCommand("GET"));
        refreshTimer.start();

        setTitle("Post Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // The board
        boardPanel = new DrawingPanel();
        boardPanel.setPreferredSize(new Dimension(boardWidth, boardHeight));
        boardPanel.setBackground(new Color(230,230,230));
        add(boardPanel, BorderLayout.CENTER);

        boardPanel.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt){
                // Get coordinates
                int clickX = evt.getX();
                int clickY = evt.getY();

                // Update teh text fields so user can see what coordinates are selected
                xField.setText(String.valueOf(clickX));
                yField.setText(String.valueOf(clickY));
            }
        });


        // Controls
        JPanel controlPanel = setupControls(colors);
        add(controlPanel, BorderLayout.SOUTH);

        // Server Log
        logArea = new JTextArea(20, 30);
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.EAST);

        pack(); // Sets window size to fit components
        setVisible(true);
    }

    private JPanel setupControls(String[] colors){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));

        // Row 1 is inputs
        JPanel inputPanel = new JPanel();
        xField = new JTextField("0", 3);
        yField = new JTextField("0", 3);
        colorBox = new JComboBox<>(colors);
        msgField = new JTextField("Hello World", 15);

        inputPanel.add(new JLabel("X:"));
        inputPanel.add(xField);
        inputPanel.add(new JLabel("Y:"));
        inputPanel.add(yField);
        inputPanel.add(new JLabel("Color:"));
        inputPanel.add(colorBox);
        inputPanel.add(new JLabel("Message:"));
        inputPanel.add(msgField);

        // Row 2 is buttons
        JPanel btnPanel = new JPanel();
        JButton btnPost = new JButton("POST");
        JButton btnGet = new JButton("GET");
        JButton btnPin = new JButton("PIN");
        JButton btnUnpin = new JButton("UNPIN");
        JButton btnClear = new JButton("CLEAR");
        JButton btnShake = new JButton("SHAKE");
        JButton btnDisconnect = new JButton("DISCONNECT");

        // Add action listeners for the buttons
        btnPost.addActionListener(e -> sendCommand("POST " + xField.getText() + " " +
                yField.getText() + " " + colorBox.getSelectedItem() + " " + msgField.getText()));
        btnGet.addActionListener(e -> sendCommand("GET")); // TODO: add parameters
        btnPin.addActionListener(e -> sendCommand("PIN " + xField.getText() + " " + yField.getText()));
        btnUnpin.addActionListener(e -> sendCommand("UNPIN " + xField.getText() + " " + yField.getText()));
        btnShake.addActionListener(e -> sendCommand("SHAKE"));
        btnClear.addActionListener(e -> sendCommand("CLEAR"));
        btnDisconnect.addActionListener(e -> {
            sendCommand("DISCONNECT");
            System.exit(0);
        });

        btnPanel.add(btnPost);
        btnPanel.add(btnGet);
        btnPanel.add(btnPin);
        btnPanel.add(btnUnpin);
        btnPanel.add(btnClear);
        btnPanel.add(btnShake);
        btnPanel.add(btnDisconnect);

        panel.add(inputPanel);
        panel.add(btnPanel);
        return panel;
    }

    // Method to send commands to server.
    private void sendCommand(String command){
        if (out != null){
            out.println(command);
            log("SENT " + command);
        }
    }

    public void handleServerResponse(String response){
        log("SERVER: " + response);

        //Parse and draw note..
        if(response.startsWith("OK")){
            // If we receive OK for a GET, clear the local notes 
            // so we can refresh the board with th enew list
            if(!response.contains("POSTED") && !response.contains("PINNED")){
                notes.clear();
                boardPanel.repaint();
            }
        }
        else if (response.startsWith("NOTE ")){
            // NOTE <x> <y> <color> <message>
            String[] parts = response.split(" ");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            String color = parts[3];
            
            int colorIndex = response.indexOf(color) + color.length();
            int pinnedIndex = response.indexOf("PINNED=");
            String message = response.substring(colorIndex, pinnedIndex).trim();

            boolean pinned = response.endsWith("true");

            notes.add(new ClientNote(x, y, color, message, pinned));
            // Repaint the board
            boardPanel.repaint();
        }

    }

    private void log(String s){
        SwingUtilities.invokeLater(() -> logArea.append(s + "\n"));
    }

    class DrawingPanel extends JPanel{
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color postitYellow = Color.decode("#FFDE21");

            for (ClientNote note : notes){

                switch (note.color.toLowerCase()){
                    case "yellow": g2d.setColor(postitYellow); 
                        break;
                    case "red": g2d.setColor(Color.RED); 
                        break;
                    case "blue": g2d.setColor(Color.BLUE);
                        break;
                    case "green": g2d.setColor(Color.GREEN);
                        break;
                    default: g2d.setColor(Color.WHITE); // Default sticky note color
                }

                // Set note color
                g2d.fillRect(note.x, note.y, noteWidth, noteHeight);

                g2d.setColor(Color.BLACK);
                g2d.drawRect(note.x, note.y, noteWidth, noteHeight);

                // Logic for wrapping text on notes
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                int padding = 5;
                int maxWidth = noteWidth - (padding * 2);
                int x = note.x + padding;
                int y = note.y + fm.getAscent() + padding;

                String[] words = note.message.split(" ");
                StringBuilder line = new StringBuilder();

                for (String word : words){
                    if (fm.stringWidth(line + word) < maxWidth){
                        line.append(word).append(" ");
                    } else {
                        g2d.drawString(line.toString(), x, y);
                        y += fm.getHeight();
                        line = new StringBuilder(word + " ");

                        if (y > note.y + noteHeight - padding){
                            break;
                        }
                    }
                    // Draw last line if there is space.
                    if (y <= note.y + noteHeight - padding){
                        g2d.drawString(line.toString(), x, y);
                    }
                    if (note.isPinned){
                        g2d.fillOval(note.x + (noteWidth / 2) - 5, note.y + 2, 10, 10);
                    }
                }

            //Draw a small pin if the note is pinned (can improve the graphic later)
                if (note.isPinned){
                    g.setColor(Color.BLACK);
                    g.fillOval(note.x + (noteWidth / 2) - 5, note.y + 5, 10, 10);
                }
            }
        }
    }
}

class ClientNote{
    int x, y;
    String color;
    String message;
    boolean isPinned;

    ClientNote(int x, int y, String color, String message, boolean pinned){
        this.x = x;
        this.y = y;
        this.color = color;
        this.message = message;
        this.isPinned = pinned;
     }
} 



