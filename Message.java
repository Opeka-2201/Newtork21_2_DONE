
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * <h1>Message class: method used to decode and creat message</h1>
 * This class contains only static method. It is very usefull to create Message
 * or to extract some elements in a MQTT byte array.
 * 
 * @author LOUISArthur
 * @author Lambermont Romain
 */
public class Message {


    /**
     * This function return the type of a MQTT message
     * @param b A total MQTT packet byte array
     * @return MQTT Type as an int
     */
    public static int getType(byte[] b) {
        int type = Byte.toUnsignedInt(b[0]);
        return (type / 16);
    }

    /**
     * Check if a MQTT CONNECT packet is legal
     * @param packet
     * @return a boolean : true if legal, false if not
     */
    public static boolean checkConnect(byte[] packet) {
        if (packet[0] != 16 || packet[2] != 0 || packet[3] != 4 || packet[4] != 77 || packet[5] != 81 || packet[6] != 84
                || packet[7] != 84 || packet[8] != 4)
            return false;
        return true;
    }

    /**
     * Return the Keep Alive parameter of an MQTT CONNECT Packet.
     * @param packet A MQTT CONNECT Packet
     * @return  the Keep Alive parameter in second.
     */
    public static int getKeepAlive(byte[] packet) {
        return (int) packet[10] * (2 ^ 8) + (int) packet[11];
    }

    /**
     * Return 2 values. The first is the remaining length and the second is the
     * number of bytes to store it.
     * (algorithm found in the MQTT doc)
     * 
     * @param packet An MQTT packet (byte array)
     * @return a int array of size 2. int [0] = remaining length, int[1] = number of
     *         bytes to store it
     */
    static public int[] getRemainingLength(byte[] packet) {
        int multiplier = 1;
        int length = 0;
        int i = 1;
        int[] rm = new int[2];
        Byte encodedByte;
        try {
            do {
                encodedByte = packet[i];
                i += 1;
                length += (encodedByte & (byte) 127) * multiplier;
                if (multiplier > 128 * 128 * 128) {
                    throw new MessageException("Not valid Remaining Length");
                }
                multiplier *= 128;

            } while ((encodedByte & (byte) 128) != 0);
        } catch (MessageException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        rm[0] = length;
        rm[1] = i - 1;
        return rm;
    }

    /**
     * Use the MQTT 3.1.1 convension to convert a byte array in String
     * @param packet A MQTT packet (byte array)
     * @param offset Index of the first byte of the string wich means the MSB length (int)
     * @return a String
     */
    public static String decodeString(byte[] packet, int offset) {
        int length = (int) packet[offset] * (2 ^ 8) + (int) packet[offset + 1];
        return new String(packet, offset + 2, length);
    }

    /**
     * Create a CONNACK MQTT packet.
     * @param sp session present flag (int)
     * @param returnCode return code(int): failure(128) or QoS
     * @return a byte array ready to be send
     */
    public static byte[] createConnack(int sp, int returnCode) {
        byte[] toReturn = new byte[4];
        toReturn[0] = (byte) 32;
        toReturn[1] = (byte) 2;
        toReturn[2] = (byte) sp;
        toReturn[3] = (byte) returnCode;
        return toReturn;
    }

    /**
     * Extract the topic of a PUBLISH MQTT packet
     * @param packet (byte array)
     * @return a String
     */
    public static String getTopic(byte[] packet) {
        return decodeString(packet, getRemainingLength(packet)[1] + 1);
    }

    /**
     * Exctract the SubscribeID of a SUBRSCIBE MQTT packet
     * @param packet (byte array)
     * @return a byte array of size 2. toReturn[0]=MSB, toReturn[1]=LSB
     */
    public static byte[] getSubscribeID(byte[] packet) {
        int[] rm = getRemainingLength(packet);
        byte[] toReturn = new byte[2];
        toReturn[0] = packet[rm[1] + 1];
        toReturn[1] = packet[rm[1] + 2];

        return toReturn;
    }

    /**
     * Exctract all the topic present in a SUBSCRIBE MQTT packet
     * @param packet (byte array)
     * @return a String array. toReturn[0]=first topic, toReturn[1]=second topic, ...
     */
    public static String[] decodeSubscribe(byte[] packet) {
        int[] rm = getRemainingLength(packet);
        int i = rm[1] + 3;
        int length;
        String s;
        ArrayList<String> ls = new ArrayList<>();
        try {
            while (i < rm[0] + rm[1]) {
                length = packet[i] * 128 + packet[i + 1];
                i += 2;
                s = new String(packet, i, length, StandardCharsets.UTF_8);
                ls.add(s);
                i += length;
                if (!((int) packet[i] == 0 || (int) packet[i] == 1 || (int) packet[i] == 2))
                    throw new MessageException("QoS error in Subscribe");
                i += 1;
            }
        } catch (MessageException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        String[] toReturn = new String[ls.size()];
        for (i = 0; i < ls.size(); i++) {
            toReturn[i] = ls.get(i);
        }
        return toReturn;
    }

    /**
     * Extract all the QoS byte relative to each Topic of a SUBSCRIBE MQTT packet
     * 
     * @param packet (byte array)
     * @return an int array. toReturn[0]=QoS of first Topic, toReturn[1]=QoS of second topic, ...
     */
    public static int[] getQoS(byte[] packet) {
        int[] rm = getRemainingLength(packet);
        int i = rm[1] + 3;
        int length;
        ArrayList<Integer> ls = new ArrayList<>();
        try {
            while (i < rm[0] + rm[1]) {
                length = packet[i] * 128 + packet[i + 1];
                i += 2;
                i += length;
                if (!((int) packet[i] == 0 || (int) packet[i] == 1 || (int) packet[i] == 2))
                    throw new MessageException("QoS error in Subscribe");
                ls.add((int) packet[i]);
                i += 1;
            }
        } catch (MessageException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        int[] toReturn = new int[ls.size()];
        for (i = 0; i < ls.size(); i++) {
            toReturn[i] = ls.get(i);
        }
        return toReturn;
    }

    /**
     * Create a SUBACK MQTT packet
     * @param subId corresponding to the SubscribeID of the correspondant SUBSCRIBE MQTT packet(int)
     * @param QosReturnCode all the QoSReturnCode corresponding to each  topic of the SUBSCRIBE MQTT packet (int array)
     * @return a byte array. Ready to be send
     */
    public static byte[] createSuback(byte[] subId, int[] QosReturnCode) {
        byte[] toReturn = new byte[4 + QosReturnCode.length];
        toReturn[0] = (byte) (128 + 16);
        toReturn[1] = (byte) (2 + QosReturnCode.length);
        toReturn[2] = subId[0];
        toReturn[3] = subId[1];
        for (int i = 0; i < QosReturnCode.length; i++)
            toReturn[4 + i] = 0;

        return toReturn;
    }

    /**
     * Create a PINGRESP MQTT packet
     * @return a byte array. Ready to be send
     */
    public static byte[] createPingResp() {
        byte[] toReturn = new byte[2];
        toReturn[0] = (byte) 208;
        toReturn[1] = (byte) 0;
        return toReturn;
    }

}