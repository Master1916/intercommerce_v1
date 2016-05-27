package org.groovy.service.common

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.MobileDao
import org.groovy.dao.trans.TransDao
import org.groovy.util.CustomerUtil
import org.groovy.util.DateUtil
import org.groovy.util.ValidateUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.common.MobileMessageService

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.sql.Timestamp


/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/17
 * Time : 16:09
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
public class MobileMessageImpl implements MobileMessageService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, MobileMessageImpl.getSimpleName())

    /**
     * 获取验证码
     *
     * @param mobile 手机号 [数据格式：15801376995]
     * @param appVersion App版本信息 [数据格式：ios.ZFT.1.1.813]
     * @return
     * @author zhangshb
     * @since 2015-11-18
     */
    public Object sendMobileMessage(HttpServletRequest request, HttpServletResponse response) {

        //引用手机相关的数据库操作
        MobileDao mobileDao = new MobileDao();
        MerchantDao merchantDao = new MerchantDao();
        //获取请求参数
        def params = Commons.parseRequest(request);
        def mobile = params.mobile?.trim();
        def type = params.type;

        if (type) {
            def acqUser = merchantDao.findCmPersonByMobileNo(mobile as String);
            log.info("acqUser=${acqUser}");
            def customerInfo = CustomerUtil.findCustomerInfoByMobile(mobile);
            if (0 == customerInfo.size()) {
                return Commons.fail(null, 'SEND_FAIL', Constants.error_code_mapping.SEND_FAIL);
            }
            if (type == 'registe') {
                if (customerInfo?.customerId || acqUser) {
                    return Commons.fail(null, 'USER_EXIST', "用户已存在");
                }
            }else if(type == 'forget'){
                log.info("mobile_not_existing.equalsIgnoreCase(customerInfo.code)=${"mobile_not_existing".equalsIgnoreCase(customerInfo.code)}")
                if ("mobile_not_existing".equalsIgnoreCase(customerInfo.code)) {
                    if(null == acqUser){
                        return Commons.fail(null, 'USER_NOT_EXIST', "用户不存在");
                    }
                }
            }
        }
        //效验手机格式是否正确
        if (!ValidateUtil.checkMobileNo(mobile)) {
            return Commons.fail(null, 'MOBILE_ERROR', Constants.error_code_mapping.MOBILE_ERROR);
        }

        //效验发送验证码的时间是否过于频繁
        def mobileIdentifyCode = mobileDao.findMobileIdentifyCodeByMobileNo(mobile);
        def sendMsgCount = mobileIdentifyCode ? mobileIdentifyCode.get_count : 0;
        if (mobileIdentifyCode) {
            if (System.currentTimeMillis() - mobileIdentifyCode.date_created?.timestampValue()?.time <= Constants.TRANS_TIMEOUT) {
                return Commons.fail(null, 'REQUEST_TOO_OFFEN', Constants.error_code_mapping.REQUEST_TOO_OFFEN);
            }
            //效验发送验证码的请求次数是否过于频繁
            if (mobileIdentifyCode && DateUtil.getYYMMDDCurrTime() != mobileIdentifyCode?.date_created?.timestampValue()?.format(Constants.YMD_DATE_FORMAT)) {
                sendMsgCount = 0
            }
            if (sendMsgCount >= Constants.MESSAGE_MAX_SENDCOUNT_DAY) {
                return Commons.fail(null, 'SEND_MSG_FAILS', Constants.error_code_mapping.SEND_MSG_FAILS);
            }
        }

        //调用发送短信接口
        int idCode = Math.random() * 9000 + 1000
        def flag = Commons.newSendMsg(mobile, "验证码：${idCode}");
        if (flag) {
            sendMsgCount++;
        } else {
            return Commons.fail(null, 'SEND_FAIL', '验证码发送失败');
        }

        //处理验证码信息表
        def uuid = UUID.randomUUID().toString().replaceAll('-', '');
        Cookie cookie = new Cookie('idCode', uuid);
        mobileIdentifyCode = [
                'id'             : mobileDao.getWSIdentifyCode(),
                'id_code'        : idCode,
                'date_created'   : new Timestamp(new Date().time),
                'cookie_value'   : uuid,
                'mobile_no'      : mobile,
                'ksn_no'         : mobile,
                'validate_status': '0',
                'get_count'      : sendMsgCount,
                'validate_count' : 0,
        ]
        if (!flag) {
            mobileIdentifyCode.status = 0;
        } else {
            mobileIdentifyCode.status = 1;
        }

        def deleteResult = mobileDao.deleteWSIdentifyCodeByMobileNo(mobile);
        if (deleteResult != 0) {
            log.info("delete success:${mobileIdentifyCode}");
        }
        mobileDao.db.dataSet('ws_identify_code').add(mobileIdentifyCode);
        response.addCookie(cookie);
        return Commons.success(null, '发送验证码成功,注意查收');

    }

    @Override
    Object sendTransMessage(HttpServletRequest request, HttpServletResponse response) {
        MerchantDao merchantDao = new MerchantDao();
        TransDao transDao = new TransDao();
        MobileDao mobileDao = new MobileDao();
        //处理请求
        def param = Commons.parseRequest(request)
        def user = Commons.initUserRequestParams(request)
        //原交易请求号
        def origReqNo = param.reqNo
        //原始交易金额
        def amount = param.amount
        String terminalNo = param.terminalNo
        String merchantNo = param.merchantNo
        def batchNo = param.batchNo
        def mobile = param.mobile
        if (!ValidateUtil.checkMobileNo(mobile)) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '手机号不正确');
        }
        //获取T1商户信息
        def merchant = merchantDao.findMerchantByMobileNo(user.user_id)
        if (!merchant) {
            return Commons.fail(null, "MERCHANT_NOT_EXIST", "商户不存在")
        }
        //批次号
        def batch = "${batchNo}".padLeft(6, '0')
        //流水号
        def trace = "${origReqNo}".padLeft(6, '0')
        //查询本地交易是否存在
        log.info("batch=${batch},trace=${trace},terminalNo=${terminalNo},merchantNo=${merchantNo},amount=${amount}")
        def oriWSTrans = transDao.findTransCurrent(batch, trace, terminalNo , merchantNo, amount as Long)
        if (!oriWSTrans) {
            return Commons.fail(null, "TRANS_MISSING", "找不到交易");
        }
        //是否发送过短信
        def transMessage = mobileDao.findTransQueryMessage(mobile, oriWSTrans.id)
        if (transMessage) {
            return Commons.success(null, '交易小票短信已经发送,注意查收')
        }
        if (oriWSTrans.resp_code == '00') {
            //添加记录短信发送
            mobileDao.addTransQueryMeaasge(mobile, oriWSTrans.id)
            //短信信息
            String message = "【中汇·掌富通】感谢您于" + oriWSTrans.trans_date_time?.timestampValue()?.format(Constants.DATE_FORMAT_COMPLEX) +
                    "在商户【" + merchant.merchant_name + "】处消费${(amount as Long) / 100}元，点击" + Constants.TRANS_MESSAGE_CHECK + " 获取交易小票详情"
            log.info "message = " + message
            def flag = Commons.newSendMsg(mobile, message);
            if (flag) {
                return Commons.success(null, '交易小票短信发送成功,注意查收')
            } else {
                return Commons.fail(null, 'SEND_FAIL', '发送失败');
            }
        } else {
            return Commons.fail(null, "TRANS_MISSING", "交易失败");
        }

    }
}
