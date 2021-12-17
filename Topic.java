import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>Topic class: class used to store a topic and its subscribers</h1>
 * An instance for each topic is created. We keep a dictionnary all the topic in
 * a class variable.
 * Each topic contains a list of client which are subscribed to the topic.
 * 
 * @author LOUIS Arthur
 * @author LAMBERMONT Romain
 */
public class Topic {
    private static Map<String, Topic> dic = new HashMap<String, Topic>();
    private List<Client> clientLs;

    /**
     * Constructor, it's used only when we received a new topic.
     * 
     * @param name , of the topic (String)
     */
    private Topic(String name) {
        this.clientLs = new ArrayList<>();
    }

    /**
     * Add client to the list of subriber of the topic
     * If the topic doesn't exist, topic is created
     * 
     * @param topicName received in a SUBSCRIBE MQTT packet (String)
     * @param client    who wants to Subcribe (Client)
     */
    public static void subscribe(String topicName, Client client) {

        if (!dic.containsKey(topicName)) {
            dic.put(topicName, new Topic(topicName));
        }
        dic.get(topicName).clientLs.add(client);
    }

    /**
     * Publish a packet to each client subscribe to a topic if the topic already
     * exists
     * 
     * @param topicName received in a PUBLISH MQTT packet
     * @param packet    to send
     */
    public static void publish(String topicName, byte[] toPublish) {
        if (dic.containsKey(topicName)) {
            List<Client> list = dic.get(topicName).clientLs;
            for (Client c : list)
                c.queue.add(toPublish);
        }
    }

    /**
     * Remove a client from the list of subscriber of a topic
     * 
     * @param client which wants to unsubscribe
     * @param topic  to unsubscribe
     */
    public static void unSubscribe(String topic, Client client) {
        if (dic.containsKey(topic))
            dic.get(topic).clientLs.remove(client);
    }
}
