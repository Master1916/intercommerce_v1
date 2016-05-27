package org.groovy.device

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
 * Time : 下午1:19
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class TrackDecoderItroni21 extends TrackDecoder{
    @Override
    CardHolder decode(String deviceMode, String deviceNo, Map<String, String> params) {

        String encTracks = params.encTracks;
        def tdk = ks.getKey(getTAK(deviceNo));
        def clear_tmk = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, new byte[16]);
        def lmk_tmk = sm.encryptToLMK(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_ZMK, clear_tmk);
        def clear_tdk = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, handler.decryptData(sm.exportKey(tdk, lmk_tmk), clear_tmk));

        def random = encTracks[-16..-1]
        def left = handler.encryptData(ISOUtil.hex2byte(random), clear_tdk);
        def xor = ISOUtil.xor(ISOUtil.hex2byte(random), ISOUtil.hex2byte("FFFFFFFFFFFFFFFF"));
        def right = handler.encryptData(xor, clear_tdk);
        def reslut = ISOUtil.concat(left, right);
        def key = handler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, reslut);
//        log.info("result=" + ISOUtil.hexString(reslut));
        def tracks = handler.decryptData(ISOUtil.hex2byte(encTracks), key);
//        log.info("tracks:"+ISOUtil.hexString(tracks))
        String  trackstr = ISOUtil.hexString(tracks);
        trackstr = trackstr[0..trackstr.indexOf("F")]
//        log.info("trackstr:"+trackstr)
        trackstr = trackstr.replaceAll('D','=').replaceAll("F","");
        if(trackstr.length() > 37) {
            trackstr = trackstr[0..36];
        }
//        log.info "track2:$trackstr"
        try {
            CardHolder cardHolder = new CardHolder(trackstr);
            return cardHolder;
        } catch (InvalidCardException e) {
            log.error("InvalidCardException:" + trackstr);
        }
        return null
    }
}
