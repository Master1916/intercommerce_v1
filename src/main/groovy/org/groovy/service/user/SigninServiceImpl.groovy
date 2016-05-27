package org.groovy.service.user

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.dictionary.DictionaryDao
import org.groovy.dao.merchant.AgencyDao
import org.groovy.dao.merchant.BankAccountDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.TerminalDao
import org.groovy.dao.trans.FeeRateDao
import org.groovy.device.DeviceKeyHelper
import org.groovy.util.ConvertUtil
import org.groovy.util.DateUtil
import org.groovy.util.ICPublicKeyUtil
import org.groovy.util.LocationUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.user.SigninService
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

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
public class SigninServiceImpl implements SigninService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, SigninServiceImpl.getSimpleName())

    public Map realStatusMap = [
            'init'  : '0',
            'submit': '3',
            'reject': '2',
            'accept': '1'
    ]

    /**
     * 签到接口
     *
     * @param request
     * @return
     */
    @Override
    Object signIn(HttpServletRequest request) {
        //返回参数
        return getAcquiringInfo(request)
    }

    /**
     * 获取商户收单信息
     *
     * @param request
     * @return
     */
    public getAcquiringInfo(HttpServletRequest request) {
        //引用商户相关的数据库操作
        MerchantDao merchantDao = new MerchantDao();
        TerminalDao terminalDao = new TerminalDao();
        DictionaryDao dictionaryDao = new DictionaryDao();
        BankAccountDao bankAccountDao = new BankAccountDao();

        //处理请求参数
        def param = Commons.parseRequest(request)
        def user = Commons.initUserRequestParams(request)
        //构建收单逻辑返回参数
        def result
        //构建T1业务数据
        def businessT1
        def merchant, d0Merchant, secondMerchantD0
        try {
            //获取T1商户信息
            merchant = merchantDao.findMerchantByMobileNo(user.user_id)
            if (!merchant) {
                //如果不是收单商户，则只用返回false
                businessT1 = [isMerchantT1: false]
                result = [businessT1: businessT1]
                return result;
            } else {
                businessT1 = [isMerchantT1: true]
            }
            //查询T1商户对应绑定的终端
            def terminal = terminalDao.findTerminalByMerchantId(merchant.id)
            //获取设备类型
            def termModel = terminalDao.findTerminalModelByID(terminal?.terminal_model_id)
            log.info 'terminal:' + terminal
            log.info 'termModel:' + termModel
            if (!terminal || !termModel) {
                return Commons.fail(null, 'ILLEGAL_ARGUMENT', '终端错误')
            }
            //T1商户对应终端KSN
            def ksn = terminalDao.findKSNByMerchantId(merchant.id)
            //T1商户的结算卡
            def bankAccount = bankAccountDao.findBankAccountByMerchantId(merchant.id)
            if (!ksn) {
                return Commons.fail(null, 'ILLEGAL_ARGUMENT', '未找到KSN')
            }
            //判断校验信息
            if (ksn.terminal_id != terminal.id) {
                return Commons.fail(null, 'ILLEGAL_ARGUMENT', '终端KSN不匹配')
            }
            if (!bankAccount) {
                return Commons.fail(null, 'ILLEGAL_ARGUMENT', '未找到注册卡号')
            }
            //设备激活码
            def license = terminalDao.findLicenseByKSNId(ksn.id)
            if (!license) {
                return Commons.fail(null, 'ILLEGAL_ARGUMENT', '未找到激活码')
            }
            //T1商户对应的代理商的产品信息
            def productDB = dictionaryDao.findPoductByAgencyId(license.agency_id)
            if (!productDB) {
                return Commons.fail(null, 'SWIPE_OR_POS_NOT_MATCH')
            }
            //获取T1商户的四审状态
            // 0 - 未提交
            // 1 - 通过
            // 2 - 未通过
            // 3 - 已提交，未审核
            def personal = merchantDao.findCmPersonalById(merchant.id)
            def realStatus = personal?.is_certification as int
            def merchantStatus = realStatusMap.get(merchant.review_status)
            def accountStatus = bankAccount.is_verified as int
            def signatureStatus = personal?.is_signature as int
            def status = realStatus + merchantStatus + accountStatus + signatureStatus
            log.debug('status=' + status)
            //添加参数
            def info = [
                    status         : status,
                    name           : "null".equalsIgnoreCase(merchant.name) ? "" : merchant.name,
                    cardTail       : bankAccount.account_no[-4..-1],
                    bluetoothName  : ksn.bluetooth_name ?: "",
                    serialType     : license.fee_type,
                    merchantReason : personal?.merchant_reason ?: "",
                    realReason     : personal?.real_reason ?: "",
                    signatureReason: personal?.signature_reason ?: "",
                    accountReason  : personal?.account_reason ?: "",
                    T1AuthFirstPass: personal?.merchant_auth_pass_flag,
            ]
            //T1商户费率信息
            def merchantFee = merchantDao.findMerchantFeeRate(merchant.id, 0);
            if (merchantFee) {
                if (merchantFee.rate_type == 11) {
                    info << [feeRate: merchantFee.params_a.toString()];
                } else if (merchantFee.rate_type == 13) {
                    info << [feeRate: merchantFee.params_a + "~" + merchantFee.max_fee];
                }
            }
            //添加T1info信息
            businessT1 << [info: info]
            //添加T1业务信息
            result = [businessT1: businessT1]
            //设备类型
            result << [model: termModel.product_model]
            //00或者null不更新IC密钥,其他状态都需要更新密钥
            if (terminal.ic_status != null && terminal.ic_status != '00') {
                //需要更新密钥
                result << [needUpdateIC: true];
                result << [ICkey: ICPublicKeyUtil.getICPublicKey()]
            } else {
                //不需要更新密钥
                result << [needUpdateIC: false];
            }
            //获取并且添加密钥信息
            def deviceInfo = getKeyInfo(termModel, ksn);
            if (!deviceInfo) {
                return Commons.fail(null, 'DEVICE_DATA_ERROR', '终端设备数据不能初始化')
            }
            result << [device: deviceInfo]
            //该商户是否有资格申请D0业务
            def agencySettle = merchantDao.findAgencyTradeInfo(merchant.agency_id);
            if (agencySettle && agencySettle?.settle_type) {
                //支持申请D0业务参数
                result << [agencyTrade: true];
            } else {
                result << [agencyTrade: false];
            }
            log.info "T1 BEFORE:" + terminal
            def isMerchantDayZero = false;
            //查看是否开通D0业务
            def merchantDayZero = merchantDao.findD0MerchantByMerId(merchant.id);
            if (merchantDayZero) {
                log.info "有D0业务"
                //查看D0终端
                def termD0 = terminalDao.findTerminalByMerchantId(merchantDayZero?.merchant_id_tzero)
                if (termD0) {
                    log.info "terminal.trace_no:" + terminal.trace_no + " termD0.trace_no:" + termD0.trace_no + "  :" + ((terminal.trace_no as int) < (termD0.trace_no as int))
                    //DO终端流水号大于T1终端流水号则使用D0的流水号
                    if ((terminal.trace_no as int) < (termD0.trace_no as int)) {
                        terminal = termD0
                    }
                } else {
                    log.info "没有D0终端"
                }
                isMerchantDayZero = true;
            } else {
                log.info "没有D0业务"
            }
            log.info "T1 AFTER:" + terminal
            terminal.trace_no = (terminal.trace_no && terminal.trace_no > 0) ? (terminal.trace_no as int) % 999999L + 1 : 1
            terminalDao.update(terminal)
            log.info "traceNo:" + terminal.trace_no
            //添加流水号
            result << [traceNo: terminal.trace_no]
            //T1商户对应的D0业务商户
            def businessD0 = [isMerchantD0: isMerchantDayZero];
            if (isMerchantDayZero) {
                //获取D0商户相关信息
                businessD0 << getDOBusiness(merchantDayZero);
                d0Merchant = merchantDao.findCmMerchantByMerId(merchantDayZero?.merchant_id_tzero);
                def isSuccess = true;
                if (!merchantDayZero?.merchant_id_dzero_second) {
                    def d0BankAccount = bankAccountDao.findBankAccountByMerchantId(merchantDayZero?.merchant_id_tzero);
                    def d0SecondTerminal = terminalDao.findTerminalByMerId(terminal?.merchant_id);
                    def d0SecondMerchantId = merchantDao.getSeqNextval("seq_cmmerchant");
                    log.info("d0BankAccount?.id=${d0BankAccount?.id}")
                    isSuccess = createD0SecondMerchant(merchantDayZero, d0BankAccount?.id, d0Merchant, d0SecondTerminal, d0SecondMerchantId);//初始化创建秒到商户相关信息
                }
                log.info("D0秒到商户是否创建成功:${isSuccess}")
                if (isSuccess) {
                    FeeRateDao feeRateDao = new FeeRateDao();
                    def feeRate = feeRateDao.findFeeRatesByMerchantId(merchantDayZero?.merchant_id_dzero_second);
                    if (feeRate) {
                        businessD0 << [d0SecondFeeRate: feeRate?.params_a];
                    }
                    def d0EachAmount = getD0EachAmountByAId(merchant.agency_id);
                    if(d0EachAmount){
                        businessD0 << [d0SecondMinSettleAmount: d0EachAmount.d0MinAmount*100];
                    }

                }
            }
            result << [businessD0: businessD0];
//        def appVersion = ValidateUtil.versionParse(param.appVersion);
            result << [functionDisabled: Constants.DISABLED_FUNCTION];
            log.info "result : " + ConvertUtil.mapConvertToJson(result)
            return Commons.success(result, "签到成功")

        } finally {
            log.debug 'position = ' + param.position
            def merchantDayZero = merchantDao.findD0MerchantByMerId(merchant?.id);
            d0Merchant = merchantDao.findCmMerchantByMerId(merchantDayZero?.merchant_id_tzero);
            secondMerchantD0 = merchantDao.findCmMerchantByMerId(merchantDayZero?.merchant_id_dzero_second);
            //调用更新/存储T1用户区域ID接口
            LocationUtil.saveAreaCode(merchant, d0Merchant, secondMerchantD0, param.position)
        }

    }

    /**
     * 下发工作密钥
     *
     * @param ksn
     * @return
     */
    private getKeyInfo(term, ksn) {
        DeviceKeyHelper helper = new DeviceKeyHelper(term.product_model, ksn.ksn_no, null);
        return helper.loadKey();
    }

    /**
     * 获取商户D0业务数据
     *
     * @param merchant
     * @param position
     * @return
     */
    private getDOBusiness(def merchantDayZero) {

        //引用商户相关的数据库操作
        MerchantDao merchantDao = new MerchantDao();
        DictionaryDao dictionaryDao = new DictionaryDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        //构建返回
        def result = [:];
        //申请了D0业务
        log.debug "merchantDayZero " + merchantDayZero
        def info = [merchantTradeStatus: merchantDayZero.is_verify as int];
        //获取DO账户信息
        def accountD0Status = bankAccountDao.findBankAccountByMerchantId(merchantDayZero.merchant_id_tzero)?.is_verified
        if (accountD0Status) {
            accountD0Status = accountD0Status as int
        } else {
            accountD0Status = 0
        }
        info << ['accountD0Status': accountD0Status];
        log.debug " accountD0Status " + accountD0Status + "====merchantDayZero.is_verify  " + merchantDayZero.is_verify
        //校验商户D0业务开通状态
        if (merchantDayZero.is_verify == 2 || accountD0Status == 2) {
            info << [reasonType: merchantDayZero.reason_type];
            info << [failReason: merchantDayZero.fail_reson == null ? "" : ConvertUtil.strConvertToMap(merchantDayZero.fail_reson)];
        } else {
            def merchantFee = merchantDao.findMerchantFeeRate(merchantDayZero.merchant_id_tzero, 0);
            if (merchantFee) {
                if (merchantFee.rate_type == 11) {
                    info << [merchantFeeRate: merchantFee.params_a.toString()];
                } else if (merchantFee.rate_type == 13) {
                    info << [merchantFeeRate: merchantFee.params_a + "~" + merchantFee.max_fee];
                }
            }
            def merchantAddFee = merchantDao.findMerchantFeeRate(merchantDayZero.merchant_id_tzero, 1);
            if (merchantAddFee) {
                if (merchantAddFee.rate_type == 11) {
                    info << [merchantAddFeeRate: merchantAddFee.params_a.toString()];
                } else if (merchantAddFee.rate_type == 13) {
                    info << [merchantAddFeeRate: merchantAddFee.params_a + "~" + merchantAddFee.max_fee];
                }
            }
        }
        //构建D0返回信息
        def startTimeSetting = dictionaryDao.findTransParam('t0_transDate_start')
        def endTimeSetting = dictionaryDao.findTransParam('t0_transDate_end');
        String st = startTimeSetting == null ? "07:00:00" : startTimeSetting.value as String;
        String et = endTimeSetting == null ? "16:50:00" : endTimeSetting.value as String;
        //是否是节假日
        result << [isHoliday: false];
        //设定D0开始时间
        result << [startTime: DateUtil.parse(DateUtil.getYMD() + st, Constants.DATE_FORMAT_SEMICOLON).getTime()];
        //设定D0结束时间
        result << [endTime: DateUtil.parse(DateUtil.getYMD() + et, Constants.DATE_FORMAT_SEMICOLON).getTime()];
        return result << [info: info]
    }

    /**
     * 初始化创建D0秒到商户信息
     *
     *
     * @param merchantDayZero D0商户关联表
     * @param terminal 终端
     * @return
     * @author zhangshb
     * @since 20160308
     */
    static def createD0SecondMerchant(
            def merchantDayZero,
            def d0BankAccountId,
            def d0Merchant, def d0SecondTerminal, def d0SecondMerchantId, def isUpdate = true) {
        MerchantDao merchantDao = new MerchantDao();
        def result = false;
        try {
            merchantDao.withTransaction {
                log.info("d0SecondMerchantId=${d0SecondMerchantId}");
                def d0SecondMerBankAccountId = merchantDao.getSeqNextval("seq_merchantbankaccount");
                log.info("d0SecondMerBankAccountId=${d0SecondMerBankAccountId}");

                //初始化创建D0秒到商户结算卡关联表信息
                merchantDao.addMerBankAccountInfo(d0SecondMerBankAccountId, d0BankAccountId, d0SecondMerchantId);

                //初始化创建D0秒到商户标扣扣率信息
                def d0SecondFeeRateId = merchantDao.getSeqNextval("seq_feeratesetting");
                log.info("d0SecondFeeRateId=${d0SecondFeeRateId}");
                def feeRateDao = new FeeRateDao();
                feeRateDao.db = merchantDao.db;
                def dictionaryDao = new DictionaryDao();
                def transParam = dictionaryDao.findTransParam("t0_sec_feeRate");
                def d0SecondFeeRate = transParam == null ? 0.49 : new BigDecimal(transParam.value).divide(new BigDecimal(100));
                feeRateDao.addFeeRateSettingInfo(d0SecondFeeRateId, 11, 0, 0, d0SecondFeeRate);

                //初始化创建D0秒到商户标扣扣率关联表信息
                def d0SecondMerFeeRateId = merchantDao.getSeqNextval("seq_merchantfeerate");
                log.info("d0SecondMerFeeRateId=${d0SecondMerFeeRateId}")
                feeRateDao.addMerchantFeeRateInfo(d0SecondMerFeeRateId, d0SecondMerchantId, d0SecondFeeRateId);

                //初始化创建D0秒到商户的结算周期
                def d0SecondSettleSettId = merchantDao.getSeqNextval("seq_settlesetting");
                log.info("d0SecondSettleSettId=${d0SecondSettleSettId}")
                def d0SecEachAmount = getD0EachAmountByAId(d0Merchant?.agency_id);
                Long d0SecondMinTransMoney = d0SecEachAmount.d0MinAmount;
//                Long d0SecondMaxTransMoney = d0SecEachAmount.d0MaxAmount;
                feeRateDao.addSettleSettingInfo(d0SecondSettleSettId, 'D', 0, 50000.00, d0SecondMinTransMoney);

                //初始化创建D0秒到商户信息
                def d0SecondMerchantNo = merchantDao.getMerchantNo() as String;
                d0Merchant.id = d0SecondMerchantId;
                d0Merchant.merchant_no = d0SecondMerchantNo;
                d0Merchant.settle_setting_id = d0SecondSettleSettId;
                def mccId = merchantDao.getMccIdByMcc(5998) as int;
                d0Merchant.mcc_id = mccId;
                d0Merchant.mcc_type = "批发类" as String;
                d0Merchant.merchant_settle_product_id = 1 as int;
                merchantDao.db.dataSet("cm_merchant").add(d0Merchant);

                //初始化创建D0秒到终端信息
                TerminalDao terminalDao = new TerminalDao();
                terminalDao.db = merchantDao.db;
                def d0SecondTerminalId = merchantDao.getSeqNextval("seq_merchantterminal");
                log.info("d0SecondTerminalId=${d0SecondTerminalId}")
                def d0SecondTerminalNo = terminalDao.getTerminalNo() as String;
                log.info("d0SecondTerminalNo=${d0SecondTerminalNo}")

                d0SecondTerminal.id = d0SecondTerminalId;
                d0SecondTerminal.terminal_no = d0SecondTerminalNo;
                d0SecondTerminal.merchant_Id = d0SecondMerchantId;
                d0SecondTerminal.merchant_no = d0SecondMerchantNo;
                terminalDao.db.dataSet("merchant_terminal").add(d0SecondTerminal);

                //更新D0商户关联表信息
                if (isUpdate) {
                    merchantDayZero.merchant_id_dzero_second = d0SecondMerchantId;
                    merchantDao.update(merchantDayZero);
                }

            }
            result = true;
        } catch (Exception e) {
            log.error("初始化创建秒到商户信息发生异常：${e.getMessage()}");
        }
        return result;
    }

    /**
     * 通过商户代理商id获取所属的垫资方式额度
     *
     *
     * @return
     * @author zhangshb
     * @since 20160419
     */
    static def getD0EachAmountByAId(def agencyId, def minVal=9000, def maxVal=50000.00, def isD0Sec=true){
        AgencyDao agencyDao = new AgencyDao();
        DictionaryDao dictionaryDao = new DictionaryDao();
        def agencySecurityDeposit = agencyDao.agencySecurityDeposit(agencyId);
        if(!agencySecurityDeposit){
            return null;
        }
        def minParamName, maxParamName;
        if (agencySecurityDeposit.deposit_source == 0) {
            //中汇垫资
            minParamName = "t0_sec_zh_singleTMoney_min"
            maxParamName = "t0_sec_zh_singleTMoney_max"
            if(!isD0Sec){
                minParamName = "t0_zh_singleTMoney_min"
                maxParamName = "t0_zh_singleTMoney_max"
            }
        } else if (agencySecurityDeposit.deposit_source == 1) {
            //代理商垫资
            minParamName = "t0_sec_agency_singleTMoney_min"
            maxParamName = "t0_sec_agency_singleTMoney_max"
            if(!isD0Sec){
                minParamName = "t0_agency_singleTMoney_min"
                maxParamName = "t0_agency_singleTMoney_max"
            }
        } else {
            //其他代理商共享
            minParamName = "t0_sec_others_singleTMoney_min"
            maxParamName = "t0_sec_others_singleTMoney_max"
            if(!isD0Sec){
                minParamName = "t0_others_singleTMoney_min"
                maxParamName = "t0_others_singleTMoney_max"
            }
        }
        def minTransParam = dictionaryDao.findTransParam(minParamName);
        Long d0MinAmount = minTransParam == null ? minVal : minTransParam.value as Long;
        def maxTransParam = dictionaryDao.findTransParam(maxParamName);
        Long d0MaxAmount = maxTransParam == null ? maxVal : maxTransParam.value as Long;
        return [d0MinAmount: d0MinAmount, d0MaxAmount: d0MaxAmount];
    }

}