package org.groovy.device

import org.groovy.dao.terminal.DeviceDao
import org.groovy.util.RSAKeyStoreUtil
import org.jpos.iso.ISOUtil
import org.jpos.security.SMAdapter

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-6
 * Time : 下午5:52
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class DeviceKeyItroni21b extends DeviceKey{
    @Override
    Map<String, String> initKey(String deviceMode, String deviceNo, Map<String, String> params) {
        log.info("deviceMode=${deviceMode},deviceNo=${deviceNo}")
        try {
            def tmk = ks.getKey(getTMK(deviceNo));
            def clear_tpk = handler.generateDESKey(SMAdapter.LENGTH_DES3_2KEY);
            def tpk = sm.encryptToLMK(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_TPK, clear_tpk);

            DeviceDao deviceDao = new DeviceDao();
            def terminal = deviceDao.findTerminalByKsn(deviceNo);
            if(!terminal){
                log.error("can not find device:" + deviceNo + ",terminal=" + terminal);
                return null;
            }

            ks.setKey(getTPK(terminal.terminal_no as String), tpk);
            log.info("tpk=${tpk},tmk=${tmk}");
            def keys = [:];
            keys << [keyType    :   "3DES"];
            keys << [keyValue    :   ISOUtil.hexString(sm.exportKey(tpk, tmk))];
            keys << [checkValue    :   ISOUtil.hexString(tpk.keyCheckValue)];

            RSAKeyStoreUtil keyStore = new RSAKeyStoreUtil();
            def key = keyStore.genKeypair();
            keyStore.setKey(getRSAKey(deviceNo),key.privateKey);
            keyStore.setKey(getRSAKey(deviceNo),key.publicKey);
            keys << [pinKeyType    :   "RSA"];
            keys << [pinKeyValue   :   key.publicKey.encoded.encodeBase64() as String];
            keys << [isBluetooth   :   true];
            keys << [macAddress    :   String.valueOf(terminal.mac_address).toUpperCase()];
            keys << [ksnNo         :    deviceNo];
            return keys
        } catch (e) {
            log.error(deviceNo + " create device key exception:" + e.getMessage());
        }
        return null;
    }
}
