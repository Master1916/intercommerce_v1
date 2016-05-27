package org.groovy.device

import org.groovy.util.RSAKeyStoreUtil

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-6
 * Time : 下午5:45
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class DeviceKeyItroni21 extends DeviceKey{
    @Override
    Map<String, String> initKey(String deviceMode, String deviceNo, Map<String, String> params) {

        RSAKeyStoreUtil keyStore = new RSAKeyStoreUtil();
        def key = keyStore.genKeypair();
        keyStore.setKey(getRSAKey(deviceNo),key.privateKey);
        keyStore.setKey(getRSAKey(deviceNo),key.publicKey);
        def keys = [:];
        keys << [isBluetooth   :   false];
        keys << [pinKeyType    :   "RSA"];
        keys << [pinKeyValue   :   key.publicKey.encoded.encodeBase64() as String];
        keys << [ksnNo         :    deviceNo];
        return keys
    }
}
