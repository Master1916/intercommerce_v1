package org.groovy.service.user

import org.apache.commons.lang.BooleanUtils
import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.dictionary.DictionaryDao
import org.groovy.util.AccountAuthUtil
import org.groovy.util.ValidateUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.user.CardManagerService

import javax.servlet.http.HttpServletRequest

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service.trans
 * Date : 15-11-30
 * Time : 下午6:28
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class CardManagerServiceImpl implements CardManagerService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, CardManagerServiceImpl.getSimpleName())

    /**
     * 绑定/解绑用户银行卡
     *
     * @param request
     * @return
     */
    public Object bindBankCard(HttpServletRequest request) {
        DictionaryDao dictionaryDao = new DictionaryDao();
        //处理请求
        def param = Commons.parseRequest(request)
        def user = Commons.initUserRequestParams(request)
        //解绑银行卡
        if (BooleanUtils.toBoolean(param.isDelete as String) && param.cardIds) {
            def cardParam = new StringBuffer();
            param.cardIds.split('\\s+').each {
                cardParam.append(", '" + it + "'")
            }
            def a = dictionaryDao.unbundlingBankCards(user.user_id as String, cardParam.toString().substring(1))
            log.info "=============解绑了=======" + a

            return Commons.success([count: a], '解绑用户银行卡成功')
        }
        // 获取手机号
        String mobile = param.mobile as String
        //校验手机号
        if (!ValidateUtil.checkMobileNo(mobile)) {
            return (Commons.fail(null, 'ILLEGAL_ARGUMENT', '手机号格式不正确'))
        }
        //查询用户绑卡数目
        int count = dictionaryDao.listBankCards(user.user_id as String)?.size() as int
        //最多可以绑定30张卡
        if (count > 30) {
            return (Commons.fail(null, 'ILLEGAL_ARGUMENT', '绑卡数量超限'))
        }
        //如果没有绑定，则根据卡号查询CardBin
        def bankInfo = dictionaryDao.findCardbin(param.bankCard as String)
        //查询不到cardBin：不支持此卡
        if (!bankInfo) {
            log.info '不支持的卡：' + param.bankCard
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '不支持的卡')
        }
        //查看是否是18家结算银行
        def bankCodeInfo = Commons.cardBinRule(bankInfo.issuer_name as String, dictionaryDao.findDictBankList())
        if (!bankCodeInfo) {
            log.info '不支持的结算银行：' + param.bankCard
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '不支持的结算银行')
        }
        //卡片类型
        String cardType = null;
        if (bankInfo.card_type && "debit".equalsIgnoreCase(bankInfo.card_type as String)) {
            log.info '借记卡不用绑定：' + param.bankCard
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '借记卡不用绑定')
        } else if (bankInfo.card_type && "credit".equalsIgnoreCase(bankInfo.card_type as String)) {
            cardType = '信用卡';
        } else {
            log.info '未知卡类型：' + bankInfo?.card_type
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '未知卡类型')
        }

        def acqUser = Commons.findUserInfoByMobileNo(user.user_id as String)
        if (!acqUser) {
            return Commons.fail(null, "USER_NOT_EXIST", "用户不存在")
        }

        def cardbind = dictionaryDao.findBankCard(param.bankCard as String)
        if (cardbind && !( cardbind.mobile == mobile && cardbind.iden_no == acqUser.idNumber && acqUser.name == cardbind.account_name)) {
            log.info '已经绑定卡：' + mobile + "--" + param.bankCard as String
            return Commons.fail(null, 'CARD_BEEN_BOUND', '绑卡失败')
        }

        //返回参数
        def result = [
                bankCard       : param.bankCard,
                bankName       : bankCodeInfo.bankname,
                bankIndex      : bankCodeInfo?.banklogoindex,
                card_type      : cardType,
                bankAccountName: acqUser.name,
        ];
        //验卡流程
        //去审核表查看是否已经审核
        def certAuth = dictionaryDao.findCertAuthByCard(param.bankCard as String)
        if (certAuth) {
            //有校验信息查看状态
            if (certAuth.status == 1) {
                log.info "有成功的鉴权信息"
                def res = addBindCard(param, acqUser, bankCodeInfo, user, cardType, dictionaryDao)
                if (res.success) {
                    return Commons.success(result, res.message)
                } else {
                    return Commons.fail(null, res.code, res.message)
                }
            } else {
                log.info "有失败的鉴权信息： 重新校验"
                def res = validate(certAuth, param, acqUser, mobile, bankCodeInfo, user, cardType, dictionaryDao)
                log.info "有失败的鉴权信息： 重新校验 " + res
                if (res.success) {
                    return Commons.success(result, res.message)
                } else {
                    return Commons.fail(null, res.code, res.message)
                }
            }
        } else {
            log.info "不存在绑定信息： 添加一条在鉴权"
            //记录上行流水
            certAuth = dictionaryDao.addCertAuth(mobile, param.bankCard, acqUser.name as String, bankCodeInfo.bankcode as String, bankCodeInfo.bankname as String, user.user_id, acqUser.idNumber as String)
            def res = validate(certAuth, param, acqUser, mobile, bankCodeInfo, user, cardType, dictionaryDao)
            log.info "不存在绑定信息： 添加一条在鉴权 ： " + res
            if (res.success) {
                return Commons.success(result, res.message)
            } else {
                return Commons.fail(null, res.code, res.message)
            }
        }
    }

    def validate(
            def certAuth,
            def param, def acqUser, def mobile, def bankCodeInfo, def user, def cardType, DictionaryDao dictionaryDao) {
        //状态不是成功：重新校验
        def res = new AccountAuthUtil().sendMessage(certAuth.sn as String, param.bankCard as String, acqUser.name as String, mobile as String, acqUser.idNumber as String, bankCodeInfo.bankname as String, bankCodeInfo.bankcode as String)
        log.info "发送请求：" + res
        if (res?.code == "M2017") {
            //如果认证中。则等10秒查询数据库看是否启用回调
            log.info "before:" + new Date().format(Constants.DATE_FORMAT)
            Thread.sleep(10 * 1000L)
            log.info "after :" + new Date().format(Constants.DATE_FORMAT)
            certAuth = dictionaryDao.findCertAuthById(certAuth.id as String)
            if (certAuth.status == 1) {
                //状态是已经验证成功：查看是否有绑卡记录
                return addBindCard(param, acqUser, bankCodeInfo, user, cardType, dictionaryDao)
            }
            //如果状态不是成功。再次校验
            res = new AccountAuthUtil().sendMessage(certAuth.sn as String, param.bankCard as String, acqUser.name as String, mobile as String, acqUser.idNumber as String, bankCodeInfo.bankname as String, bankCodeInfo.bankcode as String)
            log.info "再次发送请求：" + res
            if (res?.code == "M0000") {
                //如果认证成功
                certAuth.user_name = user.user_id
                certAuth.account_name = acqUser.name
                certAuth.mobile = mobile
                certAuth.iden_no = acqUser.idNumber
                certAuth.status = 1
                dictionaryDao.update(certAuth)
                return addBindCard(param, acqUser, bankCodeInfo, user, cardType, dictionaryDao)
            } else {
                //失败直接响应
                return [success: false,
                        code   : 'AUTH_FAILED',
                        message: '卡片验证失败'
                ]
            }
        } else if (res?.code == "M0000") {
            def result = addBindCard(param, acqUser, bankCodeInfo, user, cardType, dictionaryDao)
            //认证成功
            certAuth.user_name = user.user_id
            certAuth.account_name = acqUser.name
            certAuth.mobile = mobile
            certAuth.iden_no = acqUser.idNumber
            certAuth.status = 1
            dictionaryDao.update(certAuth)
            return result
        } else {
            //失败直接响应(超时也直接返回失败)
            return [success: false,
                    code   : 'AUTH_FAILED',
                    message: '卡片验证失败'
            ]
        }
    }

    def addBindCard(
            def param, def acqUser, def bankCodeInfo, def user, def cardType, DictionaryDao dictionaryDao) {
        //状态是已经验证成功：查看是否有绑卡记录
        def bandCard = dictionaryDao.findBindCardByCardNo(param.bankCard as String)
        if (bandCard) {
            log.info " 有绑卡记录"
            //有绑卡记录
            if (bandCard.user_id == user.user_id) {
                //自己绑定的卡：更新状态
                if (bandCard.delete_flag == 1) {
                    //如果卡绑定状态为未校验，则修改状态
                    bandCard.delete_flag = 0
                    dictionaryDao.update(bandCard)
                }
                return [success: true,
                        message: '绑卡成功']
            }
        }
        log.info " 没有绑卡记录：添加一条"
        //没有绑卡记录：添加一条
        dictionaryDao.addBankCard(user.user_id, param.bankCard, acqUser.name, bankCodeInfo.banklogoindex, cardType,
                bankCodeInfo.bankname)
        return [success: true,
                message: '绑卡成功']
    }

    /**
     * 获取商户绑定银行卡列表
     *
     * @param request
     * @return
     */
    public Object listBandCard(HttpServletRequest request) {
        DictionaryDao dictionaryDao = new DictionaryDao();
        //处理请求
        def user = Commons.initUserRequestParams(request)
        def bindCards = dictionaryDao.listBankCards(user.user_id as String)
        //返回绑定信息
        def cards = []
        bindCards.each {
            cards << [
                    cardId   : it.id,
                    accountNo: it.bank_card,
                    bankName : it.bank_name,
                    status   : it.status,
                    bankIndex: it.bank_index,
            ]
        }
        return Commons.success([list: cards], '获取成功')
    }
}
