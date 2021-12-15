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
            String[] topicLs;
            while(true){
                offset = 0;
                length = this.in.read(stream);
                int read = 0;
                
                while(length >read){
                    rm = Message.getRemainingLength(stream,read);
                    packet = Arrays.copyOfRange(stream, read, read + rm + 2);
                    type = Message.getType(packet);
                    System.out.println("name = " + this.name + "| type =" + type);

                    switch (type) {
                        case 3:
                            topic = Message.getTopic(packet);
                            System.out.println("publish received");
                            Topic.publish(topic, packet);                            
                            break;
                        
                        case 8:
                            System.out.println("subscribe received");
                            topicLs = Message.decodeSubscribe(packet);
                            for (String c : topicLs)
                                Topic.subscribe(c,this.queue);
                            break;
                        default:
                            System.out.println("else received");
                            break;
                    }

                    read += (rm + 2);
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