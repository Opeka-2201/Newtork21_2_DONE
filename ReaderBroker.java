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
    byte[] stream;
    String name ;
    int writedLength = 0;
    int readerCount = 0;


    public ReaderBroker(Socket s, BlockingQueue<byte[]> q) throws IOException{
        this.queue = q;
        this.s = s;
        this.in = s.getInputStream();
        this.stream = new byte[2000];
    }

    @Override
    public void run() {
        try {
			s.setTcpNoDelay(true);
            int type;
            String topic;
            String[] topicArray = null;
            byte[] packet= null;
            while(true){
                packet = read();
                //System.out.println(Integer.toHexString((int)packet[0]));
                type = Message.getType(packet);
                System.out.println(type);
                switch (type) {
                    case 1:
                        if (!Message.checkConnect(packet))
                            System.out.println("C est casse"); // TODO
                        s.setSoTimeout(Message.getKeepAlive(packet) * 1000);
                        this.name = Message.decodeString(packet, 12);
                        this.queue.add(Message.createConnack(1, 0));
                        System.out.println(this.name + ": conncected");
                        break;

                    case 3:
                        topic = Message.getTopic(packet);
                        System.out.println("    Publish received");
                        Topic.publish(topic, packet);                            
                        break;

                    
                    case 8:
                        System.out.println("  Subscribe received");
                        byte[] subId = Message.getSubscribeID(packet);
                        topicArray = Message.decodeSubscribe(packet);
                        int [] listQoS = Message.getQoS(packet);
                        for (String c : topicArray)
                            Topic.subscribe(c,this.queue);
                        this.queue.add(Message.createSuback(subId,listQoS));
                        System.out.println("  SUBACK send");
                        break;
                    
                    case 12:
                        System.out.println("PingReq received");
                        this.queue.add(Message.createPingResp());
                        System.out.println("PingResp received");

                        break;
                    default:
                        System.out.println("else received");
                        break;
                }
            }
            
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
    }

    public byte[] read()
    {  
        int[] rm;
        int msgLength;
        byte[] packet = null;
        boolean end = false;
        int writed = 0;
        int received = 0;
        try {
            received = this.in.read(this.stream);
            rm = Message.getRemainingLength(this.stream,0);
            msgLength = rm[0] + rm[1] + 1;
            packet = new byte[msgLength];
            while (!end){
                for(int i = 0; i < received ; i++)
                    packet[writed+ i] = this.stream[i];
                writed += received;
                //System.out.println("msg = "+ msgLength +" writed= "+ writed);
                if (msgLength == writed)
                    end = true;
                else
                    received = this.in.read(this.stream);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return packet;
    }
}