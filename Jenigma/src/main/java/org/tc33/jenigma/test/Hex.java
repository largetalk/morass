package org.tc33.jenigma.test;

public class Hex {

    public static byte[] toBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    public static String toHexString(byte[] bytes) {
        StringBuilder s = new StringBuilder(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv= Integer.toHexString(v);
            if (hv.length() < 2) {
                s.append(0);
            }
            s.append(hv);
        }
        return s.toString();
    }
}