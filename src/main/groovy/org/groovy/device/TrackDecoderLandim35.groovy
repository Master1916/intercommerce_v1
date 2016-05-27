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
 * Time : 上午10:28
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class TrackDecoderLandim35 extends TrackDecoder{


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
        def encTrackBytes = ISOUtil.hex2byte(params.encTracks);
//        int size = encTrackBytes.length % 16 == 0 ? encTrackBytes.length / 16 : encTrackBytes.length / 16 + 1;
//        for(i in 1..size) {
//            if(i * 16 - 1 > encTrackBytes.length) {
//                def tunk = encTrackBytes[(i - 1) * 16..(encTrackBytes.length - 1)] as byte[];
//                def clear_tunk = handler.decryptData(tunk, clear_tdk);
//                System.arraycopy(clear_tunk, 0, encTrackBytes, (i - 1) * 16, tunk.length);
//            } else {
//                def tunk = encTrackBytes[(i - 1) * 16..(i * 16 - 1)] as byte[];
//                def clear_tunk = handler.decryptData(tunk, clear_tdk);
//                System.arraycopy(clear_tunk, 0, encTrackBytes, (i - 1) * 16, 16);
//            }
//        }
        try {
            encTrackBytes = doFinal(encTrackBytes,clear_tdk.encoded,false);
        } catch (Exception e){
            log.error("decrypt track2 exception:" + e.getMessage(),e);
            return null;
        }

        int byteCount = 0;
//        log.error("track2:" + new String(encTrackBytes) + "END");
        for(int index = 0; index < encTrackBytes.length;index ++){
            if(encTrackBytes[index] == 0){
                break;
            }
            byteCount ++;
        }
        byte[] newEncTrackBytes = new byte[byteCount];
        System.arraycopy(encTrackBytes,0,newEncTrackBytes,0,byteCount);
        String t = ISOUtil.hexString(newEncTrackBytes);
        def tracks = new String(ISOUtil.hex2byte(t.replaceAll("F","")));
        StringBuilder track = new StringBuilder();
        if(tracks.length() > 37) {
            track.append(tracks[0..36].replaceAll('D','='));
        } else {
            track.append(tracks.replaceAll('D','='));
        }
        try {
            CardHolder cardHolder = new CardHolder(track.toString());
            return cardHolder;
        } catch (InvalidCardException e){
            log.error("InvalidCardException:" + track.toString());
        }
        return null;
    }
}
