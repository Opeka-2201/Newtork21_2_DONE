
/**
 * <h1>MessageException Class: to init the server</h1>
 * This class is an Exception class wich is throws for any error related to an
 * MQTT packet
 * 
 * @author LOUIS Arthur
 * @author LAMBERMONT Romain
 */

public class MessageException extends Exception {
    public MessageException(String s) {
        super(s);
    }
}
