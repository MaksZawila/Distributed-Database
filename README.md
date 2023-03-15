# Distributed Database

## About the project:  
The project is an implementation of a distributed database in Java 1.8.  
Nodes communicate with each other using the UDP protocol.  
Client-Server communication is based on TCP protocol.  
The project is multi-threaded, which means that it is possible to communicate  
with multiple clients and servers at the same time.  

## Project Structure:
	.
    ├── compiled              # Directory for compiled files 
    ├── src                   # Source files
    ├── test-scripts          # Scripts used to test the project
    ├── Compiler.bat
    ├── LICENSE
    └── README.md

## Classes:
	.
    ├── Client                # Helper class used for client-server communication
    ├── DatabaseClient        # Main class for a client used for sending requests
    ├── DatabaseNode          # Main class for creating new node
    ├── Datagram              # Helper class for communication between nodes
    ├── Server                # Node representation. All database operations are performed in this class
    ├── Node                  # Model that represents <ip>:<port> of the node that the server is connected to
    └── Record                # Model that represents <key>:<value> which is stored by the server
    
## Interfaces:
	.
	└── ReceiveListener       # Contains the method that is called when new datagram packet's been received

## How to run the program:
The client is launched from the command:

	$ java DatabaseClient -gateway <ip>:<port> -operation <operation>

Available operations:

| Operation                  | Description                                                                   | Success result  | Failure result |
|----------------------------|-------------------------------------------------------------------------------|-----------------|----------------|
| `set-value <key>:<value>`  | Sets `<value>` for specific `<key>`                                           | `"OK"`          | "Error"        |
| `get-value <key>`          | Returns `<value>` for specific `<key>`                                        | `<value>`       | "Error"        |
| `find-key <key>`           | Searches for the address of the node that has a record with the given `<key>` | `<ip>:<port>`   | "Error"        |
| `get-max`                  | Searches for the largest value in the database                                | `<key>:<value>` | None           |
| `get-min`                  | Searches for the smallest value in the database                               | `<key>:<value>` | None           |
| `new-record <key>:<value>` | Sets a new record `<key>:<value>` for the target server                       | `"OK"`          | None           |
| `terminate`                | Removes the server from the node                                              | `"OK"`          | None           |

The server is launched from the command:

	$ java DatabaseNode -tcpport <port> -record <key>:<value> [ -connect <ip>:<port> ]

## Installation:
Run [`Compiler.bat`](https://github.com/MaksZawila/Distributed-Database/blob/main/Compiler.bat) folder and run "Compiler.bat" script to compile the project

## How it works:
When starting the server, DatabaseNode class takes care of every flag that has been passed.
Then it creates new Server object with 3 properties:

* `int port`
* `Record record`
* `Set<Node> nodes`

Server constructor does all the necessary work to create a server to connect to.

1. Creates and binds ServerSocket, and Datagram objects to the specified port.
2. Asigns record and set of nodes to local variables.
3. Connects with all nodes that were specified
4. Starts a Datagram receiver
5. Starts listening on the port for new connections
       
After "Ready" is displayed on the console, the server is ready to accept new clients.

Now if client has connected to the server, and operation from client's been received,
the server starts handling the operation in 
[`handleRequest(String command, int id)`](https://github.com/MaksZawila/Distributed-Database/blob/main/src/Server.java#L196) 
method. Depending on the given type of the operation, the appropriate method is called.
		
If the operation does not require node communication, the result is returned to the client.
However if the opertion does require communication with other nodes, then the server
spreads this operation to all "neighbours", and waits for all the responses.
Message that is sent to the nodes looks like this:

    id <id> [ -blacklist <ip>:<port> ] -operation <operation>

Where `<id>` is related to the port of the client and `-blacklist <ip>:<port>` is
an array of nodes that were already asked, to prevent the query from looping around
the nodes.
		
Nodes constantly receive and process messages from each other in the
[`receiveMessage()`](https://github.com/MaksZawila/Distributed-Database/blob/main/src/Datagram.java#L35)
method, which is executed by the thread until the server is closed.
When the Datagram object receives a packet, it informs the listener of the message received by calling
[`onMessageReceived(InetAddress address, int port, String message)`](https://github.com/MaksZawila/Distributed-Database/blob/1c53b75eac5e2396208559b87ee30ad53aa3659d/src/Server.java#L97). 
This method interprets the flags passed in the message and starts handling the operation in
[`handleRequest(String command, int id)`](https://github.com/MaksZawila/Distributed-Database/blob/main/src/Server.java#L196)
method. Message broadcasting between nodes ends when all the nodes were asked, or the answer
was found.

The nodes respond with a message that looks like this:

    id <id> -response <answer>
		
If the server has received a response from all fo the nodes, then the final response
is sent to the client.

## Copyright
Distributed Database is released under the MIT license. See [LICENSE](https://github.com/MaksZawila/Distributed-Database/blob/main/LICENSE) for details.