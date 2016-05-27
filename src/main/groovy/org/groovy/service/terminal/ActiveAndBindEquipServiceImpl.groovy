package org.groovy.service.terminal

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.merchant.BankAccountDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.TerminalDao
import org.groovy.dao.trans.FeeRateDao
import org.groovy.dao.user.UserDao
import org.groovy.util.AlgorithmUtil
import org.groovy.util.CustomerUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.terminal.ActiveAndBindEquipService

import javax.servlet.http.HttpServletRequest
import java.sql.Timestamp

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/20
 * Time : 16:09
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
class ActiveAndBindEquipServiceImpl implements ActiveAndBindEquipService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, ActiveAndBindEquipServiceImpl.getSimpleName())

    /**
     * 激活绑定设备
     *
     *
     * @param ksnNo ksnNo号 [数据格式：'5010100000023402']
     * @param appVersion App版本信息 [数据格式：ios.ZFT.1.1.813]
     * @param activeCode 激活码 [数据格式：11C718FF1FD14531]
     * @param product 产品型号 [数据格式：ZFT]
     * @param mobile 手机号 [数据格式：15801376995]
     * @param password 密码 [数据格式：000000]
     * @return
     * @author zhangshb
     * @since 2015-11-20
     */
    @Override
    public Object activeAndBindEquip(HttpServletRequest request) {

        //激活绑定设备操作
        try {
            //引用数据库操作
            MerchantDao merchantDao = new MerchantDao();
            TerminalDao terminalDao = new TerminalDao();
            terminalDao.db = merchantDao.db;
            FeeRateDao feeRateDao = new FeeRateDao();
            feeRateDao.db = merchantDao.db;
            UserDao userDao = new UserDao();
            userDao.db = merchantDao.db;
            BankAccountDao bankAccountDao = new BankAccountDao();
            bankAccountDao.db = merchantDao.db;

            //获取请求参数
            def attrNames = Commons.initUserRequestParams(request);
            log.info("attrNames=${attrNames}")
            def params = Commons.parseRequest(request);
            def ksnNo = params.ksnNo?.trim();
            def activeCode = params.activeCode?.trim();
            def product = params.product?.trim().toUpperCase();
            def model = params.model?.trim().toLowerCase();
            def macAddress = params.macAddress?.toUpperCase();
            def mobile = attrNames.user_id

            //校验是否需要上传MAC地址
            if(!(model in Constants.NOT_NEED_MAC_ADDRESS)){
                if(!macAddress){
                    return Commons.fail(null, 'REQUEST_NOT_NULL', 'MAC地址不能为空');
                }
            }

            //校验请求参数
            if (!ksnNo || !activeCode || !product || !model || !mobile) {
                return Commons.fail(null, 'REQUEST_NOT_NULL', Constants.error_code_mapping.REQUEST_NOT_NULL);
            }

            def mobilePerson = merchantDao.findPersonByMobileNo(mobile);
            if (mobilePerson) {
                return Commons.fail(null, 'USER_EXIST', '用户已经注册设备');
            }

            //调用客户信息接口查看信息是否存在
            def customerInfo = CustomerUtil.findCustomerInfoByMobile(mobile);
            if (0 == customerInfo.size() || customerInfo.code) {
                return Commons.fail(null, 'ACTIVE_BIND_EQUIP_FAIL', Constants.error_code_mapping.ACTIVE_BIND_EQUIP_FAIL);
            }
            def password = customerInfo.password;

            //查看KsnNo使用情况
            def ksn = terminalDao.findKsnByKsnNo(ksnNo)
            if (!ksn) {
                return Commons.fail(null, 'SWIPER_NOT_EXIST', Constants.error_code_mapping.SWIPER_NOT_EXIST);
            }
            log.info "ksn: (${ksn?.id}, ${ksn?.ksn_no}, is_used:${ksn?.is_used}, is_activated:${ksn?.is_activated})"

            def result = [];
            def license = null;
            //校验设备使用情况
            if (ksn.is_activated == 1 && ksn.is_used == 0) {
                license = terminalDao.findLicenseByKSNId(ksn.id)
                result << [
                        serialType   : license.fee_type,
                        signatureFlag: true
                ]
                return Commons.fail(result, 'KSNNO_ACTIVATED', Constants.error_code_mapping.KSNNO_ACTIVATED);
            }

            if (ksn.is_activated == 0 && ksn.is_used == 1) {
                return Commons.fail(null, 'KSNNO_NOT_AVAILABLE', Constants.error_code_mapping.KSNNO_NOT_AVAILABLE);
            }

            if (ksn.is_activated == 1 && ksn.is_used == 1) {
                return Commons.fail(null, 'ILLEGAL_KSNNO', Constants.error_code_mapping.ILLEGAL_KSNNO);
            }

            //效验激活码信息
            license = terminalDao.findLicenseByCode(activeCode);
            if (!license) {
                return Commons.fail(null, 'KSNNO_OR_LICENSECODE_NOT_EXIST', Constants.error_code_mapping.KSNNO_OR_LICENSECODE_NOT_EXIST);
            }

            if (license.is_used == 1 || license.is_cancell == 1) {
                return Commons.fail(null, 'ACTIVECODE_USED', Constants.error_code_mapping.ACTIVECODE_USED);
            }

            //校验激活码是否有效
            def mccType = license?.mcc_type;
            def mccId = merchantDao.getMccIdByMcc(license?.mcc);
            if(!mccType || !mccId){
                return Commons.fail(null, 'ACTIVATION_CODE_IS_INVALID');
            }

            //效验产品型号是否与激活码所属的产品型号相匹
            def productDB = terminalDao.findPoductByAgencyId(license.agency_id)
            if (!productDB || productDB.product_no.toUpperCase() != product) {
                return Commons.fail(null, 'SWIPE_OR_POS_NOT_MATCH');
            }

            //校验终端与ksn号是否匹配
            def dictModel = terminalDao.findTerminalModelByProductModel(model);
            if (!(ksnNo as String).startsWith(dictModel?.identity_code as String)) {
                return Commons.fail(null, 'DEVICE_NOT_MATCH');
            }

            def cmMerchantSeq = null, merOperatorSeq = null;
            merchantDao.withTransaction {
                //初始化序列信息
                def settleSettingSeq = merchantDao.getSeqNextval("seq_settlesetting");
                log.info("settleSettingSeq=${settleSettingSeq}")
                cmMerchantSeq = merchantDao.getSeqNextval("seq_cmmerchant");
                log.info("cmMerchantSeq=${cmMerchantSeq}")
                def serNumTerminalSeq = merchantDao.getSeqNextval("seq_serialnumberterminal");
                log.info("serNumTerminalSeq=${serNumTerminalSeq}")
                def terminalSeq = merchantDao.getSeqNextval("seq_merchantterminal");
                log.info("terminalSeq=${terminalSeq}")
                merOperatorSeq = merchantDao.getSeqNextval("seq_merchantoperator");
                log.info("merOperatorSeq=${merOperatorSeq}")
                def feeRateSettingSeq = merchantDao.getSeqNextval("seq_feeratesetting");
                log.info("feeRateSettingSeq=${feeRateSettingSeq}")
                def merchantFeeRateSeq = merchantDao.getSeqNextval("seq_merchantfeerate");
                log.info("merchantFeeRateSeq=${merchantFeeRateSeq}")
                def bankAccountSeq = merchantDao.getSeqNextval("seq_bankaccount");
                log.info("bankAccountSeq=${bankAccountSeq}")
                def merBankAccountSeq = merchantDao.getSeqNextval("seq_merchantbankaccount");
                log.info("merBankAccountSeq=${merBankAccountSeq}")

                //初始化创建限额信息
                feeRateDao.addSettleSettingInfo(settleSettingSeq);

                //初始化创建商户个人信息
                def typeCode = getTypeCodeByProductNo(productDB?.product_no);
                merchantDao.addPersonalInfo(cmMerchantSeq, mobile, "legal", null, '', '', productDB?.id, 0, typeCode);

                //初始化创建商户信息
                def merchantNo = merchantDao.getMerchantNo();
                def createTime = new Timestamp(new Date().time);
                merchantDao.addMerchantInfo(cmMerchantSeq, merchantNo, settleSettingSeq, license.agency_id, createTime, null, mccId as int, mccType as String);
//                merchantDao.addMerchantInfo(cmMerchantSeq, merchantNo, settleSettingSeq, license.agency_id, createTime, null);

                //初始化创建激活码终端对应信息
                def terminalNo = terminalDao.getTerminalNo();
                terminalDao.addSerNumberTerminalInfo(serNumTerminalSeq, license?.code, terminalNo, createTime);

                //初始化创建商户终端信息
                def terminalModelId = terminalDao.findTerminalModelByProductModel(model);
                log.info("ksn?.ksn_no=${ksn?.ksn_no},merchantNo=${merchantNo},terminalNo=${terminalNo},terminalModelId?.id=${terminalModelId?.id}")
                terminalDao.addMerchantTerminalInfo(terminalSeq, createTime, ksn?.ksn_no, cmMerchantSeq, merchantNo, terminalNo, terminalModelId?.id, null);

                //初始化创建商户的操作员信息
                String loginName = "${product}.${model}.${mobile}"
                def roleId = userDao.findRoleIdByRoleCode("uqs_merchant");
                merchantDao.addMerOperatorInfo(merOperatorSeq, createTime, loginName, password, AlgorithmUtil.encodeBySha1('000000'), cmMerchantSeq, roleId);

                //变更ksn使用信息
                ksn.terminal_id = terminalSeq;
                if(macAddress) {
                    ksn.mac_address = macAddress
                }
                ksn.is_activated = true;
                ksn.is_used = true;
                merchantDao.update(ksn);

                //变更激活码使用信息
                license.terminal_id = terminalSeq;
                license.is_used = true;
                license.ksn_id = ksn?.id;
                merchantDao.update(license);

                //初始化费率设置信息
                def feeType = license.fee_type
                def feeRateInfo = getFeeRateByFeeType(feeType);
                feeRateDao.addFeeRateSettingInfo(feeRateSettingSeq, feeRateInfo.rate_type, feeRateInfo.max_fee, feeRateInfo.min_x, feeRateInfo.params_a);

                //变更商户所属的业务员、行业大类信息
                merchantDao.updateSalesmanId(cmMerchantSeq);
//                def mccInfo = getMccInfo(feeRateInfo.rate_type, feeType, merchantDao);
//                if (mccInfo.isUpdate) {
//                    def merchant = merchantDao.findCmMerchantById(cmMerchantSeq);
//                    merchant.mcc_type = mccInfo.mccInfo.mcc_type;
//                    merchant.mcc_id = mccInfo.mccInfo.mcc_id;
//                    merchantDao.update(merchant);
//                }

                //初始化创建商户费率信息
                feeRateDao.addMerchantFeeRateInfo(merchantFeeRateSeq, cmMerchantSeq, feeRateSettingSeq);

                //初始化创建银行账户信息
                bankAccountDao.addBankAccountInfo(bankAccountSeq);

                //初始化创建商户银行账户信息
                merchantDao.addMerBankAccountInfo(merBankAccountSeq, bankAccountSeq, cmMerchantSeq);
            }
            return Commons.success([merchantId: cmMerchantSeq, userId: merOperatorSeq], '激活绑定设备成功');
        } catch (Exception e) {
            log.error("${e.getMessage()}====${e.toString()}");
            return Commons.fail(null, 'ACTIVE_BIND_EQUIP_FAIL', Constants.error_code_mapping.ACTIVE_BIND_EQUIP_FAIL);
        }
    }

    @Override
    Object downloadFinished(HttpServletRequest request) {
        TerminalDao terminalDao = new TerminalDao();
        MerchantDao merchantDao = new MerchantDao();
        def user = Commons.initUserRequestParams(request);
        def merchant = merchantDao.findMerchantByMobileNo(user.user_id)
        if (!merchant) {
            return Commons.success(null)
        }
        def ksn = terminalDao.findKSNByMerchantId(merchant.id)
        terminalDao.updateICPublicStatus(ksn.ksn_no);
        return Commons.success(null, "已更新状态!")

    }

    private def getTypeCodeByProductNo(def productNo) {
        def typeCode = '06';
        if (productNo == Constants.SZKM) {
            typeCode = '04'   //神州卡盟
        } else if (productNo == Constants.SHZF) {
            typeCode = '05'   //上海掌富
        }
        typeCode;
    }

    private def getFeeRateByFeeType(def feeType) {
        def feeRateSetting = [min_x: 0];
        switch (feeType){
            case '0.38' :  feeRateSetting << [rate_type:11,max_fee:0.00,params_a:0.3800]
                break
            case '0.45' :  feeRateSetting << [rate_type:11,max_fee:0.00,params_a:0.4500]
                break
            case '0.49' :  feeRateSetting << [rate_type:11,max_fee:0.00,params_a:0.4900]
                break
            case '0.5' :  feeRateSetting << [rate_type:11,max_fee:0.00,params_a:0.5000]
                break
            case '0.78' :  feeRateSetting << [rate_type:11,max_fee:0.00,params_a:0.7800]
                break
            case '1' :  feeRateSetting << [rate_type:11,max_fee:0.00,params_a:1.0000]
                break
            case '1.25' :  feeRateSetting << [rate_type:11,max_fee:0.00,params_a:1.2500]
                break
            case '0.78--26' :  feeRateSetting << [rate_type:13,max_fee:26.00,min_x:3333,params_a:0.7800]
                break
            case '0.78--35' :  feeRateSetting << [rate_type:13,max_fee:35.00,min_x:3333,params_a:0.7800]
                break
            case '0.78--50' :  feeRateSetting << [rate_type:13,max_fee:50.00,min_x:3333,params_a:0.7800]
                break
            case '0.78--100' :  feeRateSetting << [rate_type:13,max_fee:100.00,min_x:3333,params_a:0.7800]
                break
            default:  feeRateSetting << [rate_type:11,max_fee:0.00,params_a:0.7800]
        }
        feeRateSetting;
    }

    /*
    private def getMccInfo(def rateType, def feeType, merchantDao) {
        boolean isUpdate = false;
        def mccInfo = [:];
        if (rateType == 11) {
            if (feeType == '0.38' || feeType == '0.49' || feeType == '0.5') {
                mccInfo << [mcc_type: '民生类'];
                mccInfo << [mcc_id: merchantDao.getMccIdByMcc(4511)];
            } else if (feeType == '0.78') {
                mccInfo << [mcc_type: '一般类'];
                mccInfo << [mcc_id: merchantDao.getMccIdByMcc(5331)];
            } else if (feeType == '1.25') {
                mccInfo << [mcc_type: '餐娱类'];
                mccInfo << [mcc_id: merchantDao.getMccIdByMcc(7911)];
            }
            isUpdate = true;
        } else if (rateType == 13) {
            mccInfo << [mcc_type: '批发类'];
            mccInfo << [mcc_id: merchantDao.getMccIdByMcc(5998)];
            isUpdate = true;
        }
        return [isUpdate: isUpdate, mccInfo: mccInfo];
    }
    */
}
