import java.io.IOException;
import java.net.ConnectException;

public class DatabaseClient {
    public static void main(String[] args) {
        String ip = "";
        int port = 0;
        StringBuilder command = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-gateway":
                    String[] gatewayArray = args[++i].split(":");
                    ip = gatewayArray[0];
                    port = Integer.parseInt(gatewayArray[1]);
                    break;
                case "-operation":
                    command.append(args[++i]);
                    break;
                default:
                    command.append(' ').append(args[i]);
            }
        }
        try {
            Client client = new Client(ip, port);
            client.printGateway();
            System.out.println("Connected to the server " + ip + ":" + port);

            client.sendMessage(command);
            client.printGateway();

            System.out.println("Response from server: " + client.receiveMessage());
            client.close();
        } catch (ConnectException e) {
            System.out.println("Couldn't connect to the server. Check your args, and run it again.");
            System.out.println("The flag should look like this: -gateway <ip>:<port>");
            System.out.println("There is a possibility, that server you are trying to reach is not running.");
        } catch (IOException e) {
            System.out.println("Error occurred");
            e.printStackTrace();
        }
    }
}