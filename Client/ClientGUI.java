import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

    private List<ClientNote> notes = new CopyOnWriteArrayList<>();
    private Timer refreshTimer;

    // Input fields
    private JTextField xField, yField, msgField;
    private JComboBox<String> colorBox;

    private JTextField ipInput, portInput;

    public ClientGUI(PrintWriter out, int width, int height, int noteWidth, int noteHeight, String[] colors){
        try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {

        }
        // Initialize instance variables
        this.out = out;
        this.boardWidth = width;
        this.boardHeight = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        
        // Refresh delay of 1.5 seconds to balance responsiveness and server load. Can adjust as needed.
        refreshTimer = new Timer(1500, e -> sendCommand("GET"));
        if (this.out != null){
            refreshTimer.start();
        }
        setTitle("Post Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // The board
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        JPanel connPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ipInput = new JTextField("localhost", 10);
        portInput = new JTextField("1738", 5);
        JButton connectBtn = new JButton("Connect");
        connPanel.add(new JLabel("Server IP:"));
        connPanel.add(ipInput);
        connPanel.add(new JLabel("Port:"));
        connPanel.add(portInput);
        connPanel.add(connectBtn);

        connectBtn.addActionListener(e -> {
            Client.startConnection(ipInput.getText(), Integer.parseInt(portInput.getText()), this);
        });

        JPanel actionBtnPanel = setupActionButtons();
        topContainer.add(connPanel);
        topContainer.add(actionBtnPanel);
        add(topContainer, BorderLayout.NORTH);

        boardPanel = new DrawingPanel();
        boardPanel.setPreferredSize(new Dimension(boardWidth, boardHeight));
        boardPanel.setBackground(new Color(230, 230, 230));
        add(new JScrollPane(boardPanel), BorderLayout.CENTER);

        boardPanel.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e){
                xField.setText(String.valueOf(e.getX()));
                yField.setText(String.valueOf(e.getY()));
            }
        });

        // Note details & collapsible log
        logArea = new JTextArea(20, 25);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Server Log"));
        logScroll.setMinimumSize(new Dimension(200, 0));
        add(logScroll, BorderLayout.EAST);

        add(setupNoteInputs(colors), BorderLayout.SOUTH);

        pack();
        setVisible(true);

    }
    // Provide connection after pressing connect button
    public void setOut(PrintWriter out) {
        this.out = out;
        if (this.out != null) {
            refreshTimer.start();
        }
        log("Connected successfully. Board sync started.");
    }

    // Update dimensions based on sevrer handshake
    public void updateBoardConfig(int width, int height, int noteWidth, int noteHeight, String[] colors){
        this.boardWidth = width;
        this.boardHeight = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;

        boardPanel.setPreferredSize(new Dimension(boardWidth, boardHeight));
        colorBox.setModel(new DefaultComboBoxModel<>(colors));

        // Refresh window layout
        this.pack();
        boardPanel.revalidate();
        boardPanel.repaint();

    }

    // Helper for the top action buttons
    private JPanel setupActionButtons(){
        JPanel actionBtnPanel = new JPanel();
        JButton btnGet = new JButton("REFRESH");
        JButton btnShake = new JButton("SHAKE");
        JButton btnClear = new JButton("CLEAR");
        //JButton btnGetCoord = new JButton("GET C");  Optional Get Coord button 
        JButton btnDisconnect = new JButton("DISCONNECT");
        JButton btnPin = new JButton("PIN");
        JButton btnUnpin = new JButton("UNPIN");

        btnGet.addActionListener(e -> sendCommand("GET"));
       // btnGetCoord.addActionListener(e -> sendCommand("GET " + xField.getText() + " " + yField.getText()));
        btnShake.addActionListener(e -> sendCommand("SHAKE"));
        btnClear.addActionListener(e -> sendCommand("CLEAR"));
        btnDisconnect.addActionListener(e ->{
            sendCommand("DISCONNECT");
            System.exit(0);
        });

        btnPin.addActionListener(e -> sendCommand("PIN " + xField.getText() + " " + yField.getText()));
        btnUnpin.addActionListener(e -> sendCommand("UNPIN " + xField.getText() + " " + yField.getText()));

        actionBtnPanel.add(btnGet);
       // actionBtnPanel.add(btnGetCoord);
        actionBtnPanel.add(btnShake);
        actionBtnPanel.add(btnClear);
        actionBtnPanel.add(btnDisconnect);
        actionBtnPanel.add(btnPin);
        actionBtnPanel.add(btnUnpin);
        return actionBtnPanel;
    }

    // Helper for note inputs at the bottom
    private JPanel setupNoteInputs(String[] colors){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xField = new JTextField("0", 3);
        yField = new JTextField("0", 3);
        colorBox = new JComboBox<>(colors);
        msgField = new JTextField("Hello World", 15);
        JButton btnPost = new JButton("POST");

        btnPost.addActionListener(e -> sendCommand("POST " + xField.getText() + " " +
                yField.getText() + " " + colorBox.getSelectedItem() + " " + msgField.getText()));

        panel.add(new JLabel("X:"));
        panel.add(xField);
        panel.add(new JLabel("Y:"));
        panel.add(yField);
        panel.add(new JLabel("Color:"));
        panel.add(colorBox);
        panel.add(new JLabel("Message:"));
        panel.add(msgField);
        panel.add(btnPost);
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
        
        SwingUtilities.invokeLater(() -> {
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
        });
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
            Color postitBlue = Color.decode("#a6ccf5");
            Color postitOrange = Color.decode("#F4B416");


            for (ClientNote note : notes){
                switch (note.color.toLowerCase()){
                    case "yellow": g2d.setColor(postitYellow);  break;
                    case "orange": g2d.setColor(postitOrange);  break;
                    case "blue": g2d.setColor(postitBlue); break;
                    case "green": g2d.setColor(Color.GREEN); break;
                    default: g2d.setColor(Color.WHITE); // Default sticky note color
                }

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
                }
                // Draw last line if there is space.
                if (y <= note.y + noteHeight - padding){
                    g2d.drawString(line.toString(), x, y);
                }

            //Draw a small pin if the note is pinned (can improve the graphic later)
                if (note.isPinned){
                    g2d.setColor(new Color (255, 0, 0, 180)); // Semi-transparent red for pin
                    g2d.fillOval(note.x + (noteWidth / 2) - 5, note.y + 5, 10, 10);
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



