import interfaces.ReceiveListener;
import models.Node;
import models.Record;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class Server extends ServerSocket implements ReceiveListener, Runnable {
    private final Record record;
    private final Set<Node> nodes;
    private final Datagram datagram;
    private boolean terminate = false;
    private final Map<Integer, List<String>> responses = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Node>> blacklist = new ConcurrentHashMap<>();

    public Server(int port, Record record, Set<Node> nodes) throws IOException {
        super();

        bind(new InetSocketAddress("localhost", port));

        this.record = record;
        this.nodes = nodes;

        datagram = new Datagram(port, this);

        printGateway();
        System.out.println("Server is running");

        connectWithNodes();

        Thread receiver = new Thread(datagram);
        receiver.start();

        printGateway();
        System.out.println("Ready");

        run();
    }

    private void connectWithNodes() {
        this.nodes.forEach(node -> {
            try {
                Client client = new Client(node);
                client.sendMessage("connect " + getGateway());

                printGateway();
                System.out.println("Response: " + client.receiveMessage());
            } catch (IOException e) {
                System.out.println("Failed to connect with " + node);
            }
        });
    }

    @Override
    public void run() {
        while (!isClosed()) {
            try {
                Socket socket = accept();
                new Thread(() -> {
                    try {
                        Client client = new Client(socket);
                        String command = client.receiveMessage();

                        printGateway();
                        System.out.println("Received command '" + command + "' from " + client.getTarget());

                        Object result = handleRequest(command, client.getPort());

                        client.sendMessage(result);
                        client.close();

                        blacklist.remove(client.getPort());
                        responses.remove(client.getPort());

                        if (terminate) {
                            close();
                            datagram.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (SocketException e) {
                printGateway();
                System.out.println("Server is done accepting clients");
            } catch (IOException e) {
                System.out.println("Error occurred during accepting client");
            }
        }
    }

    @Override
    public void onMessageReceived(InetAddress address, int port, String message) {
        printGateway();
        System.out.print("Received message from " + address.getHostAddress() + ":" + port);
        String[] elements = message.split(" ");
        int id = 0;
        StringBuilder command = new StringBuilder();

        for (int i = 0; i < elements.length; i++) {
            switch (elements[i]) {
                case "-id":
                    id = Integer.parseInt(elements[++i]);
                    blacklist.computeIfAbsent(id, list -> new CopyOnWriteArraySet<>());
                    responses.computeIfAbsent(id, list -> new CopyOnWriteArrayList<>());
                    break;
                case "-blacklist":
                    try {
                        Node node = new Node(elements[++i]);

                        blacklist.get(id).add(node);

                    } catch (UnknownHostException e) {
                        System.out.println("Failed to add node to blacklist");
                    }
                    break;
                case "-request":
                    command = new StringBuilder(elements[++i]);
                    break;
                case "-response":
                    responses.get(id).add(elements[++i]);
                    synchronized (this) {
                        this.notify();
                    }
                    System.out.println(" - '" + elements[i] + '\'');
                    return;
                default:
                    command.append(" ").append(elements[i]);
            }
        }
        System.out.println(" - '" + command + '\'');
        Object answer = handleRequest(command.toString(), id);
        String result = "-id " + id + " -response " + answer;
        datagram.sendMessage(result, address, port);
        System.out.println(result);
        printGateway();
        System.out.println("Sending '" + answer + ", to " + address.getHostAddress() + ":" + port);
        blacklist.remove(id);
        responses.remove(id);
    }

    public void spreadMessage(int id, String command) {

        try {
            Node node = new Node(getGateway());
            blacklist.get(id).add(node);
        } catch (UnknownHostException e) {
            System.out.println("Error occurred during spreading message");
        }

        StringBuilder blacklistFlags = new StringBuilder();

        for (Node node : blacklist.get(id))
            blacklistFlags.append(" -blacklist ").append(node.toString());

        for (Node node : nodes)
            blacklistFlags.append(" -blacklist ").append(node.toString());

        String message = "-id " + id + blacklistFlags + " -request " + command;

        nodes.stream()
                .filter(node -> blacklist.computeIfAbsent(id, k -> new CopyOnWriteArraySet<>()).stream().noneMatch(node::equals))
                .forEach(node -> {
                    printGateway();
                    System.out.println("Resending command to " + node);
                    datagram.sendMessage(message, node.getInetAddress(), node.getPort());
                });
    }

    private void waitForResponses(int id) {
        long askedNodes = nodesToAsk(id);
        long receivedResponses = responses.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>()).size();

        synchronized (this) {
            while (receivedResponses < askedNodes) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                receivedResponses = responses.get(id).size();
            }
        }
    }

    private long nodesToAsk(int id) {
        return nodes.stream()
                .filter(node -> blacklist.computeIfAbsent(id, k -> new CopyOnWriteArraySet<>()).stream().noneMatch(node::equals))
                .count();
    }

    public Object handleRequest(String command, int id) {
        String[] elements = command.split(" ");

        String operation = elements[0];
        String param = "";

        if (elements.length > 1)
            param = elements[1];

        printGateway();
        System.out.println("Performing '" + command + '\'');

        blacklist.computeIfAbsent(id, list -> new CopyOnWriteArraySet<>());
        responses.computeIfAbsent(id, list -> new CopyOnWriteArrayList<>());

        switch (operation) {
            case "get-value":
                return getValue(param, id);
            case "set-value":
                return setValue(param, id);
            case "find-key":
                return findKey(param, id);
            case "get-max":
                return getMax(id);
            case "get-min":
                return getMin(id);
            case "new-record":
                return newRecord(param);
            case "terminate":
                return terminate(id);
            case "connect":
                return connect(param);
            case "disconnect":
                return disconnect(param);
        }

        return "Error";
    }

    private Object getResult(int id) {
        return responses.get(id).stream()
                .filter(response -> !response.equals("Error"))
                .findAny()
                .orElse("Error");
    }

    public Object getValue(String key, int id) {
        if (record.equals(key)) {
            return record.getValue();
        }

        spreadMessage(id, "get-value " + key);
        waitForResponses(id);
        return getResult(id);
    }

    public Object setValue(String param, int id) {
        String key = param.split(":")[0];
        String value = param.split(":")[1];
        if (record.equals(key)) {
            printGateway();
            System.out.println("Setting value");

            record.setValue(value);

            printGateway();
            System.out.println("Value successfully changed to: " + record);

            return "OK";
        }

        spreadMessage(id, "set-value " + param);
        waitForResponses(id);
        return getResult(id);
    }

    public Object findKey(String key, int id) {
        if (record.equals(key)) {
            return getGateway();
        }
        if (nodesToAsk(id) == 0)
            return "Error";

        spreadMessage(id, "find-key " + key);
        waitForResponses(id);
        return getResult(id);
    }

    public Object getMax(int id) {
        if (nodesToAsk(id) == 0)
            return record;

        spreadMessage(id, "get-max");
        waitForResponses(id);

        responses.get(id).add(record.toString());
        return responses.get(id).stream()
                .map(Record::new)
                .max(Comparator.comparingInt(o -> Integer.parseInt(o.getValue())))
                .map(Record::toString)
                .orElse("Error");
    }

    public Object getMin(int id) {
        if (nodesToAsk(id) == 0)
            return record;

        spreadMessage(id, "get-min");
        waitForResponses(id);

        responses.get(id).add(record.toString());
        return responses.get(id).stream()
                .map(Record::new)
                .min(Comparator.comparingInt(o -> Integer.parseInt(o.getValue())))
                .map(Record::toString)
                .orElse("Error");
    }

    public String newRecord(String record) {
        printGateway();
        System.out.println("Changing record: " + this.record);
        this.record.setRecord(record);
        printGateway();
        System.out.println("New record: " + this.record);
        return "OK";
    }

    public Object connect(String gateway) {
        try {
            nodes.add(new Node(gateway));
        } catch (UnknownHostException e) {
            return "Invalid gateway";
        }
        printGateway();
        System.out.println(gateway + " joined to the network");

        return "Connected";
    }

    public Object disconnect(String gateway) {
        nodes.removeIf(node -> {
            try {
                return node.equals(new Node(gateway));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });

        return "Disconnected";
    }

    public Object terminate(int id) {
        spreadMessage(id, "disconnect " + getGateway());
        waitForResponses(id);
        terminate = true;
        return "OK";
    }

    public void printGateway() {
        System.out.print('[' + getGateway() + "]: ");
    }

    public String getGateway() {
        return getInetAddress().getHostAddress() + ":" + getLocalPort();
    }
}