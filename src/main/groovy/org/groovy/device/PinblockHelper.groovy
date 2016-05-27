package org.groovy.device

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-4
 * Time : 下午7:15
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class PinblockHelper {
    private String deviceMode = null;

    private String deviceNo = null;

    private Map<String,String> params = null;

    public PinblockHelper(String deviceMode, String deviceNo, Map<String, String> params) {

        this.deviceMode = deviceMode;
        this.deviceNo = deviceNo;
        this.params = params;
    }

    public String checkPin() {
        def check = pincheck.find { deviceMode.matches(it.getKey()) }?.value;
        if (check) {
            CheckPinblock cp = (CheckPinblock) check.newInstance();
            return cp.check(deviceMode, deviceNo, params);
        }
        return null;
    }

    /*PIN对应关系*/
    def pincheck = [
            "itroni21"        :       CheckPinblockItroni21,
            "itroni21b"       :       CheckPinblockItroni21,
            "dh-103"          :       CheckPinblockItroni21,
            "itron15-9"       :       CheckPinblockLandim35,
            "landim35"        :       CheckPinblockLandim35,
            "hz-m20"          :       CheckPinblockLandim35,
            "landim18"        :       CheckPinblockItroni21,
            "ty63250"         :       CheckPinblockItroni21,
            "ty71249"         :       CheckPinblockLandim35,
    ];

}
