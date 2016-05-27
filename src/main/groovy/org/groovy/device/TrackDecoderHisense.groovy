package org.groovy.device

import org.jpos.core.CardHolder

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-3
 * Time : 上午11:57
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class TrackDecoderHisense extends TrackDecoder{
    @Override
    CardHolder decode(String deviceMode, String deviceNo, Map<String, String> params) {
        return new TrackDecoderLandim35().decode(deviceMode,deviceNo,params);
    }
}
