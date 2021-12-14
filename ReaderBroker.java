/**
 * ReaderBroker
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
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
            System.out.println("length connect =" + length);
            if (!Message.checkConnect(stream))
                System.out.println("C est casse"); //TODO
            s.setSoTimeout(Message.getKeepAlive(stream)*1000);
            this.name = Message.decodeString(stream, 12);
            this.queue.add(Message.createConnack(1, 0));
            int count = 0;
            int rm;
            int type;
            int offset;
            String topic;
            String content;
            byte[] packet;
            while(true){
                offset = 0;
                length = this.in.read(stream);
                rm = Message.getRemainingLength(stream, offset);
                packet = Arrays.copyOfRange(stream,offset,offset+rm+2);
                while(length < rm+2){
                    packet = Arrays.copyOfRange(stream, offset, offset + rm + 2);
                    type = Message.getType(stream, offset);
                    switch (type) {
                        case 3:
                            topic = Message.getTopic(packet, offset);
                            Topic.publish(topic, packet);                            
                            break;
                        
                        case 8:
                            
                        
                            break;
                        default:
                            break;
                    }

                }
            }
            
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        

        
    }
}