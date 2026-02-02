import java.util.*;

//can only use public on the main class here, think that should be ok
//board class
public class Board {

    //board.Note
    private List<Note> notes;
    //Board.Pin
    private List<Pin> pins;

    //variables for the baord
    private int board_width;
    private int board_height;
    private int note_width;
    private int note_height;
    private List<String> color;

    //Board constructor
    public Board(int board_width, int board_height, int note_width, int note_height, List<String> color){
        this.board_width = board_width;
        this.board_height = board_height;
        this.note_width = note_width;
        this.note_height = note_height;
        this.color = color;
        this.notes = new ArrayList<>();
        this.pins = new ArrayList<>();

    }


    //Note class
    public class Note{

        int x, y;
        String color;
        String myText;
        boolean isPinned;
        Note(int x, int y, String color, String myText){
            this.x = x;
            this.y = y;
            this.color = color;
            this.myText = myText;
            this.isPinned = isPinned;
    
        }
    }

    //pin class
    public class Pin{
        int x, y;

        Pin(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
}



