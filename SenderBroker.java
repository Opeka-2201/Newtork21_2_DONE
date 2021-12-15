import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.io.OutputStream;
import java.io.IOException;

/**
 * SenderBroker
 */
public class SenderBroker implements Runnable{

    Socket s;
    BlockingQueue<byte[]> queue;
    OutputStream out;

    public SenderBroker(Socket next, BlockingQueue<byte[]> queue) throws IOException {
        this.s = next;
        this.queue = queue;
        this.out = this.s.getOutputStream();
    }

    @Override
    public void run() {
        try {
            while(true){    
                s.setTcpNoDelay(true);
                byte[] msg = null;
                try {
                    msg = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                out.write(msg);}
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
    }
}