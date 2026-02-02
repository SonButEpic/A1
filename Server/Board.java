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
    //hardcode the allowed colours
    private static final List<String> AcceptColours = Arrays.asList("red", "orange", "yellow", "green", "blue", "purple");
    
    //Board constructor
    public Board(int board_width, int board_height, int note_width, int note_height){
        this.board_width = board_width;
        this.board_height = board_height;
        this.note_width = note_width;
        this.note_height = note_height;
        this.notes = new ArrayList<>();
        this.pins = new ArrayList<>();

    }


    //Note class
    public class Note{

        //make these private to avoid any accidential modifications
        private int x, y;
        private String color;
        private String myText;
        private boolean isPinned;
        //note constructor
        Note(int x, int y, String color, String myText){

            if(!AcceptColours.contains(color.toLowerCase())){
                throw new IllegalArgumentException("Invalid color, please use one of the follwoing: red, orange, yellow, green, blue, purple");
            }

            this.x = x;
            this.y = y;
            //if the user messes with caps, program can continue
            this.color = color.toLowerCase();
            this.myText = myText;
            //set as not pinned by default
            this.isPinned = false;
    
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

    //add note
    //use a bool to tell the HttpRequest.java file if the note meets the criteria to be created, else bad request
    public boolean addNote(int x, int y, String color, String myText){
        boolean returnV = true;

        //check that the note size is > 1 and that it also fits on the board
        if(x < 0 || y < 0 || x + note_width > board_width || y + note_height > board_height){
            
            returnV = false;
            return returnV;
        }

        //check if there is overlap on the notes
        for(int i = 0; i < notes.size(); i++){
            Note tempNote = notes.get(i);

            if(tempNote.x == x && tempNote.y == y){
                returnV = false;
                return returnV;
            }
        }

        notes.add(new Note(x, y, color, myText));

        return returnV;

    }

    //get note (return an ArrayList of notes)
    public List<Note> getNotes(){
        return new ArrayList<>(notes);
    }

    //add pin
    public boolean addPin(int x, int y){
        boolean returnV = false;
        
        for(int i = 0; i < notes.size(); i++){
            Note tempNote = notes.get(i);

            if(x >= tempNote.x && x < tempNote.x + note_width && y >= tempNote.y && y < tempNote.y + note_height){
                tempNote.isPinned = true;
                pins.add(new Pin(x, y));

                returnV = true;
                return returnV;
            }
        }

        return returnV;
    }


    //get pin

    //remove pin

    //shake

    //clear
}



