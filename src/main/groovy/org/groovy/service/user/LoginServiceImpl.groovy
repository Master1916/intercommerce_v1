package org.groovy.service.user

import org.cnepay.customer.exception.CustomerException
import org.cnepay.customer.request.MobileCustomerReq
import org.cnepay.customer.service.CustomerConst
import org.cnepay.customer.service.CustomerService
import org.groovy.common.Commons
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.TerminalDao
import org.groovy.util.AlgorithmUtil
import org.groovy.util.PosUtil
import org.groovy.util.ResponseUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.common.SpringApplicationContext
import org.mobile.mpos.service.user.LoginService
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service
 * Date : 15-11-14
 * Time : 下午7:40
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
@Service
public class LoginServiceImpl implements LoginService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, LoginServiceImpl.getSimpleName())

    /**
     * 基于2.0版本客户端登陆
     * @param request
     * @return
     */
    public Object login(HttpServletRequest request, HttpServletResponse response) {
        //引用商户相关的数据库操作
        MerchantDao merchantDao = new MerchantDao();
        def params = Commons.parseRequest(request);
        //登录名
        String loginName = params.loginName;
        log.info("find loginName=" + loginName);
        //密碼
        String password = AlgorithmUtil.encodeBySha1((params.password as String) + ".woshua");
        log.info("current password=" + password);
        //获取收单商户登录信息
        def acqUser = merchantDao.findLoginNameByMobileNo(loginName);
        log.info("find acqUser=" + acqUser);
        //TODO 调用远程用户中心接口验证用户
        CustomerService customerService;
        MobileCustomerReq mobileCustomerReq = new MobileCustomerReq();
        def operator = merchantDao.findOperatorByLoginName(acqUser?.login_name);
        def result = [:]
        try {
            try {
                customerService = SpringApplicationContext.getBean("customerService")
                mobileCustomerReq.setMobile(loginName);
                mobileCustomerReq.setPassword(password);
                customerService.loginByMobile(mobileCustomerReq)
                if (!acqUser) {
                    //用户系统中没有开通交易的:返回登录成功
                    log.info "用户系统中没有开通交易"
                    if (!Commons.addSession(request, response, loginName, params.position as String, params.appVersion as String)) {
                        log.info("can not create session for " + loginName);
                        return ResponseUtil.failResponse("INIT_SESSION_ERROR", "初始化会话失败");
                    }
                    def posMerchatMap = isPosMerchant(loginName);
                    if(!posMerchatMap || posMerchatMap.isEmpty()){
                        return ResponseUtil.failResponse("NOT_FIND_POS_MERCHANT", "无法获取传统商户状态");
                    }
                    result << posMerchatMap;
                    result << isMobileMerchant(operator);
                    return Commons.success(result, "登录成功");
                }
                log.info "用户系统中存在且也开通了业务"
            } catch (CustomerException e) {
                def message = e.code;
                log.info 'message :' + message
                if (CustomerConst.PROPERTY_PASSWORD_IS_NOT_CORRECT.equalsIgnoreCase(message)) {
                    log.info "密码错误!!!"
                    return ResponseUtil.failResponse(message, "密码错误");
                }
                if (acqUser) {
                    log.info "老用户!"
                    log.info(password + " =pwd=" + operator.mobile_pwd)
                    if (password != operator.mobile_pwd) {
                        return ResponseUtil.failResponse("ILLEGAL_USER_OR_PASSWORD", "用户或密码错误");
                    }
                    //老用户：需要同步收单信息到用户系统中
                    mobileCustomerReq.setCustomerName(loginName)
                    try {
                        customerService.registerByMobile(mobileCustomerReq)
                    } catch (CustomerException e1) {
                        log.error 'loginName:' + loginName + ", password:" + password + "-" + e1.code
                        return ResponseUtil.failResponse("SYSTEM_ERROR", "系统异常，请稍后登录");
                    } catch (Exception e1) {
                        log.error 'loginName:' + loginName + ", password:" + password + "--" + e1.getMessage()
                        return ResponseUtil.failResponse("SYSTEM_ERROR", "系统异常，请稍后登录");
                    }
                } else {
                    log.info "用户不存在!"
                    return ResponseUtil.failResponse("USER_NOT_EXIST", "用户不存在");
                }
            }
            //记录本地用户登录信息
            log.info("find operator=" + operator);
            if (!operator) {
                return ResponseUtil.failResponse("USER_NOT_EXIST", "用户不存在");
            }
            log.info(password + " =pwd=" + operator.mobile_pwd)
            if (password != operator.mobile_pwd) {
                return ResponseUtil.failResponse("ILLEGAL_USER_OR_PASSWORD", "用户或密码错误");
            }
            result << isMobileMerchant(operator);
            def posMerchat = isPosMerchant(loginName);
            if(!posMerchat || posMerchat.isEmpty()){
                return ResponseUtil.failResponse("NOT_FIND_POS_MERCHANT", "无法获取传统商户状态");
            }
            result << posMerchat;
            if (!Commons.addSession(request, response, loginName, params.position as String, params.appVersion as String)) {
                log.info("can not create session for " + loginName);
                return ResponseUtil.failResponse("INIT_SESSION_ERROR", "初始化会话失败");
            }
        } catch (Exception e1) {
            log.error 'loginName:' + loginName + ", password:" + password + "---" + e1.getMessage()
            return ResponseUtil.failResponse("SYSTEM_ERROR", "系统异常，请稍后登录");
        }
        return Commons.success(result, "登录成功");
    }

    @Override
    Object heartBeat() {
        return Commons.success(null, "成功");
    }

    private Map<String, Object> isMobileMerchant(operator) {
        TerminalDao terminalDao = new TerminalDao();
        def mobileMerchant = [:];
        def device = terminalDao.findKSNByMerchantId(operator?.merchant_id);
        log.info("find device=" + device);
        if (device) {
            mobileMerchant << [isMobileMerchant: true]
        } else {
            mobileMerchant << [isMobileMerchant: false]
        }
    }

    private Map<String, Object> isPosMerchant(String userId) {
        def posMerchant = [:];
        def pos = PosUtil.posRealNameAuthStatus(userId);
        if(!pos.isEmpty()){
            if(pos.status == 3){
                posMerchant << [isPosMerchant   :   true];
                posMerchant << [idCard          :   pos?.idCard];
                posMerchant << [realName        :   pos?.realName];
                posMerchant << [posStatus       :   pos?.status];
            } else {
                posMerchant << [isPosMerchant   :   false];
                posMerchant << [posStatus       :   pos?.status];
            }
        }
        return posMerchant;
    }

}