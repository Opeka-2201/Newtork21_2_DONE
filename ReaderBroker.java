import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * ReaderBroker
 */
public class ReaderBroker implements Runnable {

    Socket s;
    BlockingQueue<byte[]> queue;
    
    public ReaderBroker(Socket s, BlockingQueue<byte[]> q){
        this.queue = q;
        this.s = s;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
}