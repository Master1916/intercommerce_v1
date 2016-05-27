package org.groovy.device

import org.groovy.common.Constants
import org.jpos.core.CardHolder
import org.jpos.ext.security.MyJCEHandler
import org.jpos.ext.security.SoftSecurityModule
import org.jpos.security.SecureKeyStore
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-2
 * Time : 下午8:29
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public abstract class TrackDecoder {

    protected Log log = new Log(NameRegistrar.getIfExists("logger.Q2") as Logger,this.getClass().getSimpleName());

    protected MyJCEHandler handler = new MyJCEHandler(Constants.PROVIDER);

    protected SecureKeyStore ks = (SecureKeyStore)NameRegistrar.get(Constants.KEY_STORE_KEY);

    protected SoftSecurityModule sm = (SoftSecurityModule)NameRegistrar.get(Constants.SMADAPTER_KEY);
    /**
     * MAC校验
     * @param deviceMode
     * @param deviceNo
     * @param params
     * @return
     */
    public abstract CardHolder decode(String deviceMode,String deviceNo,Map<String,String> params);

    protected String getTPK(String tid){
        return "tid." + tid + ".tpk";
    }

    protected String getTAK(String deviceNo){
        return "ws." + deviceNo + ".tak";
    }

    public static byte[] encrypt(byte[] data, byte[] key)
    {
        SecretKey sk = new SecretKeySpec(key, "DES");
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, sk);
            byte[] enc = cipher.doFinal(data);
            return enc;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] doFinal(byte[] data, byte[] key, boolean encrypt1) {
        byte[] out = null;
        if (key.length == 8) {
            if (encrypt1) {
                out = encrypt(data, key);
            } else {
                out = decrypt(data, key);
            }
            return out;
        }
        if (key.length != 24 && key.length != 16) {
            new IllegalArgumentException("key length should be 24 or 16").printStackTrace();
            return null;
        }
        int shift = 16;
        if (key.length == 16) shift = 0;
        byte[] k1 = new byte[8];
        byte[] k2 = new byte[8];
        byte[] k3 = new byte[8];
        for (int i = 0; i < 8; i++) {
            k1[i] = key[i];
            k2[i] = key[i + 8];
            k3[i] = key[i + shift];
        }
        if (encrypt1) {
            out = encrypt(data, k1);
            out = decrypt(out, k2);
            out = encrypt(out, k3);
        } else {
            out = decrypt(data, k3);
            out = encrypt(out, k2);
            out = decrypt(out, k1);
        }
        return out;
    }

    public static byte[] decrypt(byte[] data, byte[] key)
    {
        SecretKey sk = new SecretKeySpec(key, "DES");
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sk);
            byte[] enc = cipher.doFinal(data);
            return enc;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
