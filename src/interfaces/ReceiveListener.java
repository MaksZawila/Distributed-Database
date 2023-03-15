package interfaces;

import java.net.InetAddress;

@FunctionalInterface
public interface ReceiveListener {
    void onMessageReceived(InetAddress address, int port, String message) throws InterruptedException;
}