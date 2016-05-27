package org.groovy.service.user

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.MobileDao
import org.groovy.util.AlgorithmUtil
import org.groovy.util.CustomerUtil
import org.groovy.util.ValidateUtil
import org.jpos.space.Space
import org.jpos.space.SpaceFactory
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.user.RegisterService
import org.springframework.stereotype.Service

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service
 * Date : 15-11-14
 * Time : 下午7:40
 * To change this template use File | Settings | File and Code Templates.
 */
@Service
public class RegisterServiceImpl implements RegisterService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, RegisterServiceImpl.getSimpleName())

    @Override
    Object register(HttpServletRequest request) {

        //引用数据库相关操作
        MerchantDao merchantDao = new MerchantDao();
        MobileDao mobileDao = new MobileDao();
        //处理请求参数
        def param = Commons.parseRequest(request)
        //获取手机号
        def mobile = param.mobile

        //校验手机号
        if (!ValidateUtil.checkMobileNo(mobile)) {
            log.debug "mobile not invalid: ${mobile}"
            return Commons.fail(null, 'MOBILE_ERROR', Constants.error_code_mapping.MOBILE_ERROR)
        }

        //TODO 调用远程接口，验证是否用户已存在，若存在需要提供新手机号注册，不存在则检测收单是否是手机收单商户
        def customerInfo = CustomerUtil.findCustomerInfoByMobile(mobile);
        if(0 == customerInfo.size()){
            return Commons.fail(null, 'REGISTER_FAIL', "注册失败");
        }
        if(!customerInfo.code){
            return Commons.fail(null, 'MOBILE_REGISTERED', "手机号已被注册,请更换");
        }

        //在本地获取手机号：如果存在，则手机号已经注册
        def userInfo = merchantDao.findLoginNameByMobileNo(mobile)
        if (userInfo) {
            return Commons.fail(null, 'MOBILE_REGISTERED', "手机号已被注册,请更换")
        }

        //校验验证码是否有效
        def mobileIdCode = mobileDao.findMobileIdentifyCodeByMobileNo(mobile)
        if (!mobileIdCode) {
            return Commons.fail(null, 'MOBILE_NOT_VALIDATE', Constants.error_code_mapping.MOBILE_NOT_VALIDATE)

        }
        //验证码只能输入5次
        def validateCount = mobileIdCode?.validate_status;
        if (validateCount >= Constants.IDCODE_LIMIT_TIMES) {
            return Commons.fail(null, 'ID_CODE_VALIDATE_COUNT_OVER_LIMIT', Constants.error_code_mapping.IDCODE_VALIDATE_COUNT)
        }
        //校验验证码的值，如果不对则失误次数+1
        def idCode = param.idCode?.trim();
        if (idCode != mobileIdCode.id_code) {
            validateCount++;
            mobileIdCode.validate_status = validateCount;
            mobileDao.update(mobileIdCode);
            return Commons.fail(null, 'ID_CODE_ERROR', Constants.error_code_mapping.IDCODE_ERROR)
        }
        //校验密码
        if (!ValidateUtil.checkPassword(param.password)) {
            log.info "password not invalid"
            return Commons.fail(null, 'PASSWORD_NOT_VALIDATE', Constants.error_code_mapping.PASSWORD_NOT_VALIDATE)
        }
        Cookie[] c = request.getCookies();
        log.info(c)
        String value
        if (c) {
            c.each {curr->
                if(curr.getName() == 'idCode'){
                    value = curr.getValue();
                    log.info 'COOKIE : ' + value
                }
            }
        }
        if (!value || value != mobileIdCode.cookie_value) {
            return Commons.fail(null, 'AFRESH_IDENTIFY', "请重新获取验证码")
        }
        //验证码添加入缓存
        Space space = SpaceFactory.getSpace();
        if (space.rdp(mobileIdCode.cookie_value)) {
            return Commons.fail(null, 'PROCESSING', Constants.error_code_mapping.PROCESSING)
        } else {
            space.out(mobileIdCode.cookie_value, new Date(), 1000 * 30);
        }

        try {
            //TODO 访问远程接口填充注册信息
            def bool = CustomerUtil.addCustomerInfo(mobile, mobile, AlgorithmUtil.encodeBySha1(param.password+".woshua"));
            if(!bool){
                return Commons.fail(null, 'REGISTER_FAIL', "注册失败");
            }

            //TODO 访问远程接口填充认证信息
            def isBool = CustomerUtil.addAuthInfo(mobile);
            if(!isBool){
                return Commons.fail(null, 'REGISTER_FAIL', "注册失败");
            }
            return Commons.success(null, '祝贺您成功注册.');
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Commons.fail(null, 'REGISTER_FAIL', "注册失败");
        }
    }
}