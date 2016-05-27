package org.groovy.device

import org.apache.commons.lang.StringUtils
import org.groovy.util.AlgorithmUtil
import org.jpos.iso.ISOUtil
import org.jpos.security.SMAdapter

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-1
 * Time : 下午1:34
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class ChecksumItroni21 extends DeviceCheck{

    @Override
    boolean check(String deviceMode, String deviceNo, Map<String, String> params) {
        def tak = ks.getKey(getTAK(deviceNo));
        def clear_tmk = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, new byte[16])
        def lmk_tmk = sm.encryptToLMK(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_TMK, clear_tmk)
        def clear_tdk = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, handler.decryptData(sm.exportKey(tak, lmk_tmk), clear_tmk))

        def encTracks = params.encTracks[0..-17]
        def random = params.encTracks[-16..-1]
        def ksnNo = params.ksnNo
        def reqNo = params.reqNo
        def data = ksnNo + reqNo
        byte[] hex=ksnNo[-8..-1].bytes

        def macStr = params.checksum[-8..-1]

        byte[] vb = ISOUtil.hex2byte(encTracks+random[0..7]+ISOUtil.hexString(hex)+ISOUtil.hexString(data.bytes))
        //log.info("pre=" + ISOUtil.hexString(vb))
        byte[] vt = new byte[vb.length+8]
        System.arraycopy(vb, 0, vt, 0,vb.length)
        int size = vt.length / 8
        byte[] res = new byte[8]
        for(int i=0;i<size;i++){
            byte[] temp = new byte[8]
            System.arraycopy(vt, i*8, temp, 0, 8)
            res = ISOUtil.xor(res, temp)
        }
        def newRandom = macStr + random[-8..-1]
        def left = handler.encryptData(ISOUtil.hex2byte(newRandom), clear_tdk)
        def xor = ISOUtil.xor(ISOUtil.hex2byte(newRandom), ISOUtil.hex2byte("FFFFFFFFFFFFFFFF"))
        def right = handler.encryptData(xor, clear_tdk)
        def result = ISOUtil.concat(left, right)
        //log.info("result=" + ISOUtil.hexString(result))
        def key = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, result)
        def leftMac = handler.encryptData(res, key)
        def mac = new byte[4]
        System.arraycopy(leftMac,0,mac,0,4)
        String macString =  ISOUtil.hexString(mac) + macStr;
        log.info("i21mac=" + ISOUtil.hexString(mac) + ",macString" + macString);
        StringBuilder checkString = new StringBuilder();
        checkString.append(params.reqTime);
        checkString.append(params.reqNo);
        checkString.append(params.amount);
        checkString.append(macString);
        checkString.append(params.position);
        if(params.tradeFlag){
            checkString.append(params.tradeFlag);
        }
        if(params.tradeSecFlag){
            checkString.append(params.tradeSecFlag);
        }
        String local = AlgorithmUtil.encodeBySha1(checkString.toString());
        String checksum = params.checksum[0..-9];
        log.info("checksum:local=" + local + ",checksum=" + checksum);
        return StringUtils.equalsIgnoreCase(local,checksum);
    }
}
