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
	
* set-value \<key>:\<value>  
Success returns: OK  
Failure returns: Error


* get-value \<key>  
    Success returns: \<value>  
    Failure returns: Error


* find-key \<key>  
    Success returns: \<ip>:\<port>  
    Failure returns: Error


* get-max  
    Success returns: \<key>:\<value>


* get-min  
    Success returns: \<key>:\<value>


* new-record \<key>:\<value>  
    Success returns: \<key>:\<value>


* terminate  
    Success returns: OK

The server is launched from the command:

	$ java DatabaseNode -tcpport <port> -record <key>:<value> [ -connect <ip>:<port> ]

## Installation:
Go to the "compiled" folder and run "Compiler.bat" script to compile the project

## How it works:
When starting the server, DatabaseNode class takes care of every flag that has been passed.
Then it creates new Server object with 3 properties:

* int port
* Record record
* Set\<Node> nodes

Server constructor does all the necessary work to create a server to connect to.

* Creates and binds ServerSocket, and Datagram objects to the specified port.
* Asigns record and set of nodes to local variables.
* Connects with all nodes that were specified
* Starts a Datagram receiver
* Starts listening on the port for new connections
       
After "Ready" is displayed on the console, the server is ready to accept new clients.

Now if client has connected to the server, and operation from client's been received,
the server starts handling the operation in handleRequest() method. Depending on
the given type of the operation, the appropriate method is called.
		
If the operation does not require node communication, the result is returned to the client.
However if the opertion does require communication with other nodes, then the server
spreads this operation to all "neighbours", and waits for all the responses.
Message that is sent to the nodes looks like this:

    id <id> [ -blacklist <ip>:<port> ] -operation <operation>

Where `<id>` is related to the port of the client and `-blacklist <ip>:<port>` is
an array of nodes that were already asked, to prevent the query from looping around
the nodes.
		
Nodes constantly receive and process messages from each other 
in the receiveMessage() method, which is executed by the thread until the server is closed.
When the Datagram object receives a packet, it informs the listener of the message
received by calling onMessageReceived(). This method interprets the flags 
passed in the message and starts handling the operation in handleRequest() method.
Message broadcasting between nodes ends when all the nodes were asked, or the answer
was found.

The nodes respond with a message that looks like this:

    id <id> -response <answer>
		
If the server has received a response from all fo the nodes, then the final response
is sent to the client.

## Copyright
Distributed Database is released under the MIT license. See [LICENSE](https://github.com/MaksZawila/Distributed-Database/LICENSE) for details.