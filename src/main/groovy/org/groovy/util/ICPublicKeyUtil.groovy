package org.groovy.util

import org.groovy.dao.secretKey.SecretKeyDao
import org.jpos.ext.security.MyJCEHandler
import org.jpos.ext.security.SoftSecurityModule
import org.jpos.iso.ISOUtil
import org.jpos.security.SMAdapter
import org.jpos.security.SecureKeyStore
import org.jpos.tlv.TLVList
import org.jpos.util.NameRegistrar

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.util
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/24
 * Time : 15:49
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class ICPublicKeyUtil {

    static jceHandler = new MyJCEHandler('com.sun.crypto.provider.SunJCE')
    static final KEYSTORE_KEY = 'keyStore'
    static final SMADAPTER_KEY = 'hsm'

    static getICPublicKey() {
        SecretKeyDao dao = new SecretKeyDao()
        def rid = dao.findICPublicRID();
        def aid = dao.findICPublicAID();
        Map<String, String> ridTlv = new HashMap<String, String>();
        rid.any {
            def tlv = new TLVList()
            tlv.append(0x9F06, it.rid)
            tlv.append(0x9F22, it.ind)
            tlv.append(0xDF05, it.exp)
            tlv.append(0xDF06, it.hash_alg)
            tlv.append(0xDF07, it.rid_alg)
            tlv.append(0xDF02, it.mod)
            tlv.append(0xDF04, it.idx)
            tlv.append(0xDF03, it.ck)
            String key = it.rid + it.ind;
            ridTlv.put(key, ISOUtil.hexString(tlv.pack()));
        }

        Map<String, String> aidTlv = new HashMap<String, String>();
        aid.any {
            def tlv = new TLVList()
            tlv.append(0x9F06, it.aid)
            tlv.append(0xDF01, it.asi)
            tlv.append(0x9F08, it.ver)
            tlv.append(0x9F09, it.ver)
            tlv.append(0xDF11, it.tac_default)
            tlv.append(0xDF12, it.tac_online)
            tlv.append(0xDF15, it.threshold)
            tlv.append(0xDF13, it.tac_deninal)
            tlv.append(0x9F1B, it.floor_limit)
            tlv.append(0xDF16, it.threshold_percent)
            tlv.append(0xDF17, it.threshold_val)
            tlv.append(0xDF14, it.ddol)
            tlv.append(0xDF18, it.online_pin)
            aidTlv.put(String.valueOf(it.aid), ISOUtil.hexString(tlv.pack()));
        }

        def result = [
                "rids": ridTlv,
                "aids": aidTlv
        ];
        return result;
    }


    static updateTPK(ksnNo, tid) {
        def ks = NameRegistrar.get(KEYSTORE_KEY) as SecureKeyStore
        def sm = NameRegistrar.get(SMADAPTER_KEY) as SoftSecurityModule
        def prefix = getKeyPrefix(ksnNo)
        def tmk = ks.getKey("${prefix}.tmk")
        def clear_tpk = jceHandler.generateDESKey(SMAdapter.LENGTH_DES3_2KEY)
        def tpk = sm.encryptToLMK(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_TPK, clear_tpk)
        ks.setKey("tid.${tid}.tpk", tpk)
        [ISOUtil.hexString(sm.exportKey(tpk, tmk)), ISOUtil.hexString(tpk.keyCheckValue)]
    }

    static getKeyPrefix(ksnNo) {
        "ws.${ksnNo}"
    }
}
