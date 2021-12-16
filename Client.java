import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    
    static ExecutorService writingPool;
    static ExecutorService readingPool;
    public SenderBroker sender;
    public ReaderBroker reader;
    public Socket s;
    public BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

    public Client(Socket s) throws IOException{
        this.s = s;
        this.sender = new SenderBroker(this);
        this.reader = new ReaderBroker(this);
        readingPool.submit(this.reader);
        writingPool.submit(this.sender);

        }
    
    public static void init(ExecutorService w, ExecutorService r) {
        writingPool = w;
        readingPool = r;   
    }

    public void stop() {
        this.sender.stop();
        this.reader.stop();
 
    }

}

