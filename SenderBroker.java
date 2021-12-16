import java.io.OutputStream;
import java.io.IOException;

/**
 * SenderBroker
 */
public class SenderBroker implements Runnable{

    OutputStream out;
    Client client;
    Boolean write;

    public SenderBroker(Client client) throws IOException {
        
        this.client = client;
        this.out = this.client.s.getOutputStream();
        write = true;
    }

    @Override
    public void run() {
        try {
            while(write){    
                byte[] msg = null;
                try {
                    msg = this.client.queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                synchronized(this){
                    this.out.write(msg);
                }
            }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
    }

    public void stop() {
        this.write = false;
        Thread.currentThread().interrupt();
    }
}