package org.groovy.device

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-6
 * Time : 下午5:18
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class DeviceKeyHelper {

    private String deviceMode = null;

    private String deviceNo = null;

    private Map<String,String> params = null;

    public DeviceKeyHelper(String deviceMode, String deviceNo, Map<String, String> params) {

        this.deviceMode = deviceMode;
        this.deviceNo = deviceNo;
        this.params = params;
    }

    public Map<String,String> loadKey(){
        def creator = deviceKeyCreator.find { deviceMode.matches(it.getKey()) }?.value;
        if (creator) {
            DeviceKey dk = (DeviceKey) creator.newInstance();
            return dk.initKey(deviceMode, deviceNo, params);
        }
        return null;
    }

    /*工作密钥对应关系*/
    def deviceKeyCreator = [
            "itroni21"        :       DeviceKeyItroni21,
            "itroni21b"       :       DeviceKeyItroni21b,
            "dh-103"          :       DeviceKeyItroni21b,//鼎合
            "itron15-9"       :       DeviceKeyLandim35,
            "landim35"        :       DeviceKeyLandim35,
            "hz-m20"          :       DeviceKeyLandim35,
            "landim18"        :       DeviceKeyItroni21b,//联迪M18
            "ty63250"         :       DeviceKeyItroni21b,//天喻105
            "ty71249"         :       DeviceKeyItroni21b,//天喻204
    ];

}
