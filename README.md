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
java WebServer 8888 800 600 100 80 yellow blue white
```
# Running the Client
Open a new terminal and run:
```
java Client
```
>[!IMPORTANT]
>***Note: The client is pre-configured to connect to `localhost` on port `8888`.***
------------------------------
## Custom Network Protocol (RFC)
The system communicates using a custom text-based protocol
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