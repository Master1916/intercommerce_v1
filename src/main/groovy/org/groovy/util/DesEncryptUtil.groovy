package org.groovy.util

import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import java.security.SecureRandom

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.util
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/24
 * Time : 15:46
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class DesEncryptUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, DesEncryptUtil.getSimpleName())

    //DES加密
    static byte[] encrypt(byte[] datasource, byte[] password) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            return cipher.doFinal(datasource);
        } catch (Exception e) {
            log.error("Exception:" + e.getMessage(), e);
        }
        return null;
    }

    //DES解密
    static byte[] decrypt(byte[] src, byte[] password) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, securekey, random);
            return cipher.doFinal(src);
        } catch (Exception e) {
            log.error("Exception:" + e.getMessage(), e);
        }
        return null;
    }

}
