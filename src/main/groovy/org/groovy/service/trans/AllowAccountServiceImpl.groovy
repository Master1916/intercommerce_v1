package org.groovy.service.trans

import org.groovy.common.Commons
import org.groovy.dao.merchant.AgencyDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.trans.TransDao
import org.groovy.util.ResponseUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.trans.AllowAccountService

import javax.servlet.http.HttpServletRequest

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service.trans
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-30
 * Time : 下午6:28
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class AllowAccountServiceImpl implements AllowAccountService {
    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, AllowAccountServiceImpl.getSimpleName())

    @Override
    Object AllowAccountD0(HttpServletRequest request) {
        MerchantDao merchantDao = new MerchantDao();
        AgencyDao agencyDao = new AgencyDao();
        TransDao transDao = new TransDao()

        def user = Commons.initUserRequestParams(request)
        log.info "user.user_id = " + user.user_id
        //T1商户信息
        def merchant = merchantDao.findMerchantByMobileNo(user.user_id)
        if (!merchant) {
            return ResponseUtil.failResponse("MERCHANT_NOT_EXIST", "商户不存在");
        }
        def merchantTrade = merchantDao.findMerchantTradeZero(merchant.id);
        if (!merchantTrade || merchantTrade.agency_status != 1 || merchantTrade.merchant_status != 1) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', "未开通及时付功能")
        }
        def agencyDeposit = agencyDao.agencySecurityDeposit(merchant.agency_id);
        if (!agencyDeposit) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '未设置代理商限额')
        }
        //D0商户成功交易的总金额
        def merchantAmount = transDao.sumAmountD0DailyByMerchant(merchantTrade.merchant_id_tzero, new Date());
        //查询D0当日交易最大交易限额:分为单位
        def merchantSettle = Long.parseLong(transDao.findMerchantTradeSettleInfo(merchantTrade.merchant_id_tzero)?.max_settle_amount as String) * 100;
        def merchantDayAmount = merchantSettle;

        //TODO 修改代理商金额限制
        def agencyAmount = 0;
        def agencySurplusMoney = 0;//代理商可以使用的D0剩余金额
        if ((agencyDeposit.deposit_source == 1 && !agencyDeposit.share_amount) || agencyDeposit.deposit_source == 0) {
            //中汇垫支\或代理商垫支
            agencyAmount = transDao.sumAmountDailyByAgency(merchant.agency_id);
            log.info "代理商D0当日交易金额：" + agencyAmount + ",代理商或中汇垫资:" + agencyDeposit.amount
            //代理商D0当日剩余金额
            agencySurplusMoney = agencyDeposit.amount - agencyAmount;
        } else if (agencyDeposit.deposit_source == 1) {
            //服务商出资且共享
            //所有共享过该大代理商金额的代理商列表
            def agencys = transDao.listAgencySecurityDeposit(merchant.agency_id);
            //大代理商的共享金额
            long share_amount = Long.parseLong(agencyDeposit.share_amount as String);
            //大代理商垫资
            long agency_amount = Long.parseLong(agencyDeposit.amount as String);
            //大代理商总金额（总限额）
            long allAmount = share_amount + agency_amount;
            if (!agencys) {
                //代理商列表为空，获取该大代理商下商户使用的总金额
                agencyAmount = transDao.sumAmountDailyByAgency(merchant.agency_id);
            } else {
                // 代理商列表不为空，获取该大代理商以及其他共享的代理商的商户使用的总金额
                StringBuilder agencyBuff = new StringBuilder();
                agencyBuff.append((merchant.agency_id as String))
                agencys.each {
                    agencyBuff.append("," + (it.agency_id as String))
                }
                agencyAmount = transDao.sumAmountDailyByAgencys(agencyBuff.toString());
            }
            log.info "代理商交易金额：" + agencyAmount + ",最终限制额:" + allAmount
            //总限额减去所有共享商户使用的交易总金额：该大代理商当天交易的最大交易限额
            agencySurplusMoney = allAmount - agencyAmount;
        } else if (agencyDeposit.deposit_source == 2) {
            //使用其他代理商共享金额的代理商
            log.info "共享金额的代理商:" + agencyDeposit.share_agency_id
            //代理商共享出的金额中已经被自己使用的金额
            long agencyMainAmount = transDao.sumAmountDailyByAgency(agencyDeposit.share_agency_id) as long;
            log.info "商户:" + merchant.merchant_no
            //所有共享过该大代理商金额的代理商列表
            def agencys = transDao.listAgencySecurityDeposit(agencyDeposit.share_agency_id);
            log.info "所有共享代理商:" + agencys
            if (!agencys) {
                //如果共享的代理商下没有子列表：则是无效代理商
                return Commons.fail(null, 'ILLEGAL_ARGUMENT', '不合法代理商')
            } else {
                //所有代理商列表拼接成字符串
                StringBuilder agencyBuff = new StringBuilder();
                log.info "agency:" + agencys;
                agencys.each {
                    agencyBuff.append((it.agency_id as String) + ",")
                }
                agencyBuff.deleteCharAt(agencyBuff.length() - 1);
                log.info "agency:" + agencyBuff.toString();
                //查询代理商列表中商户一共使用了多少金额
                def subAgencyAmount = transDao.sumAmountDailyByAgencys(agencyBuff.toString()) as long;
                //该主代理商的代理商共享的金额信息
                def mainAgencyDeposit = agencyDao.agencySecurityDeposit(agencyDeposit.share_agency_id);
                //主代理商的共享金额
                long share_amount = Long.parseLong(mainAgencyDeposit.share_amount as String);
                //主代理商的代理商金额
                long agency_amount = Long.parseLong(mainAgencyDeposit.amount as String);
                log.info "主代理商：" + agencyMainAmount + ",其他代理商:" + subAgencyAmount
                log.info "共享金额：" + share_amount + ",其他代理商:" + subAgencyAmount
                // 计算代理商D0当日剩余金额
                if (agencyMainAmount <= agency_amount) {
                    agencySurplusMoney = share_amount - subAgencyAmount;
                } else {
                    long more = agencyMainAmount - agency_amount;
                    agencySurplusMoney = share_amount - more - subAgencyAmount;
                }
            }
        }
        def result = [:];
        //用商户当日交易最大金额减去D0商户成功交易的总金额：当日商户剩余可用金额
        def merchantShowAccount = merchantDayAmount - merchantAmount;
        result << [merchantAccount: merchantShowAccount];
        result << [agencyAccount: agencySurplusMoney]
        def show = merchantShowAccount > agencySurplusMoney ? agencySurplusMoney : merchantShowAccount;
        result << [showAccount: show > 0 ? show : 0]
        return Commons.success(result)
    }

    @Override
    Object AllowAccountT1(HttpServletRequest request) {
        MerchantDao merchantDao = new MerchantDao();
        TransDao transDao = new TransDao()
        def user = Commons.initUserRequestParams(request)
        log.info "user.user_id = " + user.user_id
        //T1商户信息
        def merchant = merchantDao.findMerchantByMobileNo(user.user_id)
        if (!merchant) {
            return ResponseUtil.failResponse("MERCHANT_NOT_EXIST", "商户不存在");
        }
        //查询当日T1消费总金额
        return Commons.success([amount: transDao.sumAmountT1DailyByMerchant(merchant.id,new Date())], "获取成功")
    }
}
