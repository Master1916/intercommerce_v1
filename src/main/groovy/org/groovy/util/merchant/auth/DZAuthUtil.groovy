package org.groovy.util.merchant.auth

import org.apache.commons.lang.StringUtils
import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.dictionary.DictionaryDao
import org.groovy.dao.merchant.BankAccountDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.TerminalDao
import org.groovy.dao.trans.FeeRateDao
import org.groovy.service.user.SigninServiceImpl
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar


/**
 * D0二审辅助类
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.util.merchant.auth
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/12/4
 * Time : 18:21
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
public class DZAuthUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, DZAuthUtil.getSimpleName());

    /**
     * 获取当前用户的D0手持身份证认证状态
     *
     *
     * @param merchantId 商户id
     * @return
     * @author zhangshb
     * @since 2015-12-4
     */
    static def getHandIdCardAuthStatus(def merchantId) {
        def authStatusResult = [authStatus: null, authCode: 'AUTH_NOT_EXIST']
        MerchantDao merchantDao = new MerchantDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        def merchant = merchantDao.findCmMerchantById(merchantId);
        if (!merchant) {
            authStatusResult << [authCode: 'MERCHANT_NOT_FOUND']; return authStatusResult;
        }

        //校验是否支持及时付业务
        def agency = merchantDao.findAgencyTradeInfo(merchant?.agency_id);
        if (!agency || !agency.settle_type) {
            authStatusResult << [authCode: 'NOT_SUPPORT_BUSINESS']; return authStatusResult;
        }

        //效验商户认证信息
        def personal = merchantDao.findCmPersonalById(merchantId)
        def bankAccount = bankAccountDao.findBankAccountByMerchantId(merchantId)
        if (!(personal.is_certification == 1 && personal.is_signature == 1 && merchant.review_status == 'accept' && bankAccount.is_verified == 1)) {
            authStatusResult << [authCode: 'MERCHANT_CONFIRM_NOTPASS']; return authStatusResult;
        }

        def merchantDayZero = merchantDao.findD0MerchantByMerId(merchant?.id);
        if (!merchantDayZero) {
            authStatusResult << [authStatus: 0];
            authStatusResult << [authCode: 'APPLY_OPEN_TIMELYPAY'];
            return authStatusResult;
        }

        def authResult = AuthUtil.getAuthStatus(merchantDayZero?.is_verify);
        authStatusResult << [authStatus: authResult.authStatus];
        authStatusResult << [authCode: authResult.authCode];
        authStatusResult << [failReson: merchantDayZero?.fail_reson ?: ''];
        authStatusResult;
    }

    /**
     * D0手持身份证半身照认证
     *
     *
     * @param merchantId 商户id
     * @param idCard 手持身份证半身照
     * @return
     * @author zhangshb
     * @since 2015-12-4
     */
    static def handIdCardAuth(def merchantId, def idCard) {
        MerchantDao merchantDao = new MerchantDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        def authResult = [isSuccess: false, authCode: 'HANDIDCARD_AUTH_COMMIT_FAIL']
        //异常处理
        try {

            bankAccountDao.db = merchantDao.db;
            FeeRateDao feeRateDao = new FeeRateDao();
            feeRateDao.db = merchantDao.db;
            TerminalDao terminalDao = new TerminalDao();
            terminalDao.db = merchantDao.db;

            def t1Merchant = merchantDao.findCmMerchantByMerId(merchantId);//获取T1商户信息
            log.info("t1Merchant.id=${t1Merchant.id}")
            def t1BankAccount = bankAccountDao.findBankAccountByMerchantId(t1Merchant?.id); //获取T1账户信息
            log.info("t1BankAccount.id=${t1BankAccount.id}")
            def t1MerTerminal = terminalDao.findTerminalByMerId(t1Merchant?.id) //获取T1终端信息
            log.info("t1MerTerminal.id=${t1MerTerminal.id}")
            def cmPersonal = merchantDao.findMerchantById(merchantId);//获取实名信息

            //校验图片信息
            def idCheckResult = AuthUtil.checkImg(idCard);
            if (!idCheckResult.isSuccess) {
                authResult << [authCode: idCheckResult.code];
                return authResult;
            }

            //上传手持身份证半身照
            def fUploadResult = AuthUtil.uploadMerImg(idCard, "id" + t1Merchant.merchant_no);//手持身份证半身照
            log.info("fUploadResult=${fUploadResult}")
            if (!fUploadResult.isSuccess) {
                authResult << [authCode: fUploadResult.code];
                return authResult;
            }

            //获取T1、D0商户关联表
            def merchantDayZero = merchantDao.findD0MerchantByMerId(t1Merchant?.id);

            def isMatchBankRule = false;
            def isSuccess;

            //事务处理
            merchantDao.withTransaction {

                def d0BankCode = bankAccountDao.findDictBankList();
                def bankCodeInfo = Commons.cardBinRule(t1BankAccount?.issuer_bank_name == null ? "" : t1BankAccount?.issuer_bank_name, d0BankCode);
                log.info("bankCodeInfo=" + bankCodeInfo);
                def d0MerchantNo = merchantDao.getMerchantNo();
                log.info("d0MerchantNo=${d0MerchantNo}")
                def d0BankAccountId = merchantDao.getSeqNextval("seq_bankaccount");
                log.info("d0BankAccountId=${d0BankAccountId}")
                def d0SecondMerchantId = merchantDao.getSeqNextval("seq_cmmerchant");
                log.info("d0SecondMerchantId=${d0SecondMerchantId}")
                if (merchantDayZero) {
                    //兼容D0第一版T1和D0共用一个银行账户问题
                    //若D0和T1共享一个银行账户，需分离
                    def isUpdateAcc = false;
                    def d0MerBankAccount;
                    def d0BankAccount = bankAccountDao.findBankAccountByMerchantId(merchantDayZero.merchant_id_tzero);
                    if (d0BankAccount.id == t1BankAccount.id) {
                        if (!bankCodeInfo) {
                            bankAccountDao.addBankAccountInfo(d0BankAccountId);
                        } else {
                            bankAccountDao.addBankAccountInfo(d0BankAccountId, t1BankAccount.account_no, t1BankAccount.account_type, cmPersonal.name, t1BankAccount.bank_name, t1BankAccount.cnaps_no, t1BankAccount.issuer_bank_name, t1BankAccount.settle_rate, t1BankAccount.is_verified, t1BankAccount.enabled);
                            isMatchBankRule = true;
                        }
                        if (isMatchBankRule) {
                            //复制T1的商户结算卡图片到D0
                            def uploadResult = uploadD0File(t1Merchant.merchant_no, d0MerchantNo);
                            if (!uploadResult) {
                                log.error "TI商户银行卡图片复制到D0失败"
                                throw new Exception("TI商户银行卡图片复制到D0失败");
                            }
                        }

                        //更新D0商户账户关联表信息
                        d0MerBankAccount = bankAccountDao.findMerBankAccountByMerchentId(merchantDayZero.merchant_id_tzero);
                        d0MerBankAccount.bank_account_id = d0BankAccountId;
                        bankAccountDao.updateMerchantBankAccount(d0MerBankAccount);
                        isUpdateAcc = true;
                    }

                    //更新D0商户关联表信息
                    merchantDayZero.is_verify = 3;
                    String imageName = "id${t1Merchant.merchant_no}.png";
                    merchantDayZero.credential_image = imageName;
                    if (d0BankAccount.is_verified == 3 || d0BankAccount.is_verified == 1) {
                        merchantDayZero.agency_status = 0;
                    }
                    log.info("merchantDayZero=${merchantDayZero}")
                    merchantDao.update(merchantDayZero)

                    if(!merchantDayZero?.merchant_id_dzero_second){
                        isSuccess = SigninServiceImpl.createD0SecondMerchant(merchantDayZero, d0BankAccount?.id, t1Merchant, t1MerTerminal, d0SecondMerchantId);
                        if(!isSuccess){
                            throw new Exception("D0秒到商户创建失败");
                        }
                    }else{
                        //校验是否需更新结算卡信息
                        if(isUpdateAcc){
                            d0BankAccount = bankAccountDao.findBankAccountByMerchantId(merchantDayZero.merchant_id_tzero);
                            if(d0BankAccount){
                                d0MerBankAccount = bankAccountDao.findMerBankAccountByMerchentId(merchantDayZero?.merchant_id_dzero_second);
                                d0MerBankAccount.bank_account_id = d0BankAccount?.id;
                                bankAccountDao.updateMerchantBankAccount(d0MerBankAccount);
                            }
                        }
                    }

                } else {
                    //初始化创建D0商户关联表
                    def merchantDayZeroSeq = merchantDao.getSeqNextval("seq_mpmerchantdayzero");
                    log.info("merchantDayZeroSeq=${merchantDayZeroSeq}")
                    def d0MerchantId = merchantDao.getSeqNextval("seq_cmmerchant");
                    log.info("d0MerchantId=${d0MerchantId}")
                    String imageName = "id${t1Merchant.merchant_no}.png";
                    merchantDao.addMpMerchantDayZeroInfo(merchantDayZeroSeq, t1Merchant?.id as Long, d0MerchantId, imageName, d0SecondMerchantId);

                    //初始化创建D0银行卡信息
                    //判断原银行卡是否在18家银行之内
                    if (!bankCodeInfo) {
                        bankAccountDao.addBankAccountInfo(d0BankAccountId);
                    } else {
                        bankAccountDao.addBankAccountInfo(d0BankAccountId, t1BankAccount.account_no, t1BankAccount.account_type, cmPersonal.name, t1BankAccount.bank_name, t1BankAccount.cnaps_no, t1BankAccount.issuer_bank_name, t1BankAccount.settle_rate, t1BankAccount.is_verified, t1BankAccount.enabled);
                        isMatchBankRule = true;
                    }

                    //复制T1的商户结算卡图片到D0
                    def uploadResult = uploadD0File(t1Merchant.merchant_no, d0MerchantNo);
                    log.info("uploadResult=${uploadResult}")
                    if (!uploadResult) {
                        log.error "TI商户银行卡图片复制到D0失败"
                        throw new Exception("TI商户银行卡图片复制到D0失败");
                    }

                    //初始化创建D0商户结算卡关联表信息
                    def merBankAccountSeq = merchantDao.getSeqNextval("seq_merchantbankaccount");
                    log.info("merBankAccountSeq=${merBankAccountSeq}")
                    merchantDao.addMerBankAccountInfo(merBankAccountSeq, d0BankAccountId, d0MerchantId);

                    //初始化创建D0商户标扣扣率
                    def d0FeeRateSeq = merchantDao.getSeqNextval("seq_feeratesetting");
                    log.info("d0FeeRateSeq=${d0FeeRateSeq}")
                    def t1FeeRates = feeRateDao.findFeeRatesByMerchantId(t1Merchant?.id);
                    t1FeeRates.id = d0FeeRateSeq;
                    merchantDao.db.dataSet("fee_rate_setting").add(t1FeeRates);

                    //初始化创建D0商户标扣扣率关联表信息
                    def d0MerFeeRateSeq = merchantDao.getSeqNextval("seq_merchantfeerate");
                    log.info("d0MerFeeRateSeq=${d0MerFeeRateSeq}")
                    feeRateDao.addMerchantFeeRateInfo(d0MerFeeRateSeq, d0MerchantId, d0FeeRateSeq);

                    //初始化创建D0的附加扣率
                    def d0ExtraFeeRateSeq = merchantDao.getSeqNextval("seq_feeratesetting");
                    log.info("d0ExtraFeeRateSeq=${d0ExtraFeeRateSeq}")
                    feeRateDao.addFeeRateSettingInfo(d0ExtraFeeRateSeq);

                    //初始化创建D0额外的商户标扣扣率关联信息
                    def d0ExtraMerFeeRateSeq = merchantDao.getSeqNextval("seq_merchantfeerate");
                    log.info("d0ExtraMerFeeRateSeq=${d0ExtraMerFeeRateSeq}")
                    feeRateDao.addMerchantFeeRateInfo(d0ExtraMerFeeRateSeq, d0MerchantId, d0ExtraFeeRateSeq, 1);

                    //初始化创建D0商户的结算周期
                    def d0SettleSettingSeq = merchantDao.getSeqNextval("seq_settlesetting");
                    log.info("d0SettleSettingSeq=${d0SettleSettingSeq}")
//                    def d0EachAmount = SigninServiceImpl.getD0EachAmountByAId(t1Merchant?.agency_id, 0, 50000.00, false);
                    feeRateDao.addSettleSettingInfo(d0SettleSettingSeq, 'D', 0);

                    //初始化创建D0商户信息
                    t1Merchant.id = d0MerchantId;
                    t1Merchant.merchant_no = d0MerchantNo;
                    t1Merchant.settle_setting_id = d0SettleSettingSeq;
                    merchantDao.db.dataSet("cm_merchant").add(t1Merchant);

                    //初始化创建D0终端信息
                    def d0TerminalSeq = merchantDao.getSeqNextval("seq_merchantterminal");
                    log.info("d0TerminalSeq=${d0TerminalSeq}")
                    def d0TerminalN0 = terminalDao.getTerminalNo();
                    log.info("d0TerminalN0=${d0TerminalN0}")
                    t1MerTerminal.id = d0TerminalSeq;
                    t1MerTerminal.terminal_no = d0TerminalN0;
                    t1MerTerminal.merchant_Id = d0MerchantId;
                    t1MerTerminal.merchant_no = d0MerchantNo;
                    terminalDao.db.dataSet("merchant_terminal").add(t1MerTerminal);

                    //创建D0秒到商户
                    def merchantTradeZero = merchantDao.findD0MerchantByMerId(merchantDayZeroSeq)
                    isSuccess = SigninServiceImpl.createD0SecondMerchant(merchantTradeZero, d0BankAccountId, t1Merchant, t1MerTerminal, d0SecondMerchantId, false);
                    if(!isSuccess){
                        throw new Exception("D0秒到商户创建失败");
                    }
                }
            }

            authResult << [isSuccess: true];
            authResult << [isOldCard: isMatchBankRule];
            authResult << [authCode: 'SUCCESS'];
        } catch (Exception e) {
            log.error("手持身份证认证发生异常：${e.getMessage()}");
        }

        authResult;
    }

    /**
     * 获取当前用户的D0账户认证状态
     *
     *
     * @param merchantId 商户id
     * @return
     * @author zhangshb
     * @since 2015-12-6
     */
    static def getDZAccountAuthStatus(def merchantId) {
        def authStatusResult = [authStatus: null, authCode: 'AUTH_NOT_EXIST']
        MerchantDao merchantDao = new MerchantDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        def merchant = merchantDao.findCmMerchantById(merchantId);
        if (!merchant) {
            authStatusResult << [authCode: 'MERCHANT_NOT_FOUND']; return authStatusResult;
        }

        //效验商户认证信息
        def personal = merchantDao.findCmPersonalById(merchantId)
        def bankAccount = bankAccountDao.findBankAccountByMerchantId(merchantId)
        if (!(personal.is_certification == 1 && personal.is_signature == 1 && merchant.review_status == 'accept' && bankAccount.is_verified == 1)) {
            authStatusResult << [authCode: 'MERCHANT_CONFIRM_NOTPASS']; return authStatusResult;
        }

        //校验手持身份证半身照是否已提交审核
        def merchantDayZero = merchantDao.findD0MerchantByMerId(merchant?.id);
        if (!merchantDayZero) {
            authStatusResult << [authCode: 'NEED_AUTH_HANDIDCARD']; return authStatusResult;
        }

        def d0BankAccount = bankAccountDao.findBankAccountByMerchantId(merchantDayZero.merchant_id_tzero);
        if (!d0BankAccount) {
            log.info("D0账户信息未发现,merchantId=${merchantDayZero.merchant_id_tzero}");
            return authStatusResult;
        }

        def authResult = AuthUtil.getAuthStatus(d0BankAccount?.is_verified);
        authStatusResult << [authStatus: authResult.authStatus];
        authStatusResult << [authCode: authResult.authCode];
        authStatusResult << [bankCard: merchantDao.findCmMerchantById(merchantDayZero?.merchant_id_tzero)?.merchant_no];
        authStatusResult << [name: d0BankAccount?.account_name];
        authStatusResult << [accountNo: d0BankAccount?.account_no];
        authStatusResult << [bankName: d0BankAccount?.bank_name];
        authStatusResult << [unionBankNo: d0BankAccount?.cnaps_no];
        authStatusResult << [bankDeposit: d0BankAccount?.issuer_bank_name];
        authStatusResult << [failReson: merchantDayZero?.fail_reson ?: ''];
        authStatusResult;
    }

    /**
     * D0账户认证
     *
     *
     * @param merchantId 商户id
     * @param bankCard 银行卡图片
     * @param name 账户名
     * @param bankDeposit 注册网点
     * @param bankName 开户行
     * @param unionBankNo 联行号
     * @param accountNo 银行卡号
     * @return
     */
    static def dzAccountAuth(
            def merchantId, def bankCard, def name, def bankDeposit, def bankName, def unionBankNo, def accountNo,def userId) {
        def authResult = [isSuccess: false, authCode: 'DZACCOUNT_AUTH_COMMIT_FAIL']
        MerchantDao merchantDao = new MerchantDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        bankAccountDao.db = merchantDao.db;
        DictionaryDao dictionaryDao = new DictionaryDao();
        dictionaryDao.db = merchantDao.db;

        try {
            //校验图片信息
            def cCheckResult = AuthUtil.checkImg(bankCard);
            if (!cCheckResult.isSuccess) {
                authResult << [authCode: cCheckResult.code];
                return authResult;
            }

            def t1Merchant = merchantDao.findCmMerchantByMerId(merchantId);//获取t1商户信息
            log.info("t1Merchant.id=${t1Merchant.id}");
            def merchantDayZero = merchantDao.findD0MerchantByMerId(t1Merchant?.id); //获取d0商户关联表信息
            log.info("merchantDayZero.id=${merchantDayZero.id}");
            def d0Merchant = merchantDao.findCmMerchantById(merchantDayZero?.merchant_id_tzero);//获取d0商户信息
            log.info("d0Merchant.id=${d0Merchant.id}");
            //校验上传的银行名称是否在十八家银行之内
            def d0BankCode = bankAccountDao.findDictBankList();
            log.info("d0BankCode=${d0BankCode}");
            if (!Commons.contains(d0BankCode, bankName)) {
                log.info("上送的银行名称不是认证的银行" + bankName);
                authResult << [authCode: 'BANK_NAME_NOT_SUPPORT']; return authResult;
            }

            //获取银行号相关联的卡信息
            def bankCodeInfo;
            def cardBinInfo = dictionaryDao.findCardbin(accountNo)
            if (cardBinInfo) {
                log.info "cardBinInfo = " + cardBinInfo
                bankCodeInfo = Commons.cardBinRule(cardBinInfo.issuer_name, d0BankCode);
            } else {
                log.info("未知的银行卡号: ${accountNo}");
                authResult << [authCode: 'BANK_NAME_NOT_SUPPORT']; return authResult;
            }

            //是否是支持的cardbin
            if (!bankCodeInfo) {
                log.info("未知的银行卡号: ${accountNo}");
                authResult << [authCode: 'BANK_NAME_NOT_SUPPORT']; return authResult;
            }

            //效验上送卡号对应的银行名跟接口上送的银行名是不是一致
            if (!StringUtils.equals(bankCodeInfo?.bankname, bankName)) {
                log.info("上送的卡号不是: ${bankName}的卡");
                authResult << [authCode: 'CARD_NOT_SUPPORT']; return authResult;
            }

            //校验是否为信用卡
            if (Constants.CARD_TYPE_CREDIT.equalsIgnoreCase(cardBinInfo?.card_type)) {
                log.info "不支持的信用卡 = " + cardBinInfo.issuer_name + " CARD:" + accountNo
                authResult << [authCode: 'CARD_TYPE_NOT_SUPPORT']; return authResult;
            }

            //判断结算的银行卡否是已经被其他D0商户注册的卡
            def isRegisterCard = bankAccountDao.findD0BankAccountByAccountNo(accountNo, merchantDayZero.merchant_id_tzero)
            log.info("isRegisterCard=${isRegisterCard}");
            if (isRegisterCard) {
                authResult << [authCode: 'CARD_CANT_REGISTER']; return authResult;
            }

            //上传银行卡信息
            def uploadResult = AuthUtil.uploadMerImg(bankCard, "c" + d0Merchant.merchant_no);
            log.info("uploadResult=${uploadResult}");
            if (!uploadResult.isSuccess) {
                authResult << [authCode: uploadResult.code];
                return authResult;
            }

            def d0BankAccount = bankAccountDao.findBankAccountByMerchantId(merchantDayZero.merchant_id_tzero);
            //获取d0银行账户信息
            if (d0BankAccount && d0BankAccount.account_name != name) {
                //老数据： D0账户名跟T1实名不一样 删除之前D0账户绑定的银行卡
                def bindCards = dictionaryDao.listBankCards(userId as String)
                //返回绑定信息
                bindCards.each {
                    if (!it.bank_account_name?.equals(name)) {
                        //删除不是当前T1实名认证绑定的银行卡
                        dictionaryDao.deleteBindCardByCardAndAccount(it.bank_card as String, it.bank_account_name as String);
                    }
                }
            }
            log.info("d0BankAccount=${d0BankAccount}");
            def t1BankAccount = bankAccountDao.findBankAccountByMerchantId(merchantDayZero.merchant_id_tone);
            //获取t1银行账户信息
            log.info("t1BankAccount=${t1BankAccount}");
            //事务处理
            merchantDao.withTransaction {
                //变更D0审核状态
                if (merchantDayZero.is_verify == 3 || merchantDayZero.is_verify == 1) {
                    log.info "更新 merchantDayZero = " + merchantDayZero
                    merchantDao.updateMerDayZeroById(0, merchantDayZero.id)
                }

                //更新银行账户信息
                def issuerBankName = AuthUtil.dealIssuerBankName(cardBinInfo?.issuer_name);
                def isUpdateAcc = false;
                def d0MerBankAccount;
                if (d0BankAccount.id == t1BankAccount.id) {
                    def d0BankAccountSeq = merchantDao.getSeqNextval("seq_bankaccount");
                    log.info("d0BankAccountSeq=${d0BankAccountSeq}")
                    bankAccountDao.addBankAccountInfo(d0BankAccountSeq, accountNo, 'private', name, bankDeposit, unionBankNo, issuerBankName, 100, 3);

                    //更新商户银行账户关联表信息
                    d0MerBankAccount = bankAccountDao.findMerBankAccountByMerchentId(d0Merchant.id);
                    log.info("d0MerBankAccount.id=${d0MerBankAccount.id}");
                    d0MerBankAccount.bank_account_id = d0BankAccountSeq;
                    d0MerBankAccount.merchant_id = merchantDayZero.merchant_id_tzero
                    merchantDao.update(d0MerBankAccount);
                    isUpdateAcc = true;
                } else {
                    //如果T1和D0不是使用一条银行卡数据，则更新D0账户信息
                    d0BankAccount.account_no = accountNo;
                    d0BankAccount.account_name = name;
                    d0BankAccount.bank_name = bankDeposit;
                    d0BankAccount.cnaps_no = unionBankNo;
                    d0BankAccount.issuer_bank_name = issuerBankName
                    d0BankAccount.is_verified = 3
                    merchantDao.update(d0BankAccount);
                }

                //校验是否需更新结算卡信息
                if(isUpdateAcc){
                    d0BankAccount = bankAccountDao.findBankAccountByMerchantId(merchantDayZero.merchant_id_tzero);
                    if(d0BankAccount){
                        d0MerBankAccount = bankAccountDao.findMerBankAccountByMerchentId(merchantDayZero.merchant_id_dzero_second);
                        d0MerBankAccount.bank_account_id = d0BankAccount?.id;
                        bankAccountDao.updateMerchantBankAccount(d0MerBankAccount);
                    }
                }
            }
            authResult << [isSuccess: true];
            authResult << [authCode: 'SUCCESS'];
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        authResult;
    }

    static def uploadD0File(def t1MerchantNo, def d0MerchantNo) {
        log.info("t1MerchantNo=${t1MerchantNo}, d0MerchantNo=${d0MerchantNo}");
        def uploadResult = false;
        try {
            def conf = Commons.getConfig();
            def rootDirT1 = new File(conf.path.merchat);
            if (!rootDirT1.exists()) {
                rootDirT1.mkdirs()
            }
            def cardFileT1 = new File(rootDirT1, "c${t1MerchantNo}.png");
            def cardFileD0 = new File(rootDirT1, "c${d0MerchantNo}.png");
            uploadResult = Commons.copyFile(cardFileT1, cardFileD0);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return uploadResult;
    }

    /**
     * 通过银行卡号获取银行名称
     *
     *
     * @param accountNo 银行卡号
     * @author zhangshb
     * @since 2015-12-22
     */
    static def getBankNameByAccountNo(def accountNo){
        BankAccountDao bankAccountDao = new BankAccountDao();
        DictionaryDao dictionaryDao = new DictionaryDao();
        def d0BankCode = bankAccountDao.findDictBankList();
        def cardBinInfo = dictionaryDao.findCardbin(accountNo);
        def bankCodeInfo = Commons.cardBinRule(cardBinInfo.issuer_name, d0BankCode);
        return bankCodeInfo?.bankname;
    }

}
