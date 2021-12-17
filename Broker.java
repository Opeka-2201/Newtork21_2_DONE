import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <h1>Broker Class: to init the server</h1>
 * This class init all class variables we need for the rest of the program
 * In this we accept socket connection and we delegate the work in the client
 * class
 * 
 * <p>
 * use "java Broker 2xxx" to lanch the broker (xxx is a number)
 * </p>
 * 
 * @author LOUIS Arthur
 * @author LAMBERMONT Romain
 */
public class Broker {
    public static void main(String[] args) {
        boolean play = true;
        if (args.length != 1) {// collect and check argument
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

        try {// instanciate the server socket
            server = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService writingPool = Executors.newFixedThreadPool(1000);// threadPool for writting thread
        ExecutorService readingPool = Executors.newFixedThreadPool(1000);// threadPool for reading thread
        Client.init(writingPool, readingPool);// send the 2 threadPools to class Client
        System.out.println("Broker ready");

        while (play) {// accept every new client
            try {
                Socket next = server.accept();
                new Client(next);// All the method specific to one Client are delegated with the Client class
            } catch (IOException e) {
                e.printStackTrace();
                play = false;
            }
        }
    }
}
