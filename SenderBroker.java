import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * SenderBroker
 */
public class SenderBroker implements Runnable{

    Socket s;
    BlockingQueue<byte[]> queue;

    public SenderBroker(Socket next, BlockingQueue<byte[]> queue) {
        this.s = next;
        this.queue = queue;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
    
}