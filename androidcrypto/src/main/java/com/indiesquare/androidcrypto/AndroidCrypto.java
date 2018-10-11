package com.indiesquare.androidcrypto;

import java.security.SecureRandom;

/**
 * Created by a on 2018/10/11.
 */

public class AndroidCrypto {
    public AndroidCrypto(){

    }

    public static byte[] generateRandomBytes(){


        SecureRandom random = new SecureRandom();

        byte bytes[] = new byte[128];

        random.nextBytes(bytes);

        return bytes;

    }

}
