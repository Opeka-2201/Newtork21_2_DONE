import java.util.ArrayList;
import java.util.List;

public class Message {
    public static int getType(byte[] b, int offset) {
        return (int)(b[offset]>>>4);
        
    }

    public static boolean checkConnect(byte[] stream) {
        if(stream[0]!=16 || stream[2]!=0 || stream[3]!=4 || stream[4]!=77 || stream[5]!=81 || stream[6] != 84|| stream[7]!=84 || stream[8]!=4)
            return false;
        return true;
    }

    public static int getKeepAlive(byte[] stream) {return (int)stream[10]*(2^8)+(int)stream[11];}

    public static int getRemainingLength(byte[] stream, int offset) {return (int)stream[offset+1];}

    public static String decodeString(byte[] stream, int offset) {
        int length = (int)stream[offset] * (2^8) + (int)stream[offset+1];
        return new String(stream, offset+2, length);  
    }

    public static byte[] createConnack(int sp ,int returnCode){
        byte[] toReturn = new byte[4];
        toReturn[0] = (byte) 32;
        toReturn[1] = (byte) 2;
        toReturn[2] = (byte) sp;
        toReturn[3] = (byte) returnCode;
        return toReturn;
    }

    public static String getTopic(byte[] stream, int offset) {
        return decodeString(stream, offset+2);  
    }

    public static String[] decodeSubscribe(byte[] packet) {
        int rm = (int) packet[1];
        rm -= 2;
        int count = 0;
        int length;
        List<String> ls = new ArrayList<>();
        while (rm>count){
            length = (int)packet[count] * (2^8) + (int)packet[count];
            ls.add(new String(packet, count + 2, count + length));
            count += (length + 2);
        }
        String[] toReturn = new String[ls.size()];
        toReturn = ls.toArray(new String[0]);
        return toReturn;        
    }
}