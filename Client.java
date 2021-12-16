import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    
    static ExecutorService writingPool;
    static ExecutorService readingPool;
    public Socket s;
    public BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

    public Client(Socket s) throws IOException{
        this.s = s;
        SenderBroker sender = new SenderBroker(this.s, this.queue);
        readingPool.submit(sender);
        writingPool.submit(new ReaderBroker(this.s, this.queue, sender));

        }
    
    public static void init(ExecutorService w, ExecutorService r) {
        writingPool = w;
        readingPool = r;   
    }

}

