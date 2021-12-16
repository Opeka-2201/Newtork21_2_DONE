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
    Client client;

    public SenderBroker(Client client) throws IOException {
        
        this.client = client;
        this.s = client.s;
        this.queue = client.queue;
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
                synchronized(this){
                out.write(msg);
                }
            }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
    }

    public void stop() {
        Thread.currentThread().interrupt();
        
    }
}