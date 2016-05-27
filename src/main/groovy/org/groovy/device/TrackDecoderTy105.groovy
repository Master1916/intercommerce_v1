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
 * Author : zhangshb
 * User : Administrator
 * Date : 2016/3/1
 * Time : 15:05
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class TrackDecoderTy105 extends TrackDecoder {
    @Override
    CardHolder decode(String deviceMode, String deviceNo, Map<String, String> params) {
        try {
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
            log.info "tracks:$tracks";
            if(tracks.length() > 37){
                tracks = tracks[0..36];
            }
            def track2 = tracks.replaceAll('D', '=').replaceAll('F', '')
            log.info "track2:$track2";
            CardHolder cardHolder = new CardHolder(track2);
            return cardHolder;
        } catch (InvalidCardException e) {
            log.error("InvalidCardException:" + e.getMessage());
        } catch (Exception e) {
            log.error("Exception:" + e.getMessage());
        }
        return null;
    }
}
