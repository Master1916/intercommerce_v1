package org.groovy.device

import org.groovy.dao.terminal.DeviceDao
import org.jpos.iso.ISOUtil
import org.jpos.security.EncryptedPIN
import org.jpos.security.SMAdapter
import org.jpos.security.SecureDESKey

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-4
 * Time : 下午7:57
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class CheckPinblockLandim35 extends CheckPinblock{

    @Override
    String check(String deviceMode, String deviceNo, Map<String, String> params) {

        DeviceDao deviceDao = new DeviceDao();
        def terminal = deviceDao.findTerminalByKsn(deviceNo);
        if(!terminal){
            log.error("can not find device:" + deviceNo + ",terminal=" + terminal);
            return null;
        }
        def pk_alias = getTPK(terminal.terminal_no as String);
        def front_zpk = getFrontZPK()
        def pk = ks.getKey(pk_alias) as SecureDESKey;
        def epin = new EncryptedPIN(params.encPinblock, SMAdapter.FORMAT01, params.cardNo);
        def bpin = sm.translatePIN(epin, pk, front_zpk, SMAdapter.FORMAT01);
        return ISOUtil.hexString(bpin.getPINBlock());
    }
}
