/**
 * ReaderBroker
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;


public class ReaderBroker implements Runnable {

    Client client;
    Socket s;
    BlockingQueue<byte[]> queue;
    InputStream in;
    byte[] stream;
    String name ;
    SenderBroker sender;
    ArrayList<String> topicLs;
    int writedLength = 0;
    int readerCount = 0;


    public ReaderBroker(Client client) throws IOException{
        
        
        this.client = client;
        this.queue = client.queue;
        this.s = client.s;
        this.in = this.s.getInputStream();
        this.stream = new byte[2000];
        this.topicLs = new ArrayList<>();
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
                type = Message.getType(packet);
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
                        Topic.publish(topic, packet);                            
                        break;

                    
                    case 8:
                        byte[] subId = Message.getSubscribeID(packet);
                        topicArray = Message.decodeSubscribe(packet);
                        int [] listQoS = Message.getQoS(packet);
                        for (String c : topicArray){
                            if (!this.topicLs.contains(c))
                                this.topicLs.add(c);
                            Topic.subscribe(c,this);
                        }
                        this.queue.add(Message.createSuback(subId,listQoS));
                        break;
                    
                    case 12:
                        this.queue.add(Message.createPingResp());

                    case 14:
                        System.out.println("Client disconnected");
                        for (String c : this.topicLs){
                            Topic.unSubscribe(c, this);
                            this.topicLs.remove(c);
                        }
                        this.client.stop();
                        
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

    public void stop() {
        Thread.currentThread().interrupt();
    }
}