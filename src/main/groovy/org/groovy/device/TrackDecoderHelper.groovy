package org.groovy.device

import org.jpos.core.CardHolder

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-2
 * Time : 下午8:32
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class TrackDecoderHelper {

    private String deviceMode = null;

    private String deviceNo = null;

    private Map<String,String> params = null;

    public TrackDecoderHelper(String deviceMode, String deviceNo, Map<String, String> params) {

        this.deviceMode = deviceMode;
        this.deviceNo = deviceNo;
        this.params = params;
    }

    public CardHolder decode() {
        def check = decoders.find { deviceMode.matches(it.getKey()) }?.value;
        if (check) {
            TrackDecoder td = (TrackDecoder) check.newInstance();
            return td.decode(deviceMode,deviceNo,params);
        }
        return null;
    }

    /*TRACK对应关系*/
    def decoders = [
            "itroni21"        :       TrackDecoderItroni21,
            "itroni21b"       :       TrackDecoderItroni21b,
            "dh-103"          :       TrackDecoderItroni21b,//鼎合
            "itron15-9"       :       TrackDecoderItron159,
            "landim35"        :       TrackDecoderLandim35,
            "hz-m20"          :       TrackDecoderHisense,
            "landim18"        :       TrackDecoderLandim35,
            "ty63250"         :       TrackDecoderTy105,
            "ty71249"         :       TrackDecoderTy105,
    ];

}
