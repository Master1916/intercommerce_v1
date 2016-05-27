package org.groovy.service.common

import net.sf.json.JSONObject
import org.apache.commons.lang.StringUtils
import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.dictionary.DictionaryDao
import org.groovy.dao.merchant.BankAccountDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.MobileDao
import org.groovy.dao.user.SessionDao
import org.groovy.util.AccountAuthUtil
import org.groovy.util.DateUtil
import org.groovy.util.MerchantUtil
import org.groovy.util.ValidateUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.service.MobileUserService

import java.sql.Timestamp

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service.common
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-5
 * Time : 下午5:22
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class MobileUserServiceImpl implements MobileUserService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, MobileUserServiceImpl.getSimpleName())
    /**
     * 发送验证码
     * @param mobile 手机号
     * @return 若成功发送则返回验证码
     */
    public String sendValidateCode(String mobile) {
        log.info "发送验证码接口：  " + mobile
        MobileDao mobileDao = new MobileDao()
        //效验手机格式是否正确
        if (!ValidateUtil.checkMobileNo(mobile)) {
            return JSONObject.fromObject([
                    "code"   : 1,
                    "message": "手机格式不匹配",
            ]).toString()
        }
        //效验发送验证码的时间是否过于频繁
        def mobileIdentifyCode = mobileDao.findMobileIdentifyCodeByMobileNo(mobile)
        log.info "发送验证码接口： mobileIdentifyCode " + mobileIdentifyCode
        if (mobileIdentifyCode) {
            if (System.currentTimeMillis() - mobileIdentifyCode.date_created?.timestampValue().time <= Constants.TRANS_TIMEOUT) {
                log.info "发送验证码接口： 发送太频繁 "
                return JSONObject.fromObject([
                        "code"   : 1,
                        "message": "验证码请求时间过于频繁,请稍后再试",
                ]).toString()
            }
        }
        //效验发送验证码的请求次数是否过于频繁
        def sendMsgCount = mobileIdentifyCode ? mobileIdentifyCode.get_count : 0
        if (mobileIdentifyCode && DateUtil.getYYMMDDCurrTime() != mobileIdentifyCode?.date_created.timestampValue().format(Constants.YMD_DATE_FORMAT)) {
            sendMsgCount = 0
        }
        if (sendMsgCount >= Constants.MESSAGE_MAX_SENDCOUNT_DAY) {
            log.info "发送验证码接口： 发送次数太多 " + sendMsgCount
            return JSONObject.fromObject([
                    "code"   : 1,
                    "message": "今天发送短信过多",
            ]).toString()
        }
        //调用发送短信接口
        int idCode = Math.random() * 9000 + 1000
        def flag = true
//        def flag = Commons.newSendMsg(mobile, "验证码：${idCode}")
        if (flag) {
            sendMsgCount++
        } else {
            log.info "发送验证码接口： 验证码发送失败 " + sendMsgCount
            return JSONObject.fromObject([
                    "code"   : 1,
                    "message": "验证码发送失败",
            ]).toString()
        }
        //处理验证码信息表
        def uuid = UUID.randomUUID().toString().replaceAll('-', '')
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
            mobileIdentifyCode.status = 0
        } else {
            mobileIdentifyCode.status = 1
        }
        mobileDao.deleteWSIdentifyCodeByMobileNo(mobile)
        mobileDao.db.dataSet('ws_identify_code').add(mobileIdentifyCode)
        log.info "发送验证码接口： 新添加短消息信息 " + mobileIdentifyCode
        return JSONObject.fromObject([
                "code"        : 0,
                "message"     : "发送验证码成功,注意查收",
                "validateCode": idCode
        ]).toString()

    }

    /**
     * 根据用户ID以及session会话唯一标示,获取用户信息
     * @param userId 用户ID
     * @param sessionId 会话唯一标示
     * @return 返回用户状态，若会话在有效期内返回手机收单用户的详细信息
     *         返回信息以json结构，详细信息见接口文档
     */
    public String validateUser(String userId, String sessionId) {
        log.info "查询Session接口 ：用户ID-" + userId + " 会话ID-" + sessionId
        MerchantDao merchantDao = new MerchantDao()
        BankAccountDao bankAccountDao = new BankAccountDao()
        SessionDao sd = new SessionDao()
        def session = sessionId.split("-")
        if (session.size() != 2) {
            return JSONObject.fromObject([code: 1, message: '未知session格式']).toString()
        }
        //获取session ：判断session是否存在
        def userSession = sd.findSessionByNoAndUserId(session[0], userId)
        log.info "查询Session接口 ：userSession：" + userSession
        if (!userSession) {
            log.info "查询Session接口 ：未知session"
            return JSONObject.fromObject([code: 1, message: '未知session']).toString()
        }
        //超时判断
        if (!userSession.expiry_time) {
            log.info "查询Session接口 ：session过期"
            return JSONObject.fromObject([code: 1, message: 'session过期']).toString()
        }
        long c = System.currentTimeMillis()
        if (c > userSession.expiry_time.timestampValue().getTime()) {
            log.info "查询Session接口 ：session超时"
            return JSONObject.fromObject([code: 1, message: 'session超时']).toString()
        }
        def userInfo = [
                code   : 0,
                message: "成功",
                userId : userSession.user_name
        ]
        //判断是否是弱注册商户
        def acqUser = merchantDao.findCmPersonByMobileNo(userSession.user_name)
        log.info "查询Session接口 ：acqUser ：" + acqUser
        if (!acqUser) {
            userInfo << [userType: 0]
            log.info "查询Session接口 :userInfo:" + userInfo
            return JSONObject.fromObject(userInfo).toString()
        } else {
            userInfo << [userType: 1]
            def merchant = merchantDao.findMerchantByMobileNo(userSession.user_name)
            if (!merchant) {
                userInfo << [status: false]
                log.info "查询Session接口 ：merchant 不存在"
                return JSONObject.fromObject(userInfo).toString()
            }
            //T1商户详细信息
            def personal = merchantDao.findCmPersonalById(merchant.id);
            if (!personal) {
                userInfo << [status: false]
                log.info "查询Session接口 ：personal 不存在"
                return JSONObject.fromObject(userInfo).toString()
            }
            //T1账户信息
            def bankAccount = bankAccountDao.findBankAccountByMerchantId(merchant.id as String);
            if (!bankAccount) {
                userInfo << [status: false]
                log.info "查询Session接口 ：bankAccount 不存在"
                return JSONObject.fromObject(userInfo).toString()
            }
            //校验4审
            if (!MerchantUtil.isValidate(merchant, personal, bankAccount)) {
                userInfo << [status: false]
                log.info "查询Session接口 ：没过4审"
                return JSONObject.fromObject(userInfo).toString()
            }
        }
        userInfo << [status: true]
        userInfo << [idCard: acqUser.id_no]
        userInfo << [realName: (acqUser.real_name == null || 'null'.equalsIgnoreCase(acqUser.real_name)) ? acqUser.name : acqUser.real_name]
        log.info "查询Session接口 :userInfo:" + userInfo
        return JSONObject.fromObject(userInfo).toString()
    }

    /**
     * 验证同一身份四要素   (不带验证码)
     * @param userId 用户唯一标识
     * @param idCard 身份证号
     * @param realName 真实姓名
     * @param bankCard 银行卡号
     * @param mobile 银行预留手机号
     * @return
     */
    public String authIdentity(String userId, String idCard, String realName, String bankCard, String mobile) {
        log.info "验证同一身份四要素 ：用户ID-" + userId + " 身份证-" + idCard + " 真实姓名-" + realName + " 银行卡号-" + bankCard + " 银行预留手机号-" + mobile
        DictionaryDao dictDao = new DictionaryDao()
        def cardBin = dictDao.findCardbin(bankCard)

        if (!cardBin) {
            log.info "验证同一身份四要素 ：不支持的卡"
            return JSONObject.fromObject([code   : 1,
                                          message: "不支持的卡"
            ]).toString()
        }
        //查看是否是18家结算银行
        def bankCodeInfo = Commons.cardBinRule(cardBin.issuer_name as String, dictDao.findDictBankList())
        if (!bankCodeInfo) {
            log.info "验证同一身份四要素 ：不支持的结算银行" + cardBin.issuer_name
            return JSONObject.fromObject([code   : 1,
                                          message: "不支持的结算银行"
            ]).toString()
        }
        //先查询是否已经绑定此卡
        def certAuth = dictDao.findCertAuthByCard(bankCard as String)
        log.info "验证同一身份四要素 ：certAuth:" + certAuth
        if (!certAuth) {
            //没有鉴权信息 新建一条记录
            certAuth = dictDao.addCertAuth(mobile, bankCard, realName, bankCodeInfo.bankcode as String, bankCodeInfo.bankname as String, mobile, idCard)
        }
        def res = new AccountAuthUtil().sendMessage(certAuth.sn as String, bankCard, realName, mobile, idCard, bankCodeInfo.bankname as String, bankCodeInfo.bankcode as String)
        log.info "验证同一身份四要素 ：res:" + res
        if (res?.code == "M2017") {
            //如果认证中。则等10秒查询数据库看是否启用回调
            Thread.sleep(10 * 1000L)
            certAuth = dictDao.findCertAuthById(certAuth.id as String)
            log.info "验证同一身份四要素 ：certAuth 10sec :" + certAuth
            if (certAuth.status == 1) {
                //状态是已经验证成功：
                return JSONObject.fromObject([code   : 0,
                                              message: "验证成功"
                ]).toString()
            }
            //如果状态不是成功。再次校验
            res = new AccountAuthUtil().sendMessage(certAuth.sn as String, bankCard, realName, mobile, idCard, bankCodeInfo.bankname as String, bankCodeInfo.bankcode as String)
            log.info "验证同一身份四要素 ：res 10SEC :" + res
            if (res?.code == "M0000") {
                //如果认证成功
                certAuth.user_name = userId
                certAuth.account_name = realName
                certAuth.mobile = mobile
                certAuth.iden_no = idCard
                certAuth.status = 1
                dictDao.update(certAuth)
                return JSONObject.fromObject([code   : 0,
                                              message: "验证成功"
                ]).toString()
            } else {
                //失败直接响应
                return JSONObject.fromObject([code   : 1,
                                              message: '卡片验证失败'
                ]).toString()
            }
        } else if (res?.code == "M0000") {
            //认证成功
            certAuth.user_name = userId
            certAuth.account_name = realName
            certAuth.mobile = mobile
            certAuth.iden_no = idCard
            certAuth.status = 1
            dictDao.update(certAuth)
            return JSONObject.fromObject([code   : 0,
                                          message: "验证成功"
            ]).toString()
        } else {
            //失败直接响应(超时也直接返回失败)
            return JSONObject.fromObject([code   : 1,
                                          message: '卡片验证失败'
            ]).toString()
        }

        return null
    }

    /**
     * 验证同一身份四要素   (不带验证码)
     * @param userId 用户唯一标识
     * @param idCard 身份证号
     * @param realName 真实姓名
     * @param bankCard 银行卡号
     * @param mobile 银行预留手机号
     * @param code 验证码
     * @return
     */
    public String authIdentity(String userId, String idCard, String realName, String bankCard, String mobile, String code) {
        log.info "验证同一身份四要素 ：验证码 ： " + code + " 手机号：" + mobile
        //校验code
        MobileDao mobileDao = new MobileDao()
        def mobileIdCode = mobileDao.findeMobileIdentifyCodeByKsnNo(mobile)
        log.info "验证同一身份四要素 ：mobileIdCode ： " + mobileIdCode
        //验证码的发送
        if (!StringUtils.isBlank(code)) {
            if (mobileIdCode?.validate_count > Constants.IDCODE_LIMIT_TIMES) {
                log.info "验证同一身份四要素 ：今天验证次数过多 ： " + mobileIdCode?.validate_count
                return JSONObject.fromObject([code   : 5,
                                              message: "今天验证次数过多,请明天重试!"
                ]).toString()
            }
            if (code != mobileIdCode?.id_code) {
                log.info "验证同一身份四要素 ：输入验证码错误 ： " + mobileIdCode?.validate_count
                mobileIdCode?.validate_count += 1
                mobileDao.update(mobileIdCode)
                return JSONObject.fromObject([code   : 6,
                                              message: "输入验证码错误"
                ]).toString()
            }

        } else {
            return JSONObject.fromObject([code   : 4,
                                          message: "校验码为空"
            ]).toString()
        }
        return authIdentity(userId, idCard, realName, bankCard, mobile)
    }
}
