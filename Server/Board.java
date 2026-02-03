import java.util.*;

//can only use public on the main class here, think that should be ok
//board class
public class Board {

    //board.Note
    private List<Note> notes;
    //Board.Pin
    private List<Pin> pins;

    

    //variables for the baord
    public int board_width;  // Public so BBoardServer an access
    public int board_height;
    public int note_width;
    public int note_height;
    //hardcode the allowed colours
    private List<String> AcceptColours;
    
    //Board constructor
    public Board(int board_width, int board_height, int note_width, int note_height, List<String> AcceptColours){
        this.board_width = board_width;
        this.board_height = board_height;
        this.note_width = note_width;
        this.note_height = note_height;
        this.AcceptColours = new Vector<>(AcceptColours);
        this.notes = new Vector<>();
        this.pins = new Vector<>();

    }


    //Note class
    public class Note{

        //make these private to avoid any accidential modifications
        private int x, y;
        private String color;
        private String myText;
        private int pinNum;
        //note constructor
        Note(int x, int y, String color, String myText){

            this.x = x;
            this.y = y;
            //if the user messes with caps, program can continue
            this.color = color;
            this.myText = myText;
            //set as not pinned by default
            this.pinNum = 0;
        }

        public int getX(){
            return x;
        }

        public int getY(){
            return y;
        }

        public String getColor(){
            return color;
        }
        public String getText(){
            return myText;
        }

        public boolean isPinned(){
            return pinNum > 0;
        }

        boolean contains(int TX, int TY){
            boolean returnV = TX >= x && TX < x + note_width && TY >= y && TY < y + note_height;

            return returnV;
        }
    }

    //pin class
    public class Pin{
        private int x, y;

        Pin(int x, int y){
            this.x = x;
            this.y = y;
        }

        public int getX(){
            return x;
        }

        public int getY(){
            return y;
        }
    }

    //add note
    //use a bool to tell the HttpRequest.java file if the note meets the criteria to be created, else bad request
    public synchronized void addNote(int x, int y, String color, String myText){

        //check that the note size is > 1 and that it also fits on the board, throw exception if out of bounds
        // Assure error codes match RFC
        if(x < 0 || y < 0 || x + note_width > board_width || y + note_height > board_height){
            
            throw new IllegalArgumentException("OUT_OF_BOUNDS");
        }

        //check if there is overlap on the notes
        for(int i = 0; i < notes.size(); i++){
            Note tempNote = notes.get(i);

            if(tempNote.x == x && tempNote.y == y){
                throw new IllegalArgumentException("COMPLETE_OVERLAP");
            }
        }

        //check for appropiate colour
        if(!AcceptColours.contains(color.toLowerCase())){
            throw new IllegalArgumentException("COLOR_NOT_SUPPORTED");
        }

        notes.add(new Note(x, y, color.toLowerCase(), myText));

    }

    //get note (return an ArrayList of notes)
    public synchronized List<Note> getNotes(){
        return new Vector<>(notes);
    }

    //add pin
    public void addPin(int x, int y){
        boolean returnV = false;
        
        for(int i = 0; i < notes.size(); i++){
            Note tempNote = notes.get(i);

            if(tempNote.contains(x, y)){
                tempNote.pinNum ++;
                returnV = true;

            }
        }
        if(!returnV){
            throw new IllegalArgumentException("NO_NOTE_AT_COORDINATE");
        }
        pins.add(new Pin(x, y));

    }


    //get pin
    public List<Pin> getPins(){
        return new ArrayList<>(pins);
    }

    //remove pin
    public void removePin(int x, int y){
        boolean tempBool = false;

        for(int i = 0; i < pins.size(); i++){
            Pin tempPin = pins.get(i);
            if(tempPin.x == x && tempPin.y == y){
                pins.remove(i);

                tempBool = true;
                break;
            }
        }

        if(!tempBool){
            throw new IllegalArgumentException("PIN_NOT_FOUND");
        }

        //recount the pins up
        for(int i = 0; i < notes.size(); i++){
            notes.get(i).pinNum = 0;

            for(int j = 0; j < pins.size(); j++){
                Pin tempP = pins.get(j);

                if(notes.get(i).contains(tempP.x, tempP.y)){
                    notes.get(i).pinNum++;
                }
            }
        }
    }

    //shake

    //clear
    
}



