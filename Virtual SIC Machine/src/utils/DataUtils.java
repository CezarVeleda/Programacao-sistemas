package utils;

public class DataUtils {

    public static int bytes24ToInt(byte[] bytes) {
        int val = ((bytes[0] & 0xFF) << 16) | 
                    ((bytes[1] & 0xFF) << 8)  | 
                     (bytes[2] & 0xFF);

        if ((val & 0x800000) != 0) {
            val |= 0xFF000000;
        }

        return val;
    }
    
    public static byte[] intToBytes24(int val) {
        byte[] bytes = new byte[3];

        bytes[0] = (byte) ((val >> 16) & 0xFF);
        bytes[1] = (byte) ((val >> 8) & 0xFF);
        bytes[2] = (byte) (val & 0xFF);

        return bytes;
    }
    
    public static int bytes24ToInt(byte[] memory, int offset) {
        int val = ((memory[offset] & 0xFF) << 16) | 
                    ((memory[offset + 1] & 0xFF) << 8) | 
                     (memory[offset + 2] & 0xFF);

        if ((val & 0x800000) != 0) {
            val |= 0xFF000000;
        }

        return val;
    }
    
}
