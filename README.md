# Assignment 1: Multi-Threaded Bulletin Board System

## Project Overview
This project implements a multi-threaded server that manages a shared board of notes and pins. Client connects to the server, receive a dynamic configuration handshake, and can interact with the board in real time.

### Key Features
* Thread-Safety: Uses  `synchronized` methods in `Board.java` to manage concurrent access from multiple clients.
* Dynamic Handshaking: Clients configure their GUI based on the dimensions and color palettes defined in the sevrer.
* Real-Time Visualization: Auto-refresh and GUI text wrapping is used to ensure notes are displayed correctly and are kept in sync.

-------------------------------------------
## Getting Started
# Compilation
Navigate to the root directory and compile both modules using the Java compiler:

```
# Compile the server
cd Server
javac *.java

# Compile the client
javac *.java
```

# Run the Server
The server requires some specific command line arguments to initialize the board environment: `java WebServer <port> <board_width> <board_height> <note_height> <color1> <color2 ...`
# Example:
```
cd ../A1/Server> java WebServer 1738 800 600 100 80 yellow orange blue
```
# Running the Client
Open a new terminal and run:
```
cd ../A1/Client> java Client
```
In the client, connect to the server using the `localhost` ip and the port: `1738`.
Enter the coordinates or click where you'd like to place your note.
Modify the color and message of the note, and press `POST`.

Pinned notes will persist through a `SHAKE`.
`CLEAR` will empty the board of all notes.
`REFRESH` updates the board with the most recent state.
`DISCONNECT` closes the client.

>[!IMPORTANT]
>***Note: The client is pre-configured to connect to `localhost` on port `8888`.***
------------------------------
# Custom Network Protocol (RFC)
## The system communicates using a custom text-based protocol
`POST x y color msg`: Places a new note at (x, y) if coordinates are valid and there is no overlap.
`GET`: Retrieves all notes that are currently on the board, and their pinned status.
`PIN x y`: Pins and note that contains the (x, y) coordinate, protecting it from `SHAKE`.
`UNPIN x y`: Removes pins from notes at the coordinates specified.
`SHAKE`: Clears all unpinned notes from the board.
`CLEAR`: Resets the board by removing all notes and pins.
`DISCONNECT`: Terminates the client session and closes the socket
------------------------------
## System Architecture
* **WebServer.java**: The entry point that listens for incoming connections and creates `CMDProcess` threads.
* **Board.java**: Handles logic for overlaps, boundary checks, thread-safe note management.
* **Client.java**: Handles socket connection and background listener thread to keep GUI updated.
* **ClientGUI.java**: Swing-based interface that supports mouse interaction for coordinate selection and automatic board refreshing.
------------------------------