/**
 * ReaderBroker
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;


public class ReaderBroker implements Runnable {

    Socket s;
    BlockingQueue<byte[]> queue;
    InputStream in;
    String name ;
    public ReaderBroker(Socket s, BlockingQueue<byte[]> q) throws IOException{
        this.queue = q;
        this.s = s;
        this.in = s.getInputStream();
    }

    @Override
    public void run() {
        int length = 0;
        try {
			s.setTcpNoDelay(true);
            byte[] stream = new byte[3000];
            length = this.in.read(stream);
            if (Message.getType(stream, 0)!=1)
                System.out.println("Ca marche");//TODO
            if (!Message.checkConnect(stream))
                System.out.println("C est casse"); //TODO
            s.setSoTimeout(Message.getKeepAlive(stream)*1000);
            this.name = Message.decodeString(stream, 12);
            System.out.println(Message.decodeString(stream, 12));
            byte[] test = new byte[4];
            test[0] = (byte) 32;
            test[1] = (byte) 2;
            test[2] = (byte) 1;
            test[3] = (byte) 0;           
            this.queue.add(test);
            
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        

        
    }
}