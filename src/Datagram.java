import interfaces.ReceiveListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Datagram extends DatagramSocket implements Runnable {

    private final ReceiveListener listener;

    public Datagram(int port, ReceiveListener listener) throws SocketException {
        super(port);
        this.listener = listener;
    }

    public void sendMessage(String message, InetAddress address, int port) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            send(packet);
        } catch (IOException e) {
            System.out.println("Failed to send a message to node");
            e.printStackTrace();
        }
    }

    public void run() {
        while (!isClosed()) {
            receiveMessage();
        }
    }

    private void receiveMessage() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                receive(packet);
            } catch (SocketException e) {
                Thread.currentThread().interrupt();
                return;
            }
            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            String message = new String(packet.getData(), 0, packet.getLength()).trim();

            if (listener != null) {
                new Thread(()-> {
                    try {
                        listener.onMessageReceived(address, port, message);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}