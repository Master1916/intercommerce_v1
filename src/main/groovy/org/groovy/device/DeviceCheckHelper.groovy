package org.groovy.device

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-30
 * Time : 下午7:46
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class DeviceCheckHelper {

    private String deviceMode = null;

    private String deviceNo = null;

    private Map<String,String> params = null;

    public DeviceCheckHelper(String deviceMode, String deviceNo, Map<String, String> params) {

        this.deviceMode = deviceMode;
        this.deviceNo = deviceNo;
        this.params = params;
    }

    public boolean switchCheck() {
        def check = checksum.find { deviceMode.matches(it.getKey()) }?.value;
        if (check) {
            DeviceCheck dc = (DeviceCheck) check.newInstance();
            return dc.check(deviceMode, deviceNo, params);
        }
        return false;
    }

    /*MAC对应关系*/
    def checksum = [
            "itroni21"        :       ChecksumItroni21,
            "itroni21b"       :       ChecksumItroni21b,
            "dh-103"          :       ChecksumLandim35,//鼎合
            "itron15-9"       :       ChecksumItron159,
            "landim35"        :       ChecksumLandim35,
            "hz-m20"          :       ChecksumHisense,
            "landim18"        :       ChecksumLandim35,
            "ty63250"         :       ChecksumItron159,
            "ty71249"         :       ChecksumItron159,
    ];

}
