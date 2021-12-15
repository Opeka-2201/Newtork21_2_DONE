import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Topic
 */
public class Topic {
    private static Map<String, Topic> dic = new HashMap<String, Topic>();
    //private String name;
    private List<BlockingQueue<byte[]>> queueLs;

    private Topic(String name){
        //this.name = name;
        this.queueLs = new ArrayList<>();
        }

    public static void subscribe(String topicName, BlockingQueue<byte[]> queue) {

        if (!dic.containsKey(topicName)) {
            dic.put(topicName, new Topic(topicName));
        }
        dic.get(topicName).queueLs.add(queue);
    }

    public static void publish(String topicName, byte[] toPublish) {
        toPublish[1] = (byte)48;
        if(dic.containsKey(topicName)){
            List<BlockingQueue<byte[]>>  list = dic.get(topicName).queueLs;
            for (BlockingQueue<byte[]> q : list)
                q.add(toPublish);
        }
    }
}

