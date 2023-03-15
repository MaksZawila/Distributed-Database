import models.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class helps with managing the messages between sockets
 * @author Maksymilian Zawiła
 */
public class Client {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    /**
     * Creating a socket which connects to a ServerSocket under given ip and port
     *
     * @param ip   IP address of the ServerSocket
     * @param port port of the ServerSocket
     * @throws IOException if failed to connect or get output stream
     */
    public Client(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    /**
     * Creating a socket which connects to a ServerSocket under given Node object
     * @param node Node object with ServerSocket information
     * @throws IOException if failed to connect or get output stream
     */
    public Client(Node node) throws IOException {
        this.socket = new Socket(node.getInetAddress(), node.getPort());
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    /**
     * Creating a Client object with already connected socket
     *
     * @param socket Socket object already connected to a server
     * @throws IOException if failed to get output stream
     */
    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    /**
     * Send message to the connected socket
     *
     * @param command message that will be sent to the connected socket
     */
    public void sendMessage(Object command) {
        String target = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();

        out.println(command);
        printGateway();
        System.out.println("Sending: '" + command + "', to " + target);

    }

    /**
     * Get message from the connected socket
     *
     * @return message that Socket received
     * @throws IOException when couldn't get the message
     */
    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public int getPort() {
        return socket.getPort();
    }

    public void printGateway() {
        System.out.print("[" + getGateway() + "]: ");
    }

    public String getTarget() {
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    private String getGateway() {
        return socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort();
    }

    /**
     * Close all IO tools, and the socket
     *
     * @throws IOException if failed to close BufferedReader or Socket object
     */
    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
}