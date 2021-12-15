import java.util.ArrayList;
import java.util.List;

public class Message {
    public static int getType(byte[] b) {
        return (int)(b[0]>>>4);
        
    }

    public static boolean checkConnect(byte[] packet) {
        if(packet[0]!=16 || packet[2]!=0 || packet[3]!=4 || packet[4]!=77 || packet[5]!=81 || packet[6] != 84|| packet[7]!=84 || packet[8]!=4)
            return false;
        return true;
    }

    public static int getKeepAlive(byte[] packet) {return (int)packet[10]*(2^8)+(int)packet[11];}

    public static int getRemainingLength(byte[] packet) {return (int)packet[1];}

    public static String decodeString(byte[] packet, int offset) {
        int length = (int)packet[offset] * (2^8) + (int)packet[offset+1];
        return new String(packet, offset+2, length);  
    }

    public static byte[] createConnack(int sp ,int returnCode){
        byte[] toReturn = new byte[4];
        toReturn[0] = (byte) 32;
        toReturn[1] = (byte) 2;
        toReturn[2] = (byte) sp;
        toReturn[3] = (byte) returnCode;
        return toReturn;
    }

    public static String getTopic(byte[] packet) {
        return decodeString(packet,2);  
    }

    public static String[] decodeSubscribe(byte[] packet) {
        int rm = (int) packet[1];
        rm -= 2;
        int count = 0;
        int length;
        ArrayList<String> ls = new ArrayList<>();
        while (rm>count){
            length = (int)packet[count] * (2^8) + (int)packet[count];
            ls.add(new String(packet, count + 2, count + length));
            count += (length + 2);
        }
        String[] toReturn = new String[ls.size()];
<<<<<<< HEAD
        for (int i = 0; i<ls.size(); i++)
            toReturn[i] = ls.get(i);
        return toReturn;    
=======
        toReturn = ls.toArray(new String[0]);
        return toReturn;        
>>>>>>> 419858b5128af8c0ce3f848b47bafbcd8b876ba9
    }
}