package org.groovy.device

import org.groovy.common.Constants
import org.jpos.ext.security.MyJCEHandler
import org.jpos.ext.security.SoftSecurityModule
import org.jpos.security.SecureKeyStore
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.device
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-6
 * Time : 下午5:20
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public abstract class DeviceKey {

    protected Log log = new Log(NameRegistrar.getIfExists("logger.Q2") as Logger,this.getClass().getSimpleName());

    protected MyJCEHandler handler = new MyJCEHandler(Constants.PROVIDER);

    protected SecureKeyStore ks = (SecureKeyStore)NameRegistrar.get(Constants.KEY_STORE_KEY);

    protected SoftSecurityModule sm = (SoftSecurityModule)NameRegistrar.get(Constants.SMADAPTER_KEY);

    public abstract Map<String,String> initKey(String deviceMode, String deviceNo, Map<String, String> params);

    protected String getTPK(String tid){
        return "tid." + tid + ".tpk";
    }

    protected String getTMK(String deviceNo){
        return "ws." + deviceNo + ".tmk";
    }

    protected String getRSAKey(String deviceNo){
        return "ws." + deviceNo;
    }
}
