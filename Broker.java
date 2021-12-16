import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broker {
    public static void main(String[] args) {
        boolean play = true;
        if (args.length != 1) {
            System.out.println("Broker expects : $ java Broker 2xxx (where xxx is a number in range : 2000 -> 2999)");
            System.exit(-1);
        }

        int serverPort = 0;

        try {
            serverPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        if (serverPort > 2999 || serverPort < 2000) {
            System.out.println("Please refer to project statement : range of ports is 2000 -> 2999");
            System.exit(-1);
        }

        ServerSocket server = null;

        try {
            server = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService writingPool = Executors.newFixedThreadPool(1000);
        ExecutorService readingPool = Executors.newFixedThreadPool(1000);
        Client.init(writingPool, readingPool);
        System.out.println("Broker ready");

        while (play) {
            try {
                Socket next = server.accept();
                new Client(next);
            } catch (IOException e) {
                e.printStackTrace();
                play = false;
            }
        }
    }
}
