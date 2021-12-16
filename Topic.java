import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Topic
 */
public class Topic {
    private static Map<String, Topic> dic = new HashMap<String, Topic>();
    // private String name;
    private List<Client> clientLs;

    private Topic(String name) {
        // this.name = name;
        this.clientLs = new ArrayList<>();
    }

    public static void subscribe(String topicName, Client client) {

        if (!dic.containsKey(topicName)) {
            dic.put(topicName, new Topic(topicName));
        }
        dic.get(topicName).clientLs.add(client);
    }

    public static void publish(String topicName, byte[] toPublish) {
        if (dic.containsKey(topicName)) {
            List<Client> list = dic.get(topicName).clientLs;
            for (Client c : list)
                c.queue.add(toPublish);
        }
    }

    public static void unSubscribe(String topic, Client client) {
        if (dic.containsKey(topic))
            dic.get(topic).clientLs.remove(client);
    }
}
