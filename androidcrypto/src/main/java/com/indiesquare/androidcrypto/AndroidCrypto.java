package com.indiesquare.androidcrypto;

import java.security.SecureRandom;
/**
 * Created by a on 2018/10/11.
 */

public class AndroidCrypto {
    public AndroidCrypto(){

    }
    private static final int VERSION_CODE_JELLY_BEAN_MR2 = 18;
    public static String generateRandomBytes(){

        PRNGFixes.apply();



        SecureRandom random = new SecureRandom();

        byte bytes[] = new byte[16];

        random.nextBytes(bytes);

        return bytesToHex(bytes)+"";

    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
