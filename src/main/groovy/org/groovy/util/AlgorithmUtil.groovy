package org.groovy.util

import org.apache.commons.codec.digest.DigestUtils
import org.jpos.iso.ISOUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.util
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-27
 * Time : 下午7:56
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
class AlgorithmUtil {
    //日志
    static log = new Log(NameRegistrar.getIfExists("logger.Q2") as Logger, AlgorithmUtil.getSimpleName());

    static encodeBySha1(String input){
        try {
            return ISOUtil.byte2hex(DigestUtils.sha(input));
        } catch (e){
            log.error("sha1 exception:" + e.getMessage(),e);
        }
        return  null;
    }

    static encodeBySha1(byte[] input){
        return ISOUtil.byte2hex(DigestUtils.sha(input));
    }
}
