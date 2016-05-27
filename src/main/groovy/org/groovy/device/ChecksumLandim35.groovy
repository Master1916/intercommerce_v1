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
 * Date : 15-12-1
 * Time : 下午7:52
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class ChecksumLandim35 extends DeviceCheck{

    @Override
    boolean check(String deviceMode, String deviceNo, Map<String, String> params) {
        int LEN = 8;
        byte[] data = new byte[LEN];

        for(String key : params.keySet()) {
            if (key.equalsIgnoreCase("checksum")) continue;
            Object obj = params.get(key);
            if (obj instanceof String) {
                try {
                    byte[] s = obj.toString().getBytes("UTF-8");
                    int len = (s.length - 1) / LEN + 1;
                    for (int i = 0; i < len; i++) {
                        data = xor(data, s, i * LEN);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        DeviceDao deviceDao = new DeviceDao();
        def terminal = deviceDao.findTerminalByKsn(deviceNo);
        if(!terminal){
            log.error("can not find device:" + deviceNo + ",terminal=" + terminal);
            return false;
        }
        def tdk = ks.getKey(getTPK(terminal.terminal_no as String));
        def clear_zmk = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, new byte[16])
        def zmk = sm.encryptToLMK(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_ZMK, clear_zmk)
        def tdk_zmk = sm.exportKey(tdk, zmk)
        def clear_tdk = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, handler.decryptData(tdk_zmk, clear_zmk))
        def clear_tunk = handler.encryptData(data, clear_tdk);
        String local = ISOUtil.hexString(clear_tunk);
        String checksum = params.checksum;
        log.info("checksum:local=" + local + ",checksum=" + checksum);
        return StringUtils.equals(local,checksum)
    }
}
