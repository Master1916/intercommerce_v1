package org.groovy.util

import org.cnepay.customer.exception.CustomerException
import org.cnepay.customer.request.MobileAuthReq
import org.cnepay.customer.request.MobileCustomerReq
import org.cnepay.customer.response.MobileCustomerInfoResp
import org.cnepay.customer.service.CustomerService
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
 * Date : 2015/12/12
 * Time : 14:58
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
public class CustomerUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, CustomerUtil.getSimpleName())

    /**
     * 通过手机号获取客户相关信息
     *
     *
     * @param mobile 手机号
     * @return
     * @author zhangshb
     * @since 2015-12-12
     */
    static def findCustomerInfoByMobile(def mobile){
        log.info("findCustomerInfoByMobile params:${mobile}")
        def respInfo = [:];
        try{
            CustomerService customerService = SpringApplicationContext.getBean("customerService");
            MobileCustomerReq mobileCustomerReq = new MobileCustomerReq();
            mobileCustomerReq.setMobile(mobile);
            MobileCustomerInfoResp mobileCustomerInfoResp = customerService.findCustomerInfoByMobile(mobileCustomerReq);
            respInfo << [customerId : mobileCustomerInfoResp.customerId];
            respInfo << [customerName : mobileCustomerInfoResp.customerName];
            respInfo << [status : mobileCustomerInfoResp.status];
            respInfo << [name : mobileCustomerInfoResp.name];
            respInfo << [idenNo : mobileCustomerInfoResp.idenNo];
            respInfo << [mobile : mobileCustomerInfoResp.mobile];
            respInfo << [password : mobileCustomerInfoResp.password];
        }catch (CustomerException e){
            log.info("e.code=${e.code}")
            respInfo << [code : e.code];
        }catch (Exception e){
            log.error("dubbo服务异常：${e.getMessage()}");
        }
        log.info("respInfo=${respInfo}")
        respInfo;
    }

    /**
     * 填充客户信息
     *
     *
     * @param customerName 客户名称
     * @param mobile 手机号
     * @param password 密码
     * @return
     */
    static def addCustomerInfo(def customerName, def mobile, def password){
        def bool = false;
        try{
            CustomerService customerService = SpringApplicationContext.getBean("customerService");
            MobileCustomerReq mobileCustomerReq = new MobileCustomerReq();
            mobileCustomerReq.setCustomerName(customerName);
            mobileCustomerReq.setPassword(password);
            mobileCustomerReq.setNewPassword(password);
            mobileCustomerReq.setMobile(mobile);
            customerService.registerByMobile(mobileCustomerReq);
            bool = true;
        }catch (CustomerException e){
            log.info("e.code=${e.code}")
        }catch (Exception e){
            log.error("dubbo服务异常：${e.getMessage()}");
        }
        bool;
    }

    /**
     * 填充认证信息
     *
     *
     * @param customerId 客户id
     * @param mobile 手机号
     * @return
     * @author zhangshb
     * @since 2015-12-12
     */
    static def addAuthInfo(def mobile){
        def bool = false;
        try{
            CustomerService customerService = SpringApplicationContext.getBean("customerService");
            MobileAuthReq mobileAuthReq = new MobileAuthReq();
            mobileAuthReq.setCustomerId(findCustomerInfoByMobile(mobile)?.customerId);
            mobileAuthReq.setMobile(mobile);
            customerService.addAuthInfo(mobileAuthReq);
            bool = true;
        }catch (CustomerException e){
            log.info("e.code=${e.code}")
        }catch (Exception e){
            log.error("dubbo服务异常：${e.getMessage()}");
        }
        bool;
    }




}
