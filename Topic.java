import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Topic
 */
public class Topic {
    private static Map<String, Topic> dic = new HashMap<String, Topic>();
    //private String name;
    private List<ReaderBroker> readerLs;

    private Topic(String name){
        //this.name = name;
        this.readerLs = new ArrayList<>();
        }

    public static void subscribe(String topicName, ReaderBroker reader) {

        if (!dic.containsKey(topicName)) {
            dic.put(topicName, new Topic(topicName));
        }
        dic.get(topicName).readerLs.add(reader);
    }

    public static void publish(String topicName, byte[] toPublish) {
        if(dic.containsKey(topicName)){
            List<ReaderBroker>  list = dic.get(topicName).readerLs;
            for (ReaderBroker r : list)
                r.queue.add(toPublish);
        }
    }

    public static void unSubscribe(String topic, ReaderBroker reader) {
            if (dic.containsKey(topic))
                dic.get(topic).readerLs.remove(reader);   
    }
}

