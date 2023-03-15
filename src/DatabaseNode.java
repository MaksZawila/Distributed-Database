import models.Node;
import models.Record;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class DatabaseNode {
    private static Record record;
    private static final Set<Node> nodes = new HashSet<>();
    private static int port;

    public static void main(String[] args) {
        handleFlags(args);
        try {
            new Server(port, record, nodes);
        } catch (IOException e) {
            System.out.println("Couldn't create a Server");
            e.printStackTrace();
        }
    }

    public static void handleFlags(String[] args) {
        for (int i = 0; i < args.length; i += 2) {
            String flag = args[i];
            String param = args[i + 1];
            if (flag.equals("-tcpport")) {
                port = Integer.parseInt(param);
            }
            if (flag.equals("-record")) {
                record = new Record(param);
            }
            if (flag.equals("-connect")) {
                try {
                    nodes.add(new Node(param));
                } catch (UnknownHostException e) {
                    System.out.println("Couldn't make InetAddress");
                    e.printStackTrace();
                }
            }
        }
    }
}
