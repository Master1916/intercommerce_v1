package org.groovy.device

import org.groovy.common.Constants
import org.jpos.ext.security.MyJCEHandler
import org.jpos.ext.security.SoftSecurityModule
import org.jpos.security.SecureDESKey
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
 * Date : 15-12-4
 * Time : 下午7:13
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public abstract class CheckPinblock {

    protected Log log = new Log(NameRegistrar.getIfExists("logger.Q2") as Logger,this.getClass().getSimpleName());

    protected MyJCEHandler handler = new MyJCEHandler(Constants.PROVIDER);

    protected SecureKeyStore ks = (SecureKeyStore)NameRegistrar.get(Constants.KEY_STORE_KEY);

    protected SoftSecurityModule sm = (SoftSecurityModule)NameRegistrar.get(Constants.SMADAPTER_KEY);

    public abstract String check(String deviceMode,String deviceNo,Map<String,String> params);

    protected String getRSAKey(String deviceNo){
        return "ws." + deviceNo;
    }

    protected String getTPK(String tid){
        return "tid." + tid + ".tpk";
    }

    protected SecureDESKey getFrontZPK(){
        def zpk = NameRegistrar.getIfExists(Constants.FRONT_ZPK_KEY) as SecureDESKey;
        if (!zpk) {
            zpk = ks.getKey(Constants.FRONT_ZPK_KEY);
            NameRegistrar.register(Constants.FRONT_ZPK_KEY, zpk);
        }
        return zpk
    }
}
