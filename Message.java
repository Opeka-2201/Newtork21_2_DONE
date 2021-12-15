import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Message {
    public static int getType(byte[] b) {
        int type = Byte.toUnsignedInt(b[0]);
        System.out.println("Le type est :" + type);
        return (type/16);
        
    }

    public static boolean checkConnect(byte[] packet) {
        if(packet[0]!=16 || packet[2]!=0 || packet[3]!=4 || packet[4]!=77 || packet[5]!=81 || packet[6] != 84|| packet[7]!=84 || packet[8]!=4)
            return false;
        return true;
    }
    public static int getKeepAlive(byte[] packet) {return (int)packet[10]*(2^8)+(int)packet[11];}

    static public int[] getRemainingLength(byte[] message, int offset) {
        // Returns the remaining length and the number of bytes used to store it
        // rem_length[0] = remaining length
        // rem_length[1] = number of bytes to store it
        //TODO changer cette ptn de fonction sinon plagiat
        int multiplier = 1;
        int value = 0;
        int i = 1;
        int[] rem_length = new int[2];
        Byte encodedByte = null;
        do {
            encodedByte = message[i+offset];
            i += 1;
            value += (encodedByte & (byte) 127) * multiplier;
            if (multiplier > 128 * 128 * 128) {
                System.out.println("Malformed remaining length. Disconnecting...");
                return null;
            }
            multiplier *= 128;

        } while ((encodedByte & (byte) 128) != 0);
        rem_length[0] = value;
        rem_length[1] = i - 1;
        return rem_length;
    }

    public static String decodeString(byte[] packet, int offset) {
        int length = (int)packet[offset] * (2^8) + (int)packet[offset+1];
        return new String(packet, offset+2, length);    }

    public static byte[] createConnack(int sp ,int returnCode){
        byte[] toReturn = new byte[4];
        toReturn[0] = (byte) 32;
        toReturn[1] = (byte) 2;
        toReturn[2] = (byte) sp;
        toReturn[3] = (byte) returnCode;
        return toReturn;
    }

    public static String getTopic(byte[] packet) {
        return decodeString(packet,getRemainingLength(packet,0)[1]+1);  
    }

    public static byte[] getSubscribeID(byte[] packet) {
        int[] rm = getRemainingLength(packet, 0);
        byte[] toReturn = new byte[2];
        toReturn[0] = packet[rm[1]+1];
        toReturn[1] = packet[rm[1]+2];

        return toReturn;
    }
    public static String[] decodeSubscribe(byte[] packet) {
        int[] rm = getRemainingLength(packet,0);
        int i = rm[1]+3;
        int length;
        String s;
        ArrayList<String> ls = new ArrayList<>();
        while (i<rm[0]+rm[1]){
            length = packet[i]*128 + packet[i+1];
            i += 2;
            s = new String(packet,i,length,StandardCharsets.UTF_8);
            ls.add(s);
            i += length;
            //if (!((int)packet[i] == 0|| (int)packet[i]==1 || (int)packet[i]==2))
                //throw new MalformedMessageError(); //TODO
            i += 1;
        }
        String[] toReturn = new String[ls.size()];
        for (i = 0; i<ls.size();i++){
            toReturn[i] = ls.get(i);
        }
        return toReturn;    
    }

    public static int[] getQoS(byte[] packet) {
        int[] rm = getRemainingLength(packet, 0);
        int i = rm[1] + 3;
        int length;
        ArrayList<Integer> ls = new ArrayList<>();
        while (i < rm[0] + rm[1]) {
            length = packet[i] * 128 + packet[i + 1];
            i += 2;
            i += length;
            // if (!((int)packet[i] == 0|| (int)packet[i]==1 || (int)packet[i]==2))
            // throw new MalformedMessageError(); //TODO
            ls.add((int)packet[i]);
            i += 1;
        }
        int[] toReturn = new int[ls.size()];
        for (i = 0; i < ls.size(); i++) {
            toReturn[i] = ls.get(i);
        }
        return toReturn;
    }

    public static byte[] createSuback(byte[] subId, int [] QosReturnCode) {
        byte[] toReturn = new byte[4+QosReturnCode.length];
        toReturn[0] = (byte)(128 + 16);
        toReturn[1] = (byte) (2 + QosReturnCode.length);
        toReturn[2] = subId[0];
        toReturn[3] = subId[1];
        for (int i=0; i<QosReturnCode.length;i++)
            toReturn[4+i] = 0;
        
        
        return toReturn;
    }

	public static byte[] createPingResp() {
        byte[] toReturn = new byte[2];
        toReturn[0] = (byte) 208;
        toReturn[1] = (byte) 0;
		return toReturn;
	}

    
}