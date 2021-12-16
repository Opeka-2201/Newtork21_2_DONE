/**
 * ReaderBroker
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;


public class ReaderBroker implements Runnable {

    Client client;
    Boolean read;
    InputStream in;
    byte[] stream;
    String name ;
    ArrayList<String> topicLs;


    public ReaderBroker(Client client) throws IOException{
        
        
        this.client = client;
        this.read = true;
        this.in = this.client.s.getInputStream();
        this.stream = new byte[2000];
        this.topicLs = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
			this.client.s.setTcpNoDelay(true);
            int type;
            String topic;
            String[] topicArray = null;
            byte[] packet= null;
            while(read){
                packet = read();
                type = Message.getType(packet);
                switch (type) {
                    case 1:
                        if (!Message.checkConnect(packet))
                            throw new MessageException("Connect malfomed");
                        this.client.s.setSoTimeout(Message.getKeepAlive(packet) * 1000);
                        this.name = Message.decodeString(packet, 12);
                        this.client.queue.add(Message.createConnack(1, 0));
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
                        this.client.queue.add(Message.createSuback(subId,listQoS));
                        break;
                    
                    case 12:
                        this.client.queue.add(Message.createPingResp());

                    case 14:
                        for (String c : this.topicLs){
                            Topic.unSubscribe(c, this);
                            this.topicLs.remove(c);
                        }
                        this.client.stop();
                        
                        break;
                    default:
                        break;
                }
            }
            
		} catch (SocketException e) {
			e.printStackTrace();
            this.client.stop();
		}catch (MessageException e) {
            e.printStackTrace();
            System.exit(-1);
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
                if (msgLength == writed)
                    end = true;
                else
                    received = this.in.read(this.stream);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return packet;
    }

    public void stop() {
        this.read = false;
        Thread.currentThread().interrupt();
    }
}