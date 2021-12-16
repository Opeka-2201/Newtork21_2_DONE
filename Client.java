import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <h1>Client class: create and stop reading and sending Thread</h1>
 * Client store the 2 ThreadPool in a class variable
 * Client store also the SenderBroker, ReaderBroker, BlockingQueue and Socket
 * instance for each new connection.
 * Client class can be seen as the parent of the reading and sending threads.
 * 
 * @author LOUIS Arthur
 * @author LAMBERMONT Romain
 */
public class Client {

    static ExecutorService writingPool;
    static ExecutorService readingPool;
    public SenderBroker sender;
    public ReaderBroker reader;
    public Socket s;
    public BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

    /**
     * Constructeur to create 2 new thread, a BloqkingQueue and store the socket
     * accepted by the Broker.
     */
    public Client(Socket s) throws IOException {
        this.s = s;
        this.sender = new SenderBroker(this);
        this.reader = new ReaderBroker(this);
        readingPool.submit(this.reader);
        writingPool.submit(this.sender);

    }

    /**
     * Initialise the 2 threadPool class variables
     * 
     * @param w
     * @param r
     */
    public static void init(ExecutorService w, ExecutorService r) {
        writingPool = w;
        readingPool = r;
    }

    /**
     * Stop the 2 threads corresponding to the client.
     */
    public void stop() {
        this.sender.stop();
        this.reader.stop();

    }

}
