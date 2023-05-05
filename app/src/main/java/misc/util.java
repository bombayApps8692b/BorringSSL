package misc;

public class util {
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
//            return null;
        	return new byte[0];
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte1(hexChars[pos]) << 4 | charToByte1(hexChars[pos + 1]));
        }
        return d;
    }
    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
     private static byte charToByte1(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
     

     
     public static byte[] inttobyte(int num){
    	 
    	 byte[] result = new byte[4];   
    	 result[0] = (byte)(num >>> 24);
    	 result[1] = (byte)(num >>> 16);
    	  result[2] = (byte)(num >>> 8);
    	 result[3] = (byte)(num );
    	 return result;   

     }
     
     
}
