package org.groovy.device

import org.groovy.util.RSAKeyStoreUtil
import org.jpos.iso.ISOUtil
import sun.misc.BASE64Decoder

import javax.crypto.Cipher

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-4
 * Time : 下午7:19
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class CheckPinblockItroni21 extends CheckPinblock{

    private String KEY_RSA = "RSA";

    @Override
    String check(String deviceMode, String deviceNo, Map<String, String> params) {
        try {
            RSAKeyStoreUtil rsaKeystore = new RSAKeyStoreUtil();
            def keyAlias =  getRSAKey(deviceNo);
            def privateKey = rsaKeystore.getKey(keyAlias, false);
            Cipher cipher = Cipher.getInstance(KEY_RSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            def pin = cipher.doFinal(new BASE64Decoder().decodeBuffer(params.encPinblock));
//          log.info("pin:" + ISOUtil.dumpString(pin));
            def ePin = sm.encryptPIN(ISOUtil.dumpString(pin)[0..5], params.cardNo);
            def zpk = getFrontZPK();
            ePin = sm.exportPIN(ePin, zpk, ePin.getPINBlockFormat());
            return ISOUtil.hexString(ePin.getPINBlock());
        } catch (Exception e) {
            log.error("pinblock error:" + e.getMessage())
        }
        return null;
    }
}
