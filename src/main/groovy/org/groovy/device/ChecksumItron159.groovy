package org.groovy.device

import org.apache.commons.lang.StringUtils
import org.groovy.dao.terminal.DeviceDao
import org.jpos.iso.ISOUtil
import org.jpos.security.SMAdapter

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-2
 * Time : 下午6:44
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class ChecksumItron159 extends DeviceCheck{
    @Override
    boolean check(String deviceMode, String deviceNo, Map<String, String> params) {

        DeviceDao deviceDao = new DeviceDao();
        def terminal = deviceDao.findTerminalByKsn(deviceNo);
        if(!terminal){
            log.error("can not find device:" + deviceNo + ",terminal=" + terminal);
            return false;
        }

        String value = checkValues(params,8);
        def tak = ks.getKey(getTPK(terminal.terminal_no as String));
        def clear_tmk = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, new byte[16])
        def lmk_tmk = sm.encryptToLMK(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_TMK, clear_tmk)
        def clear_tak = handler.decryptData(sm.exportKey(tak, lmk_tmk), clear_tmk);
        log.info("value=" + value);
        byte[] res = xor(value)
        //res 取前八个 和后八个字节
        def start = ISOUtil.hexString(res)[0..7].getBytes();
        def end =  ISOUtil.hexString(res)[8..15].getBytes();
        //前八个字节和mak做3des
        def result = handler.encryptData(start, handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, clear_tak));
        //然后和后八个字节做异或
        result = ISOUtil.xor(end ,result)
        result = handler.encryptData(result, handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, clear_tak));
        String local = ISOUtil.hexString(ISOUtil.hexString(result).getBytes())[0..15];
        String checksum = params.checksum;
        log.info("checksum:local=" + local + ",checksum=" + checksum);
        return StringUtils.equals(local,checksum);
    }
}
