package org.groovy.device

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-2
 * Time : 下午8:07
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class ChecksumItroni21b extends DeviceCheck{
    @Override
    boolean check(String deviceMode, String deviceNo, Map<String, String> params) {
        ChecksumItron159 itron159 = new ChecksumItron159();
        return itron159.check(deviceMode,deviceNo,params);
    }
}
