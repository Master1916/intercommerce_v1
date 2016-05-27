package org.groovy.util

import com.cnepay.app.AppMservice
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.common.SpringApplicationContext

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.util
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/12/13
 * Time : 16:31
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
public class PosUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, PosUtil.getSimpleName())

    /**
     * 通过手机号获取传统POS实名认证相关信息
     *
     *
     * @param mobile 手机号
     * @return
     * @author zhangshb
     * @since 2015-12-13
     */
    static def posRealNameAuthStatus(def mobile){
        log.info("posRealNameAuthStatus params:${mobile}")
        def respInfo = [:];
        try{
            AppMservice appMservice = SpringApplicationContext.getBean("appMservice");
            String authResultStr = appMservice.posUserInfo(mobile);
            log.info("authResultStr=${authResultStr}");
            def resMap = ConvertUtil.strConvertToMap(authResultStr);
            log.info("resMap=${resMap}");
            def resCode = resMap.code as int;
            if(0 == resCode){
                def status = resMap.status as int;
                respInfo << [code : resCode];
                respInfo << [message : resMap.message];
                respInfo << [userId : resMap.userId];
                respInfo << [status : status];
                if(3 == status){
                    respInfo << [idCard : resMap.idCard];
                    respInfo << [realName : resMap.realName];
                }
            }
        }catch (Exception e){
            log.error("传统POS的dubbo服务异常：${e.getMessage()}");
        }
        log.info("respInfo=${respInfo}")
        respInfo;
    }

}
