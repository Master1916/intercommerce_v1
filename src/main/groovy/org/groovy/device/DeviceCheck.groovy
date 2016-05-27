package org.groovy.device

import org.groovy.common.Constants
import org.jpos.ext.security.MyJCEHandler
import org.jpos.ext.security.SoftSecurityModule
import org.jpos.iso.ISOUtil
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
 * Date : 15-12-1
 * Time : 下午1:24
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public abstract class DeviceCheck {

    protected Log log = new Log(NameRegistrar.getIfExists("logger.Q2") as Logger,this.getClass().getSimpleName());

    protected MyJCEHandler handler = new MyJCEHandler(Constants.PROVIDER);

    protected SecureKeyStore ks = (SecureKeyStore)NameRegistrar.get(Constants.KEY_STORE_KEY);

    protected SoftSecurityModule sm = (SoftSecurityModule)NameRegistrar.get(Constants.SMADAPTER_KEY);
    /**
     * MAC校验
     * @param deviceMode
     * @param deviceNo
     * @param params
     * @return
     */
    public abstract boolean check(String deviceMode,String deviceNo,Map<String,String> params);


    protected String getTAK(String deviceNo){
        return "ws." + deviceNo + ".tak";
    }

    protected String getTPK(String tid){
        return "tid." + tid + ".tpk";
    }

    protected String checkValues(params,int LEN){
        byte[] data = new byte[LEN];
        for(String key : params.keySet()) {
            if (key.equalsIgnoreCase("checksum")) continue;
            Object obj = params.get(key);
            if (obj instanceof String) {
                try {
                    byte[] s = obj.toString().getBytes("UTF-8");
                    int len = (s.length - 1) / LEN + 1;
                    for (int i = 0; i < len; i++) {
                        data = xor(data, s, i * LEN);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return ISOUtil.hexString(data);
    }

    protected byte[] xor (byte[] op1, byte[] op2, int pos2) {
        byte[] result = op1;
        for (int i = 0; i < result.length && i < op2.length - pos2; i++) {
            result[i] = (byte)(op1[i] ^ op2[i + pos2]);
        }
        return result;
    }

    protected byte[] xor(String value){
        byte[] vb = ISOUtil.hex2byte(value)
        byte[] vt = new byte[vb.length + 8]
        System.arraycopy(vb, 0, vt, 0,vb.length)
        int size = vt.length / 8
        byte[] res = new byte[8]
        for(int i=0;i<size;i++){
            byte[] temp = new byte[8]
            System.arraycopy(vt, i*8, temp, 0, 8)
            res = ISOUtil.xor(res, temp)
        }
        return res
    }

}
