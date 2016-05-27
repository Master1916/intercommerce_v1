package org.groovy.service.trans

import org.apache.commons.lang.BooleanUtils
import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.dictionary.DictionaryDao
import org.groovy.dao.merchant.AgencyDao
import org.groovy.dao.merchant.BankAccountDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.DeviceDao
import org.groovy.dao.terminal.RiskValueDao
import org.groovy.dao.terminal.TerminalDao
import org.groovy.dao.trans.TransDao
import org.groovy.device.DeviceCheckHelper
import org.groovy.device.PinblockHelper
import org.groovy.device.TrackDecoderHelper
import org.groovy.util.MerchantUtil
import org.groovy.util.ResponseUtil
import org.groovy.util.TransUtil
import org.groovy.util.ValidateUtil
import org.jpos.core.CardHolder
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOUtil
import org.jpos.tlv.TLVList
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.trans.AcqCommonService
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletRequest
import java.text.ParseException
import java.text.SimpleDateFormat

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
public class AcqCommonServiceImpl implements AcqCommonService {
    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, AcqCommonServiceImpl.getSimpleName())


    @Override
    public Object sale(HttpServletRequest request, MultipartFile signature) {

    }

    private def checkRiskTrans(String amount, String cardNo, String terminalNo, String productId, String modelId) {
        RiskValueDao riskValueDao = new RiskValueDao();
        def riskValue = riskValueDao.findRiskValueByProductIdAndModelId(productId, modelId);
        if (!riskValue) {
            return false;
        }
        def result = [];
        long currentAmount = Long.parseLong(amount);
        if (currentAmount > riskValue.each_max) {
            result << ["TR_TRAN_AMOUNT_TOO_MUCH"];
            result << ["很抱歉, 交易被拒绝, 交易金额过高"]
            return result;
        }
        if (currentAmount < riskValue.each_min) {
            result << ["TR_TRAN_AMOUNT_TOO_SMALL"];
            result << ["很抱歉, 交易被拒绝, 交易金额过低"]
            return result;
        }
        if (Commons.isDebitCard(cardNo)) {
            return false;
        }
        if (currentAmount > riskValue.credit_card_each_max) {
            result << ["TR_TRAN_AMOUNT_TOO_MUCH"];
            result << ["很抱歉, 交易被拒绝, 交易金额过高"]
            return result;
        }
        TransDao trans = new TransDao();
        if (riskValue.credit_card_terminal_daily_max) {
            long dayAmount = trans.sumAmountDailyByTerminalNo(terminalNo) as long;
            if (dayAmount + currentAmount > riskValue.credit_card_terminal_daily_max) {
                result << ["TR_TRAN_AMOUNT_TOO_MUCH"];
                result << ["很抱歉, 交易被拒绝, 超出当日限额"]
                return result;
            }
        }

        if (riskValue.credit_card_terminal_month_max) {
            long monthAmount = trans.sumAmountMonthByTerminalNo(terminalNo) as long;
            if (monthAmount + currentAmount > riskValue.credit_card_terminal_month_max) {
                result << ["TR_TRAN_AMOUNT_TOO_MUCH"];
                result << ["很抱歉, 交易被拒绝, 超出当日限额"]
                return result;
            }
        }

        if (riskValue.credit_card_limit_time) {
            def t = trans.findLastTransByTerminalNo(terminalNo, cardNo);
            if (t) {
                long time_out = System.currentTimeMillis() - t.trans_date_time?.timestampValue().time;
                if (time_out <= riskValue.credit_card_limit_time) {
                    result << ["REQUEST_TOO_OFFEN"];
                    result << ["很抱歉, 交易被拒绝, 交易太频繁"]
                    return result;
                }
            }
        }
        return false;
    }

    private
    def checkDayZeroAmount(String trace, String batch, String agencyId, String merchantId, long amount, CardHolder cardHolder,
                           def cardbin, def param) {
        MerchantDao merchantD0 = new MerchantDao();
        AgencyDao agencyDao = new AgencyDao();
        agencyDao.db = merchantD0.db;
        DictionaryDao dictionary = new DictionaryDao();
        dictionary.db = merchantD0.db;
        TransDao transDao = new TransDao();
        transDao.db = merchantD0.db;
        TerminalDao terminalDao = new TerminalDao();
        terminalDao.db = merchantD0.db;
        def result = null;
        merchantD0.withTransaction {
            if (!checkDayZeroTransTime()) {
                result = Commons.fail(null, "OVER_TRANS_TIME", "非及时付时间,请查询及时付业务规则");
                return;
            }

            def agencySecurityDeposit = agencyDao.agencySecurityDeposit(agencyId);
            if (!agencySecurityDeposit) {
                result = Commons.fail(null, "AGENCY_NOT_RULE", "代理商未设置交易限额");
                return;
            }
            log.info("agencySecurityDeposit=" + agencySecurityDeposit + 'agencyId ' + agencyId);
            def mobileTrans = dictionary.findTransParam("t0_mobileTrans_control");
            log.info("mobileTrans=" + mobileTrans);
            boolean isCheck = BooleanUtils.toBoolean(mobileTrans.value as String);
            def tradMerchant = merchantD0.findMerchantFinishedTradeZero(merchantId);
            if (!tradMerchant) {
                result = Commons.fail(null, "ILLEGAL_ARGUMENT", "未开通及时付");
                return;
            }

            def merchant = merchantD0.findCmMerchantById(merchantId);
            if (!merchant) {
                result = Commons.fail(null, "ILLEGAL_ARGUMENT", "未发现及时付信息");
                return;
            }

            //中汇垫资
            if (agencySecurityDeposit.deposit_source == 0 && isCheck) {
                def min_setting = dictionary.findTransParam("t0_zh_singleTMoney_min");
                def max_setting = dictionary.findTransParam("t0_zh_singleTMoney_max");
                log.info "中汇垫资--最大值:" + max_setting + ",最小值:" + min_setting;
                Long max = max_setting == null ? 50000 : max_setting.value as Long;
                Long min = min_setting == null ? 2000 : min_setting.value as Long;
                long merchantEachMax = Long.parseLong(tradMerchant.each_max as String);
                if (agencySecurityDeposit.each_max) {
                    merchantEachMax = merchantEachMax > Long.parseLong(agencySecurityDeposit.each_max as String) ? Long.parseLong(agencySecurityDeposit.each_max as String) : merchantEachMax;
                }
                max = merchantEachMax > max * 100 ? max * 100 : merchantEachMax;
                log.info "单笔最大值:" + max;
                if (min > max / 100) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制设置有误,单笔最小金额为${min}元,最大为${max / 100}元,详情咨询代理商!");
                    return;
                }
                if (amount < min * 100 || amount > max) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制在" + min + "元-" + max / 100 + "元,请查询及时付业务规则!");
                    return;
                }

                def agencyMax = dictionary.findTransParam("t0_zh_oneDayTMoney_max");
                def sigCardAmount = transDao.findSumAmountDailyByAgencyAndCard(agencyId, cardHolder.pan);
                long agencyEachMax = agencySecurityDeposit.each_day_max ? Long.parseLong(agencySecurityDeposit.each_day_max as String) : Long.parseLong(agencyMax.value as String) * 100;

                if (agencyEachMax < amount + sigCardAmount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '超过最大限额度:' + agencyEachMax / 100 + '元');
                    return;
                }

                //代理商垫资
            } else if (agencySecurityDeposit.deposit_source == 1 && isCheck) {
                def min_setting = dictionary.findTransParam("t0_agency_singleTMoney_min");
                def max_setting = dictionary.findTransParam("t0_agency_singleTMoney_max");
                log.info "代理商垫资--最大值:" + max_setting + ",最小值:" + min_setting;
                Long max = max_setting == null ? 50000 : max_setting.value as Long;
                Long min = min_setting == null ? 2000 : min_setting.value as Long;
                long merchantEachMax = Long.parseLong(tradMerchant.each_max as String);
                log.info("merchantEachMax=${merchantEachMax}")
                if (agencySecurityDeposit.each_max) {
                    log.info("agencySecurityDeposit.each_max=${agencySecurityDeposit.each_max}")
                    merchantEachMax = merchantEachMax > Long.parseLong(agencySecurityDeposit.each_max as String) ? Long.parseLong(agencySecurityDeposit.each_max as String) : merchantEachMax;
                }
                max = merchantEachMax > max * 100 ? max * 100 : merchantEachMax;
                log.info "单笔最大值:" + max;
                if (min > max / 100) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制设置有误,单笔最小金额为${min}元,最大为${max / 100}元,详情咨询代理商!");
                    return;
                }
                if (amount < min * 100 || amount > max) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制在" + min + "元-" + max / 100 + "元,请查询及时付业务规则!");
                    return;
                }

                def agencyMax = dictionary.findTransParam("t0_agency_oneDayTMoney_max");

                long agencyEachMax = agencySecurityDeposit.each_day_max ? Long.parseLong(agencySecurityDeposit.each_day_max as String) : Long.parseLong(agencyMax.value as String) * 100;
                def sigCardAmount = transDao.findSumAmountDailyByAgencyAndCard(agencyId, cardHolder.pan);
                log.info " agencyEachMax " + agencyEachMax + "  amount " + amount + "  sigCardAmount " + sigCardAmount
                if (agencyEachMax < amount + sigCardAmount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '超过最大限额度:' + agencyEachMax / 100 + '元');
                    return;
                }
                //其他代理商共享
            } else if (isCheck) {
                def min_setting = dictionary.findTransParam("t0_others_singleTMoney_min");
                def max_setting = dictionary.findTransParam("t0_others_singleTMoney_max");
                log.info "其他代理商垫资--最大值:" + max_setting + ",最小值:" + min_setting;
                Long max = max_setting == null ? 50000 : max_setting.value as Long;
                Long min = min_setting == null ? 2000 : min_setting.value as Long;
                long merchantEachMax = Long.parseLong(tradMerchant.each_max as String);
                if (agencySecurityDeposit.each_max) {
                    merchantEachMax = merchantEachMax > Long.parseLong(agencySecurityDeposit.each_max as String) ? Long.parseLong(agencySecurityDeposit.each_max as String) : merchantEachMax;
                }
                max = merchantEachMax > max * 100 ? max * 100 : merchantEachMax;
                log.info "单笔最大值:" + max;
                if (min > max / 100) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制设置有误,单笔最小金额为${min}元,最大为${max / 100}元,详情咨询代理商!");
                    return;
                }
                if (amount < min * 100 || amount > max) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制在" + min + "元-" + max / 100 + "元,请查询及时付业务规则!");
                    return;
                }

                def agencyMax = dictionary.findTransParam("t0_others_oneDayTMoney_max");

                long agencyEachMax = agencySecurityDeposit.each_day_max ? Long.parseLong(agencySecurityDeposit.each_day_max as String) : Long.parseLong(agencyMax.value as String) * 100;
                def sigCardAmount = transDao.findSumAmountDailyByAgencyAndCard(agencyId, cardHolder.pan);

                if (agencyEachMax < amount + sigCardAmount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '超过最大限额度:' + agencyEachMax / 100 + '元');
                    return;
                }
            }
            def terminal = terminalDao.findTerminalByMerchantId(merchantId);

            def merchantAmount = transDao.findSumAmountDailyByMerchant(tradMerchant.merchant_id_tzero);
            def merchantSettle = agencyDao.findMerchantTradeSettleInfo(tradMerchant.merchant_id_tzero);
            long merchantMaxDayAmount = Long.parseLong(merchantSettle.max_settle_amount as String) * 100;
            log.info "当前金额：" + (merchantAmount + amount) + ",商户限制额:" + merchantMaxDayAmount

            if (merchantAmount + amount > merchantMaxDayAmount) {
                result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '商户金额超限:' + merchantMaxDayAmount / 100 + "元");
                return;
            }

            long agencyAmount = 0;
            log.info "初始化agencyAmount:" + agencyAmount;
            if ((agencySecurityDeposit.deposit_source == 1 && !agencySecurityDeposit.share_amount) || agencySecurityDeposit.deposit_source == 0) {
                agencyAmount = transDao.findSumAmountDailyByAgency(agencyId);
                if (agencyAmount + amount > agencySecurityDeposit.amount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '代理商金额超限:' + agencySecurityDeposit.amount / 100 + "元");
                    return;
                }
            } else if (agencySecurityDeposit.deposit_source == 1) {
                def agencys = agencyDao.listAgencySecurityDeposit(agencyId);
                long share_amount = Long.parseLong(agencySecurityDeposit.share_amount as String);
                long agency_amount = Long.parseLong(agencySecurityDeposit.amount as String);
                long allAmount = share_amount + agency_amount;
                if (!agencys) {
                    agencyAmount = transDao.findSumAmountDailyByAgency(agencyId);
                } else {
                    StringBuilder agencyBuff = new StringBuilder();
                    agencyBuff.append((agencyId as String) + ",")
                    agencys.each {
                        agencyBuff.append((it.agency_id as String) + ",")
                    }
                    agencyBuff.deleteCharAt(agencyBuff.length() - 1);
                    log.info "代理商ID：" + agencyBuff.toString()
                    agencyAmount = transDao.findSumAmountDailyByAgencys(agencyBuff.toString());
                }
                log.info "代理商交易金额：" + agencyAmount + ",最终限制额:" + allAmount
                if (agencyAmount + amount > allAmount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '代理商金额超限[' + allAmount / 100 + '元]');
                    return;
                }
            } else if (agencySecurityDeposit.deposit_source == 2) {
                long agencyMainAmount = transDao.findSumAmountDailyByAgency(agencySecurityDeposit.share_agency_id) as long;
                def agencys = agencyDao.listAgencySecurityDeposit(agencySecurityDeposit.share_agency_id);
                if (!agencys) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '不合法代理商');
                    return;
                } else {
                    StringBuilder agencyBuff = new StringBuilder();
                    agencys.each {
                        agencyBuff.append((it.agency_id as String) + ",")
                    }
                    agencyBuff.deleteCharAt(agencyBuff.length() - 1);
                    log.info "代理商ID：" + agencyBuff.toString()
                    def subAgencyAmount = transDao.findSumAmountDailyByAgencys(agencyBuff.toString()) as long;
                    def mainAgencyDeposit = agencyDao.agencySecurityDeposit(agencySecurityDeposit.share_agency_id);
                    long share_amount = Long.parseLong(mainAgencyDeposit.share_amount as String);
                    long agency_amount = Long.parseLong(mainAgencyDeposit.amount as String);
                    log.info "共享：" + share_amount + ",当前的交易金额:" + subAgencyAmount + ",主代理商交易金额:" + agencyMainAmount
                    if (agencyMainAmount <= agency_amount) {
                        if (subAgencyAmount + amount > share_amount) {
                            result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '代理商金额超限[' + share_amount / 100 + '元]');
                            return;
                        }
                    } else {
                        long more = agencyMainAmount - agency_amount;
                        if (subAgencyAmount + amount > share_amount - more) {
                            result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '代理商金额超限[' + share_amount / 100 + '元]');
                            return;
                        }
                    }
                }
            }
            //上行交易流水预存
            def trans = transDao.addWsTrans(amount, batch, trace, cardHolder.pan, param.appVersion, param.currency, param.position, param.signatureUri, terminal, param.ksnNo, cardbin)

            try {
                //构建报文并且接受响应报文
                ISOMsg resultResponse = TransUtil.sendAndRecive(TransUtil.packageSaleMessage(batch, trace, cardHolder, amount, param.pin, terminal.merchant_no as String, terminal.terminal_no as String, param.cardSerialNum as String, param.icData as String))
                trans.comp_status = 1
                //TODO 后处理

                if (!resultResponse) {
                    result = Commons.fail(null, 'TRANS_SERVER_TIME_OUT', '交易超时');
                    return;
                }

                def code = resultResponse.getString(39)
                trans.resp_code = code
                trans.reference_no = resultResponse.getString(37)
                trans.auth_no = resultResponse.getString(38)
                def f55 = null;
                //响应55域数据
                if (resultResponse.hasField(55)) {
                    f55 = ISOUtil.hexString(resultResponse.getBytes(55));
                }

                def now = new Date();

                if (code == '00') {
                    trans.trans_status = 1;
                    def res = [
                            reqNo       : param.reqNo ?: null,
                            merchantName: merchant.merchant_name,
                            merchantNo  : terminal.merchant_no,
                            terminalNo  : terminal.terminal_no,
                            operatorNo  : '01',
                            resultCode  : '00',
                            cardNoWipe  : trans.card_no_wipe,
                            amount      : amount,
                            currency    : param.currency ?: 'CNY',
                            issuer      : cardbin ? cardbin.issuer_name : null,
                            voucherNo   : trace,
                            batchNo     : batch,
                            transTime   : now.format(Constants.DATE_FORMAT_YEAR) + resultResponse.getString(13) + resultResponse.getString(12),
                            refNo       : resultResponse.getString(37),
                            authNo      : resultResponse.hasField(38) ? resultResponse.getString(38) : ' ',
                            script      : f55 ?: ''
                    ];
                    result = Commons.success(res);
                    return;
                } else {
                    String errorMessage = Constants.trans_error_code.get(code) ?: code + ":未知错误";
                    result = Commons.fail([reqNo: param.reqNo ?: null, script: f55 ?: '', resultCode: code], code, errorMessage)
                    return;
                }
            } finally {
                trans.comp_status = 2
                transDao.update(trans)
                terminal.trace_no = param.reqNo as int
                terminalDao.update(terminal)
            }
        }
        return result;
    }

    private
    def checkDayZeroSecAmount(String trace, String batch, String agencyId, String merchantId, long amount, CardHolder cardHolder,
                              def cardbin, def param) {
        MerchantDao merchantD0 = new MerchantDao();
        AgencyDao agencyDao = new AgencyDao();
        agencyDao.db = merchantD0.db;
        DictionaryDao dictionary = new DictionaryDao();
        dictionary.db = merchantD0.db;
        TransDao transDao = new TransDao();
        transDao.db = merchantD0.db;
        TerminalDao terminalDao = new TerminalDao();
        terminalDao.db = merchantD0.db;
        def result = null;
        merchantD0.withTransaction {
            if (!checkDayZeroTransTime()) {
                result = Commons.fail(null, "OVER_TRANS_TIME", "非及时付时间,请查询及时付业务规则");
                return;
            }

            //获取道理上交易限额
            def agencySecurityDeposit = agencyDao.agencySecurityDeposit(agencyId);
            if (!agencySecurityDeposit) {
                result = Commons.fail(null, "AGENCY_NOT_RULE", "代理商未设置交易限额");
                return;
            }
            //交易限额开关
            boolean isCheck = BooleanUtils.toBoolean(dictionary.findTransParam("t0_mobileTrans_control")?.value as String);

            def tradMerchant = merchantD0.findMerchantFinishedTradeZeroSec(merchantId);
            if (!tradMerchant) {
                result = Commons.fail(null, "ILLEGAL_ARGUMENT", "未开通及时付");
                return;
            }
            def merchant = merchantD0.findCmMerchantById(merchantId);
            if (!merchant) {
                result = Commons.fail(null, "ILLEGAL_ARGUMENT", "未发现及时付信息");
                return;
            }

            if (agencySecurityDeposit.deposit_source == 0 && isCheck) {
                //中汇垫资
                def min_setting = dictionary.findTransParam("t0_sec_zh_singleTMoney_min");
                def max_setting = dictionary.findTransParam("t0_sec_zh_singleTMoney_max");
                log.info "中汇垫资--最大值:" + max_setting + ",最小值:" + min_setting;
                Long max = max_setting == null ? 50000 : max_setting.value as Long;
                Long min = min_setting == null ? 9000 : min_setting.value as Long;
                long merchantEachMax = Long.parseLong(tradMerchant.each_max as String);
                if (agencySecurityDeposit.each_max) {
                    merchantEachMax = merchantEachMax > Long.parseLong(agencySecurityDeposit.each_max as String) ? Long.parseLong(agencySecurityDeposit.each_max as String) : merchantEachMax;
                }
                max = merchantEachMax > max * 100 ? max * 100 : merchantEachMax;
                log.info "单笔最大值:" + max;
                if (min > max / 100) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制设置有误,单笔最小金额为${min}元,最大为${max / 100}元,详情咨询代理商!");
                    return;
                }
                if (amount < min * 100 || amount > max) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制在" + min + "元-" + max / 100 + "元,请查询及时付业务规则!");
                    return;
                }

                def agencyMax = dictionary.findTransParam("t0_zh_oneDayTMoney_max");
                def sigCardAmount = transDao.findSumAmountDailyByAgencyAndCard(agencyId, cardHolder.pan);
                long agencyEachMax = agencySecurityDeposit.each_day_max ? Long.parseLong(agencySecurityDeposit.each_day_max as String) : Long.parseLong(agencyMax.value as String) * 100;

                if (agencyEachMax < amount + sigCardAmount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '超过最大限额度:' + agencyEachMax / 100 + '元');
                    return;
                }

            } else if (agencySecurityDeposit.deposit_source == 1 && isCheck) {
                //代理商垫资
                def min_setting = dictionary.findTransParam("t0_sec_agency_singleTMoney_min");
                def max_setting = dictionary.findTransParam("t0_sec_agency_singleTMoney_max");
                log.info "代理商垫资--最大值:" + max_setting + ",最小值:" + min_setting;
                Long max = max_setting == null ? 50000 : max_setting.value as Long;
                Long min = min_setting == null ? 9000 : min_setting.value as Long;
                long merchantEachMax = Long.parseLong(tradMerchant.each_max as String);
                log.info("merchantEachMax=${merchantEachMax}")
                if (agencySecurityDeposit.each_max) {
                    log.info("agencySecurityDeposit.each_max=${agencySecurityDeposit.each_max}")
                    merchantEachMax = merchantEachMax > Long.parseLong(agencySecurityDeposit.each_max as String) ? Long.parseLong(agencySecurityDeposit.each_max as String) : merchantEachMax;
                }
                max = merchantEachMax > max * 100 ? max * 100 : merchantEachMax;
                log.info "单笔最大值:" + max;
                if (min > max / 100) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制设置有误,单笔最小金额为${min}元,最大为${max / 100}元,详情咨询代理商!");
                    return;
                }
                if (amount < min * 100 || amount > max) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制在" + min + "元-" + max / 100 + "元,请查询及时付业务规则!");
                    return;
                }

                def agencyMax = dictionary.findTransParam("t0_agency_oneDayTMoney_max");

                long agencyEachMax = agencySecurityDeposit.each_day_max ? Long.parseLong(agencySecurityDeposit.each_day_max as String) : Long.parseLong(agencyMax.value as String) * 100;
                def sigCardAmount = transDao.findSumAmountDailyByAgencyAndCard(agencyId, cardHolder.pan);

                if (agencyEachMax < amount + sigCardAmount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '超过最大限额度:' + agencyEachMax / 100 + '元');
                    return;
                }

            } else if (isCheck) {
                //其他代理商共享
                def min_setting = dictionary.findTransParam("t0_sec_others_singleTMoney_min");
                def max_setting = dictionary.findTransParam("t0_sec_others_singleTMoney_max");
                log.info "其他代理商垫资--最大值:" + max_setting + ",最小值:" + min_setting;
                Long max = max_setting == null ? 50000 : max_setting.value as Long;
                Long min = min_setting == null ? 9000 : min_setting.value as Long;
                long merchantEachMax = Long.parseLong(tradMerchant.each_max as String);
                if (agencySecurityDeposit.each_max) {
                    merchantEachMax = merchantEachMax > Long.parseLong(agencySecurityDeposit.each_max as String) ? Long.parseLong(agencySecurityDeposit.each_max as String) : merchantEachMax;
                }
                max = merchantEachMax > max * 100 ? max * 100 : merchantEachMax;
                log.info "单笔最大值:" + max;
                if (min > max / 100) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制设置有误,单笔最小金额为${min}元,最大为${max / 100}元,详情咨询代理商!");
                    return;
                }
                if (amount < min * 100 || amount > max) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', "交易金额限制在" + min + "元-" + max / 100 + "元,请查询及时付业务规则!");
                    return;
                }

                def agencyMax = dictionary.findTransParam("t0_others_oneDayTMoney_max");

                long agencyEachMax = agencySecurityDeposit.each_day_max ? Long.parseLong(agencySecurityDeposit.each_day_max as String) : Long.parseLong(agencyMax.value as String) * 100;
                def sigCardAmount = transDao.findSumAmountDailyByAgencyAndCard(agencyId, cardHolder.pan);

                if (agencyEachMax < amount + sigCardAmount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '超过最大限额度:' + agencyEachMax / 100 + '元');
                    return;
                }
            }
            def terminal = terminalDao.findTerminalByMerchantId(merchantId);

            def merchantAmount = transDao.findSumAmountDailyByMerchant(tradMerchant.merchant_id_dzero_second);
            def merchantSettle = agencyDao.findMerchantTradeSettleInfo(tradMerchant.merchant_id_dzero_second);
            long merchantMaxDayAmount = Long.parseLong(merchantSettle.max_settle_amount as String) * 100;
            log.info "当前金额：" + (merchantAmount + amount) + ",商户限制额:" + merchantMaxDayAmount

            if (merchantAmount + amount > merchantMaxDayAmount) {
                result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '商户金额超限:' + merchantMaxDayAmount / 100 + "元");
                return;
            }

            long agencyAmount = 0;
            log.info "初始化agencyAmount:" + agencyAmount;
            if ((agencySecurityDeposit.deposit_source == 1 && !agencySecurityDeposit.share_amount) || agencySecurityDeposit.deposit_source == 0) {
                agencyAmount = transDao.findSumAmountDailyByAgency(agencyId);
                if (agencyAmount + amount > agencySecurityDeposit.amount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '代理商金额超限:' + agencySecurityDeposit.amount / 100 + "元");
                    return;
                }
            } else if (agencySecurityDeposit.deposit_source == 1) {
                def agencys = agencyDao.listAgencySecurityDeposit(agencyId);
                long share_amount = Long.parseLong(agencySecurityDeposit.share_amount as String);
                long agency_amount = Long.parseLong(agencySecurityDeposit.amount as String);
                long allAmount = share_amount + agency_amount;
                if (!agencys) {
                    agencyAmount = transDao.findSumAmountDailyByAgency(agencyId);
                } else {
                    StringBuilder agencyBuff = new StringBuilder();
                    agencyBuff.append((agencyId as String) + ",")
                    agencys.each {
                        agencyBuff.append((it.agency_id as String) + ",")
                    }
                    agencyBuff.deleteCharAt(agencyBuff.length() - 1);
                    log.info "代理商ID：" + agencyBuff.toString()
                    agencyAmount = transDao.findSumAmountDailyByAgencys(agencyBuff.toString());
                }
                log.info "代理商交易金额：" + agencyAmount + ",最终限制额:" + allAmount
                if (agencyAmount + amount > allAmount) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '代理商金额超限[' + allAmount / 100 + '元]');
                    return;
                }
            } else if (agencySecurityDeposit.deposit_source == 2) {
                long agencyMainAmount = transDao.findSumAmountDailyByAgency(agencySecurityDeposit.share_agency_id) as long;
                def agencys = agencyDao.listAgencySecurityDeposit(agencySecurityDeposit.share_agency_id);
                if (!agencys) {
                    result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '不合法代理商');
                    return;
                } else {
                    StringBuilder agencyBuff = new StringBuilder();
                    agencys.each {
                        agencyBuff.append((it.agency_id as String) + ",")
                    }
                    agencyBuff.deleteCharAt(agencyBuff.length() - 1);
                    log.info "代理商ID：" + agencyBuff.toString()
                    def subAgencyAmount = transDao.findSumAmountDailyByAgencys(agencyBuff.toString()) as long;
                    def mainAgencyDeposit = agencyDao.agencySecurityDeposit(agencySecurityDeposit.share_agency_id);
                    long share_amount = Long.parseLong(mainAgencyDeposit.share_amount as String);
                    long agency_amount = Long.parseLong(mainAgencyDeposit.amount as String);
                    log.info "共享：" + share_amount + ",当前的交易金额:" + subAgencyAmount + ",主代理商交易金额:" + agencyMainAmount
                    if (agencyMainAmount <= agency_amount) {
                        if (subAgencyAmount + amount > share_amount) {
                            result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '代理商金额超限[' + share_amount / 100 + '元]');
                            return;
                        }
                    } else {
                        long more = agencyMainAmount - agency_amount;
                        if (subAgencyAmount + amount > share_amount - more) {
                            result = Commons.fail(null, 'ILLEGAL_ARGUMENT', '代理商金额超限[' + share_amount / 100 + '元]');
                            return;
                        }
                    }
                }
            }
            //上行交易流水预存
            def trans = transDao.addWsTrans(amount, batch, trace, cardHolder.pan, param.appVersion, param.currency, param.position, param.signatureUri, terminal, param.ksnNo, cardbin)

            try {
                //构建报文并且接受响应报文
                ISOMsg resultResponse = TransUtil.sendAndRecive(TransUtil.packageSaleMessage(batch, trace, cardHolder, amount, param.pin, terminal.merchant_no as String, terminal.terminal_no as String, param.cardSerialNum as String, param.icData as String))
                trans.comp_status = 1
                //TODO 后处理

                if (!resultResponse) {
                    result = Commons.fail(null, 'TRANS_SERVER_TIME_OUT', '交易超时');
                    return;
                }

                def code = resultResponse.getString(39)
                trans.resp_code = code
                trans.reference_no = resultResponse.getString(37)
                trans.auth_no = resultResponse.getString(38)
                def f55 = null;
                //响应55域数据
                if (resultResponse.hasField(55)) {
                    f55 = ISOUtil.hexString(resultResponse.getBytes(55));
                }

                def now = new Date();

                if (code == '00') {
                    trans.trans_status = 1;
                    def res = [
                            reqNo       : param.reqNo ?: null,
                            merchantName: merchant.merchant_name,
                            merchantNo  : terminal.merchant_no,
                            terminalNo  : terminal.terminal_no,
                            operatorNo  : '01',
                            resultCode  : '00',
                            cardNoWipe  : trans.card_no_wipe,
                            amount      : amount,
                            currency    : param.currency ?: 'CNY',
                            issuer      : cardbin ? cardbin.issuer_name : null,
                            voucherNo   : trace,
                            batchNo     : batch,
                            transTime   : now.format(Constants.DATE_FORMAT_YEAR) + resultResponse.getString(13) + resultResponse.getString(12),
                            refNo       : resultResponse.getString(37),
                            authNo      : resultResponse.hasField(38) ? resultResponse.getString(38) : ' ',
                            script      : f55 ?: ''
                    ];
                    result = Commons.success(res);
                    return;
                } else {
                    String errorMessage = Constants.trans_error_code.get(code) ?: code + ":未知错误";
                    result = Commons.fail([reqNo: param.reqNo ?: null, script: f55 ?: '', resultCode: code], code, errorMessage)
                    return;
                }
            } finally {
                trans.comp_status = 2
                transDao.update(trans)
                terminal.trace_no = param.reqNo as int
                terminalDao.update(terminal)
            }
        }
        return result;
    }

    private def checkDayZeroTransTime() {
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_SEMICOLON);
        String today = new Date().format(Constants.DATE_FORMAT_YMD);
        DictionaryDao dictionary = new DictionaryDao();
        String startTime = dictionary.findTransParam("t0_transDate_start")?.value;
        String endTime = dictionary.findTransParam("t0_transDate_end")?.value;
        try {
            long currentTime = System.currentTimeMillis();
            long start = format.parse(today + startTime ?: Constants.DEFAULT_START_TIME).getTime();
            long end = format.parse(today + endTime ?: Constants.DEFAULT_END_TIME).getTime();
            if (currentTime < start || currentTime > end) {
                return false;
            }
        } catch (ParseException e) {
            log.error("ParseException:" + e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    Object query(HttpServletRequest request) {
        MerchantDao merchantDao = new MerchantDao();
        TerminalDao terminalDao = new TerminalDao();
        DeviceDao deviceDao = new DeviceDao();
        DictionaryDao dictionaryDao = new DictionaryDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        TransDao transDao = new TransDao();
        //处理请求
        def param = Commons.parseRequest(request)
        def user = Commons.initUserRequestParams(request)
        //校验商户终端等信息
        log.info "user.user_id = " + user.user_id
        //T1商户信息
        def merchant = merchantDao.findMerchantByMobileNo(user.user_id)
        if (!merchant) {
            return ResponseUtil.failResponse("MERCHANT_NOT_EXIST", "商户不存在");
        }
        //T1商户详细信息
        def personal = merchantDao.findCmPersonalById(merchant.id);
        if (!personal) {
            return ResponseUtil.failResponse("PERSONAL_NOT_EXIST", "商户资料不存在");
        }
        //T1账户信息
        def bankAccount = bankAccountDao.findBankAccountByMerchantId(merchant.id as String);
        if (!bankAccount) {
            return ResponseUtil.failResponse("ACCOUNT_NOT_EXIST", "账户不存在");
        }
        //校验4审
        if (!MerchantUtil.isValidate(merchant, personal, bankAccount)) {
            return ResponseUtil.failResponse("NOT_VALIDATE", "商户认证未通过");
        }

        //T1设备
        def terminal = terminalDao.findTerminalByMerchantId(merchant.id);
        //校验设备KSN
        String ksnNo = param.ksnNo
        def device = deviceDao.findDeviceByKsn(ksnNo);
        def termModel = terminalDao.findTerminalModelByID(terminal.terminal_model_id)
        if (!device || !terminal || !termModel) {
            return ResponseUtil.failResponse("DEVICE_NOT_EXIST", "设备不存在");
        }
        if (device.is_used != 1 && device.is_activated != 1) {
            return ResponseUtil.failResponse("DEVICE_NOT_ACTIVATED", "设备未激活");
        }
        if (terminal.id != device.terminal_id) {
            return ResponseUtil.failResponse("DEVICE_NOT_MATCH", "设备与用户不匹配");
        }
        //mac处理
//        DeviceCheckHelper deviceHelper = new DeviceCheckHelper(termModel.product_model as String, ksnNo, param)
//        if (!deviceHelper.switchCheck()) {
//            return Commons.fail(null, 'PERMISSION_DENIED', '抱歉, 您不能进行此操作')
//        }
        //TRACK处理
        TrackDecoderHelper trackHelper = new TrackDecoderHelper(termModel.product_model as String, ksnNo, param)
        def cardHolder = trackHelper.decode();
        if (!cardHolder) {
            return Commons.fail(null, 'DEVICE_DATA_FIND', '抱歉, 请求数据不能识别')
        }
        def cardbin = dictionaryDao.findCardbin(cardHolder.pan)
        if (!cardbin) {
            return Commons.fail(null, 'CARDBIN_NOT_FOUND', '卡交易被拒绝, 请更换其他银行卡重试')
        }
        if ("debit" != cardbin.card_type) {
            return Commons.fail(null, 'NOT_SUPPORT_CREDIT_CARD', '该功能不支持信用卡')
        }
        param << [cardNo: cardHolder.pan]
        //pin处理
        String pin
        if (param.encPinblock) {
            PinblockHelper pinHelper = new PinblockHelper(termModel.product_model as String, ksnNo, param)
            pin = pinHelper.checkPin()
            if (!pin) {
                return Commons.fail(null, 'PERMISSION_DENIED', '抱歉, 您不能进行此操作')
            }
        }

        def queryCount = transDao.findQueryCountByTerminalNo(terminal.terminal_no)
        if (queryCount && queryCount.count >= Constants.QUERY_COUNT_LIMIT) {
            return Commons.fail(null, 'QUERY_OVER_LIMIT', '查余超出次数限制')
        }
        //构建交易报文
        //批次号
        String batch = "${terminal.batch_no}".padLeft(6, '0')
        //交易流水
        String trace = "${param.reqNo}".padLeft(6, '0')
        try {
            //构建报文并且接受响应报文
            ISOMsg resultResponse = TransUtil.sendAndRecive(TransUtil.packageQueryAmountMessage(batch, trace, cardHolder, terminal.merchant_no as String, terminal.terminal_no as String, pin, param.cardSerialNum as String, param.icData as String))
            //后处理
            if (!resultResponse) {
                return Commons.fail(null, 'TRANS_SERVER_TIME_OUT', '交易超时');
            }
            //响应55域数据
            def f55 = null;
            if (resultResponse.hasField(55)) {
                f55 = ISOUtil.hexString(resultResponse.getBytes(55));
            }
            //#39域返回码
            def code = resultResponse.getString(39)
            if (code == '00') {
                return Commons.success([
                        balance   : resultResponse.getString(54)[-12..-1],
                        currency  : 'CNY',
                        resultCode: '00',
                        issuer    : cardbin ? cardbin.issuer_name : null,
                        cardNoWipe: cardHolder.pan[0..5] + '*****' + cardHolder.pan[-4..-1],
                        //添加返回脚本
                        script    : f55 ?: ''
                ])
            } else {
                String errorMessage = Constants.trans_error_code.get(code) ?: code + ":未知错误";
                return Commons.fail([script: f55 ?: '', resultCode: code], code, errorMessage);
            }
        } finally {
            terminal.trace_no = param.reqNo as int
            terminalDao.update(terminal)
        }

    }

    @Override
    Object queryCurrent(HttpServletRequest request) {
        return null
    }

    @Override
    Object transStatus(HttpServletRequest request) {
        MerchantDao merchantDao = new MerchantDao();
        TerminalDao terminalDao = new TerminalDao();
        DictionaryDao dictionaryDao = new DictionaryDao();
        TransDao transDao = new TransDao();
        //处理请求
        def param = Commons.parseRequest(request)
        def user = Commons.initUserRequestParams(request)
        //原交易请求号
        def origReqNo = param.origReqNo
        //原始交易类型
        def origTransType = param.origTransType
        //原始交易金额
        def amount = param.amount
        //是否是D0业务
        def tradeFlag = param.tradeFlag
        if (origTransType != 'sale') {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '不支持的交易');
        }
        //获取T1商户信息
        def merchant = merchantDao.findMerchantByMobileNo(user.user_id)
        if (!merchant) {
            return Commons.success(null)
        }
        log.info 'merchantT1=' + merchant

        //获取T1终端号
        def terminal = terminalDao.findTerminalByMerchantId(merchant.id)
        //批次号
        def batch = "${terminal.batch_no}".padLeft(6, '0')
        //流水号
        def trace = "${origReqNo}".padLeft(6, '0')
        //是否开启D0业务
        if (tradeFlag) {
            def tradeMerchant = merchantDao.findMerchantTradeZero(merchant.id);
            if (!tradeMerchant) {
                return Commons.fail(null, 'ILLEGAL_ARGUMENT', '不支持及时付');
            }
            def merchantDayZero = merchantDao.findMerchantTradeZero(merchant.id);
            merchant = merchantDao.findMerchantById(merchantDayZero.merchant_id_tzero);
            terminal = terminalDao.findTerminalByMerchantId(merchantDayZero.merchant_id_tzero);
        }
        //查询本地交易是否存在
        def oriWSTrans = transDao.findTrans(batch, trace, terminal.terminal_no as String, amount as Long)
        if (!oriWSTrans) {
            return Commons.fail(null, '25');
        }
        //添加交易交易类型的条件
        def ori = transDao.findTrans(oriWSTrans.reference_no as String, batch, trace, terminal.terminal_no as String, amount as Long)

        if (!ori || ori.comp_status == 1) {
            if (!ori && (new Date().getTime() - oriWSTrans.time_create?.timestampValue().getTime()) > 90000) {
                return Commons.fail(null, '96', '交易失败!')
            }
            return Commons.fail(null, 'PROCESSING')
        }

        log.info "oriWSTrans: ${oriWSTrans}", "ori trans: ${ori}"

        if (ori.trans_status == 1) {
            oriWSTrans.trans_status = 1
            oriWSTrans.reference_no = ori.reference_no
            oriWSTrans.auth_no = ori.acq_auth_no
            def cardbin = dictionaryDao.findCardbin(oriWSTrans.card_no)
            merchantDao.update(oriWSTrans)
            return Commons.success([
                    reqNo       : oriWSTrans.trace_no,
                    merchantName: merchant.merchant_name,
                    merchantNo  : terminal.merchant_no,
                    terminalNo  : terminal.terminal_no,
                    operatorNo  : '01',
                    cardNoWipe  : oriWSTrans.card_no_wipe,
                    amount      : "${oriWSTrans.amount}".padLeft(12, '0'),
                    currency    : oriWSTrans.currency ?: 'CNY',
                    issuer      : cardbin ? cardbin.issuer_name : null,
                    voucherNo   : "${oriWSTrans.trace_no}".padLeft(6, '0'),
                    batchNo     : "${oriWSTrans.batch_no}".padLeft(6, '0'),
                    transTime   : oriWSTrans.time_create?.timestampValue()?.format(Constants.DATE_FORMAT),
                    refNo       : ori.reference_no,
                    authNo      : ori.acq_auth_no ?: ' ',
            ])

        } else {
            return Commons.fail(null, ori.resp_code ?: '96')
        }
    }

    @Override
    Object queryTrans(HttpServletRequest request) {
        MerchantDao merchantDao = new MerchantDao();
        TerminalDao terminalDao = new TerminalDao();
        DictionaryDao dictionaryDao = new DictionaryDao();
        //处理请求
        def param = Commons.parseRequest(request)
        def user = Commons.initUserRequestParams(request)
        //获取查询日期
        def date = param.date
        //获取T1商户信息
        def merchantT1 = merchantDao.findMerchantByMobileNo(user.user_id);
        if (!merchantT1) {
            return Commons.success(null)
        }
        //获取T1终端号
        def termT1 = terminalDao.findTerminalByMerchantId(merchantT1.id)
        //查询T1商户是否有D0业务
        def tradeMerchantD0 = merchantDao.findMerchantTradeZero(merchantT1.id)
        log.info 'tradeMerchantD0 :' + tradeMerchantD0
        //构建SQL
        StringBuffer SQL = new StringBuffer("select * from ws_trans where comp_status=2 and trans_status=1");
        SQL.append(" and (terminal_no=${termT1?.terminal_no}")
        def termD0;
        def termD0Sec;
        //判断D0业务
        if (tradeMerchantD0) {
            //获取D0终端号
            termD0 = terminalDao.findTerminalByMerchantId(tradeMerchantD0.merchant_id_tzero)
            if (termD0) {
                SQL.append(" or terminal_no=${termD0?.terminal_no}")
            }
            //获取秒到账终端号
            termD0Sec = terminalDao.findTerminalByMerchantId(tradeMerchantD0.merchant_id_dzero_second)
            if (termD0Sec) {
                SQL.append(" or terminal_no=${termD0Sec?.terminal_no}")
            }
        }
        SQL.append(" )")

        if (!date) {
            //日期传递参数不存在，则查询当天数据
            date = new Date().format(Constants.DATE_FORMAT_YMD)
            log.info 'date ; ' + date
        }
        SQL.append(" and time_create >= to_timestamp( '${date + '000000'}' ,'${Constants.DATE_FORMAT_24}') and time_create <= to_timestamp( '${date + '235959'}' ,'${Constants.DATE_FORMAT_24}') ")
        //排序
        SQL.append(" order by id desc")
        log.info 'SQL=' + SQL.toString()
        //构建查询数据行
        def rows = merchantDao.db.rows(SQL.toString());
        //构建返回参数列表
        def transactions = []
        //遍历交易记录
        rows.each {
            //是否是D0交易
            boolean isTrade = false
            if (it.merchant_no == termD0?.merchant_no) {
                isTrade = true
            }
            //是否是秒到账交易
            boolean isTradeSec = false
            if (it.merchant_no == termD0Sec?.merchant_no) {
                isTrade = true
                isTradeSec = true
            }
            //查询cardbin获取银行
            def cardbin = dictionaryDao.findCardbin(it.card_no);
            transactions << [
                    respNo         : it.id,//交易ID
                    transType      : it.trans_type,//交易类型
                    voucherNo      : it.trace_no,//交易流水号
                    respCode       : it.resp_code,//返回码39
                    amount         : it.amount,//金额
                    merchantNo     : it.merchant_no,//商户号
                    terminalNo     : it.terminal_no,//终端号
                    currency       : it.currency,//货币类型
                    merchantName   : merchantT1.merchant_name,
                    immediatePay   : isTrade,//是否是DO交易
                    immediateSecPay: isTradeSec,//是否是秒到账交易
                    transTime      : (new Date(it.time_create.timestampValue().time)).format(Constants.DATE_FORMAT),//交易时间
                    refNo          : !it.reference_no ? "" : it.reference_no,//检索参考号 37域
                    authNo         : !it.auth_no ? "" : it.auth_no,//#38
                    cardTail       : it.card_no[-4..-1],//卡号后四位
                    cardNoWipe     : it.card_no_wipe,//带星号卡号
                    operatorNo     : '01',//操作员
                    issuer         : cardbin ? cardbin.issuer_name : null,//银行名
                    batchNo        : it.batch_no,//批次
            ]
        }
        return Commons.success([
                respNo      : param.respNo ?: null,
                merchantName: merchantT1.merchant_name,
                merchantNo  : merchantT1.merchant_no,
                terminalNo  : termT1.terminal_no,
                transactions: transactions,
        ])

    }

    /**
     * IC回调
     *
     *
     * @param request
     * @return
     * @author zhangshb
     * @since 2015-12-10
     */
    @Override
    public Object transNotify(HttpServletRequest request) {
        //获取请求参数
        def params = Commons.parseRequest(request);
        def attrNames = Commons.initUserRequestParams(request);
        def userInfo = Commons.findUserInfoByMobileNo(attrNames?.user_id);
        def reqNo = params.reqNo;
        def origTransTime = params.origTransTime;
        def origTransType = params.origTransType;
        def origReqNo = params.origReqNo;
        String icData = params.icData;
        String cardNo = params.cardNo;
        String cardSerialNum = params.cardSerialNum;
        def tradeType = params.tradeType;

        try {
            //校验四审是否通过审核
            MerchantDao merchantDao = new MerchantDao();
            BankAccountDao bankAccountDao = new BankAccountDao();
            TransDao transDao = new TransDao();
            def merchant = merchantDao.findCmMerchantById(userInfo?.merchantId);
            def personal = merchantDao.findCmPersonalById(userInfo?.merchantId);
            def bankAccount = bankAccountDao.findBankAccountByMerchantId(userInfo?.merchantId);
            if (!(personal.is_certification == 1 && personal.is_signature == 1 && merchant.review_status == 'accept' && bankAccount.is_verified == 1)) {
                return Commons.fail(null, 'MERCHANT_CONFIRM_NOTPASS', Constants.error_code_mapping.MERCHANT_CONFIRM_NOTPASS);
            }

            //校验是否为重复交易
            TerminalDao terminalDao = new TerminalDao();
            def terminal = terminalDao.findTerminalByMerchantId(userInfo?.merchantId);
            if(tradeType){
                tradeType = tradeType as int;
                if (16 == tradeType || 17 == tradeType) {
                    def merchantTradeZero = merchantDao.findMerchantTradeZero(userInfo?.merchantId);
                    def merchantId = merchantTradeZero?.merchant_id_tzero;
                    if (17 == tradeType) {
                        merchantId = merchantTradeZero?.merchant_id_dzero_second;
                    }
                    terminal = terminalDao.findTerminalByMerchantId(merchantId);
                }
            }

            String batch = "${terminal.batch_no}".padLeft(6, '0');
            String trace = "${reqNo}".padLeft(6, '0');
            def transCount = transDao.findTransCount(batch, trace, terminal.terminal_no, terminal.merchant_no);
            if (transCount > 0) {
                return Commons.fail(null, '94', '重复交易');
            }

            //效验交易是否有效
            def tranDate = origTransTime[0..7];
            def origReqNoInfo = "${origReqNo}".padLeft(6, '0');
            log.info("batch=${batch},origReqNoInfo=${origReqNoInfo},origTransType=${origTransType},tranDate=${tranDate},terminal.terminal_no=${terminal.terminal_no}");
            def tranInfo = transDao.findTranByTranInfo(batch, origReqNoInfo, origTransType, tranDate, terminal.terminal_no);
            if (!tranInfo) {
                return Commons.fail(null, '12', '无效交易');
            }

            //校验是否已发送
            TLVList tlv = new TLVList();
            tlv.unpack(ISOUtil.hex2byte(icData));
            String send = tlv.getString(57137);
            if (!send) {
                return Commons.success([reqNo: reqNo ?: null]);
            }

            //发送
            def msg = TransUtil.packageSaleNotifyMessage(trace, terminal, cardNo, cardSerialNum, icData);
            if (!msg) {
                return Commons.fail(null, 'TRANSNOTIFY_FAIL', Constants.error_code_mapping.TRANSNOTIFY_FAIL);
            }

            ISOMsg r = TransUtil.sendAndRecive(msg, null, userInfo?.loginName);
            if (!r) {
                return
            }

            def result = null
            def code = r.getString(39)
            if (code == '00') {
                result = Commons.success([
                        reqNo: reqNo ?: null
                ])
            } else {
                result = Commons.fail([reqNo: reqNo ?: null], code)
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Commons.fail(null, 'TRANSNOTIFY_FAIL', Constants.error_code_mapping.TRANSNOTIFY_FAIL);
    }
}
