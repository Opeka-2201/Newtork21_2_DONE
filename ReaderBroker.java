
/**
 * ReaderBroker
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * <h2>ReaderBroker Class: for the reception and the creation of MQTT
 * packet</h2>
 * This class is responsible of reading MQTT packet. It is also responsible of
 * creation a response
 * There is a thread of this class for each client.
 * 
 * @author LOUIS Arthur
 * @author LAMBERMONT Romain
 */
public class ReaderBroker implements Runnable {

    Client client;
    Boolean read;
    InputStream in;
    byte[] stream;
    String name;
    ArrayList<String> topicLs;

    /**
     * This is the constructor, of the class.
     * All the principal informations are already in Client class so we just need
     * the Client.
     * 
     * @param client corresponding to connected client
     */
    public ReaderBroker(Client client) throws IOException {

        this.client = client;
        this.read = true;
        this.in = this.client.s.getInputStream();
        this.stream = new byte[2000];
        this.topicLs = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            this.client.s.setTcpNoDelay(true);// To send immediatly TCP packet
            int type;
            String topic;
            String[] topicArray = null;
            byte[] packet = null;
            while (read) {
                packet = read();
                type = Message.getType(packet);
                switch (type) {
                    case 1:// CONNECT case
                        if (!Message.checkConnect(packet))
                            throw new MessageException("Connect malfomed");
                        this.client.s.setSoTimeout(Message.getKeepAlive(packet) * 1000);
                        this.name = Message.decodeString(packet, 12);
                        this.client.queue.add(Message.createConnack(1, 0));
                        break;

                    case 3:// PUBLISH case
                        topic = Message.getTopic(packet);
                        Topic.publish(topic, packet);
                        break;

                    case 8:// SUBSCRIBE case
                        byte[] subId = Message.getSubscribeID(packet);
                        topicArray = Message.decodeSubscribe(packet);
                        int[] listQoS = Message.getQoS(packet);
                        for (String c : topicArray) {
                            if (!this.topicLs.contains(c))
                                this.topicLs.add(c);
                            Topic.subscribe(c, this.client);
                        }
                        this.client.queue.add(Message.createSuback(subId, listQoS));
                        break;

                    case 12:// PINGREQ case
                        this.client.queue.add(Message.createPingResp());

                    case 14:// DISCONNECT case
                        for (String c : this.topicLs) {// Unsubscribe the client of all topic precendently follow
                            Topic.unSubscribe(c, this.client);
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
        } catch (MessageException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    /**
     * Read is responsible to read an entire MQTT packet. Even if the packet is in
     * two TCP stream.
     * 
     * @return a byte array only composed by the MQTT message.
     */
    public byte[] read() {
        int[] rm;
        int msgLength;
        byte[] packet = null;
        boolean end = false;
        int writed = 0;
        int received = 0;
        try {
            received = this.in.read(this.stream);
            rm = Message.getRemainingLength(this.stream, 0);
            msgLength = rm[0] + rm[1] + 1;
            packet = new byte[msgLength];
            while (!end) {
                for (int i = 0; i < received; i++)
                    packet[writed + i] = this.stream[i];
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