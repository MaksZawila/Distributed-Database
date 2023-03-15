package models;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Node {
    private final InetAddress address;
    private final int port;

    public Node(String gateway) throws UnknownHostException {
        String[] elements = gateway.split(":");
        this.address = InetAddress.getByName(elements[0].trim());
        this.port = Integer.parseInt(elements[1].trim());
    }

    public InetAddress getInetAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean equals(Node node) {
        return node.getInetAddress().equals(address) && node.getPort() == port;
    }

    @Override
    public String toString() {
        return address.getHostAddress() + ":" + port;
    }
}