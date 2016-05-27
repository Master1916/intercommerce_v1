package org.groovy.device

import org.groovy.dao.terminal.DeviceDao
import org.jpos.core.CardHolder
import org.jpos.core.InvalidCardException
import org.jpos.iso.ISOUtil
import org.jpos.security.SMAdapter

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-3
 * Time : 下午12:00
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class TrackDecoderItron159 extends TrackDecoder{
    @Override
    CardHolder decode(String deviceMode, String deviceNo, Map<String, String> params) {
        DeviceDao deviceDao = new DeviceDao();
        def terminal = deviceDao.findTerminalByKsn(deviceNo);
        if(!terminal){
            log.error("can not find device:" + deviceNo + ",terminal=" + terminal);
            return null;
        }
        def tdk = ks.getKey(getTPK(terminal.terminal_no as String));
        def clear_zmk = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, new byte[16]);
        def zmk = sm.encryptToLMK(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_ZMK, clear_zmk);
        def tdk_zmk = sm.exportKey(tdk, zmk);
        def clear_tdk = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, handler.decryptData(tdk_zmk, clear_zmk));
        def tracks = ISOUtil.hexString(handler.decryptData(ISOUtil.hex2byte(params.encTracks),clear_tdk))
        //log.info "tracks:$tracks";
        def track2 = tracks[0..36].replaceAll('D','=').replaceAll("F","");
        //log.info "track2:$track2";
        try {
            CardHolder cardHolder = new CardHolder(track2);
            return cardHolder;
        } catch (InvalidCardException e) {
            log.error("InvalidCardException:" + e.getMessage());
        }
        return null;
    }
}
