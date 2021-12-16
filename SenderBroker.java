import java.io.OutputStream;
import java.io.IOException;

/**
 * <h1>Sender Broker: send the message in the queue</h1>
 * This class is quit simple. It's responsible to send the message in the
 * Bloquing queue.
 * So to send a message, just put it in the queue store in Client class
 * 
 * @author LOUIS Arthur
 * @author Lambermont Romain
 */
public class SenderBroker implements Runnable {

    OutputStream out;
    Client client;
    Boolean write;

    /**
     * Constructor for Sender Broker Object. All informations needed is store in
     * Client.
     * 
     * @param client
     * @throws IOException
     */
    public SenderBroker(Client client) throws IOException {

        this.client = client;
        this.out = this.client.s.getOutputStream();
        write = true;
    }

    @Override
    public void run() {
        try {
            while (write) {
                byte[] msg;
                try {
                    msg = this.client.queue.take(); // take the first element and wait if the queue is empty
                    synchronized (this) {
                        this.out.write(msg);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * This function is responsible to stop sending and interrupt the Thread
     */
    public void stop() {
        this.write = false;
        Thread.currentThread().interrupt();
    }
}