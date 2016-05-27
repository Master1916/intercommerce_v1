package org.groovy.util.merchant.auth

import org.groovy.common.Commons
import org.groovy.dao.dictionary.DictionaryDao
import org.groovy.dao.merchant.BankAccountDao
import org.groovy.dao.merchant.MerchantDao
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.springframework.web.multipart.MultipartFile

import java.sql.Timestamp

/**
 * 四审辅助类
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.util.merchant
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/30
 * Time : 19:33
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
public class AuthUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, AuthUtil.getSimpleName())

    /**
     * 获取当前用户的实名认证状态
     *
     *
     * @param merchantId 商户id
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    static def getRealNameAuthStatus(def merchantId){
        def authStatus = null;
        def authCode = 'AUTH_NOT_EXIST';
        MerchantDao merchantDao = new MerchantDao();
        def personal = merchantDao.findCmPersonalById(merchantId);
        if(personal){
            def authResult = getAuthStatus(personal.is_certification);
            authStatus = authResult.authStatus;
            authCode = authResult.authCode;
        }
        [authStatus:authStatus, authCode: authCode];
    }

    /**
     * 实名认证
     *
     *
     * @param userId 商户操作员id
     * @param merchantId 商户id
     * @param name 姓名
     * @param idNumber 身份证号
     * @param personal 身份证正面照
     * @param personalBack 身份证背面照
     * @author zhangshb
     * @since 2015-12-1
     */
    static def realNameAuth(def userId, def merchantId, def name, def idNumber, MultipartFile personal, MultipartFile personalBack){
        MerchantDao merchantDao = new MerchantDao();

        BankAccountDao bankAccountDao = new BankAccountDao();
        bankAccountDao.db = merchantDao.db;

        def authResult = [isSuccess: false, authCode: 'REALNAME_AUTH_COMMIT_FAIL']

        //事务处理
        try{

            //检测图片信息
            def fCheckResult = checkImg(personal);
            if(!fCheckResult.isSuccess){
                authResult << [authCode : fCheckResult.code];
                return authResult;
            }

            def bCheckResult = checkImg(personalBack);
            if(!bCheckResult.isSuccess){
                authResult << [authCode : bCheckResult.code];
                return authResult;
            }

            //校验商户是否存在
            def merchant = merchantDao.findCmMerchantById(merchantId);
            if(!merchant){
//                authResult << [authCode : 'MERCHANT_NOT_FOUND'];
                return authResult;
            }

            def cmPersonal = merchantDao.findCmPersonalById(merchantId);

            //上传身份证图片信息
            def fUploadResult = uploadMerImg(personal, "f"+merchant.merchant_no);//身份证正面照
            if(!fUploadResult.isSuccess){
                authResult << [authCode : fUploadResult.code];
                return authResult;
            }
            def bUploadResult = uploadMerImg(personalBack, "b"+merchant.merchant_no);//身份证背面照
            if(!bUploadResult.isSuccess){
                authResult << [authCode : bUploadResult.code];
                return authResult;
            }

            //校验商户操作员信息
            def merOperator = merchantDao.findMerOperatorById(userId);
            if(!merOperator){
                log.info("商户操作员信息不存在,id=${userId}");
//                authResult << [authCode : 'MEROPERATOR_NOT_FOUND'];
                return authResult;
            }

            merchantDao.withTransaction{
                //变更商户操作员信息
                merOperator.real_name = name;
                merOperator.nick_name = name;
                merchantDao.update(merOperator);

                //如果商户未过首次审核(已经提交账户信息)，则修改实名的时候要修改账户名
                if (cmPersonal.merchant_auth_pass_flag == 0) {
                    def bankAccount = bankAccountDao.findBankAccountByMerchantId(merchant.id);
                    //判断是否存在账户信息如果存在且账户名跟新修改的不一致，则替换账户名
                    if (bankAccount && !name.equals(bankAccount.account_name)) {
                        bankAccount.account_name = name;
                        bankAccountDao.update(bankAccount)
                    }
                }

                //变更商户更新时间
                merchant.last_updated = new Timestamp(new Date().time);
                merchantDao.update(merchant);

                //变更个人商户信息
                cmPersonal.name = name;
                cmPersonal.is_certification = 3;
                cmPersonal.id_no = idNumber;
                cmPersonal.real_reason = '';
                merchantDao.update(cmPersonal);

            }
            authResult << [isSuccess : true];
            authResult << [authCode : 'SUCCESS'];
        }catch (Exception e){
            log.error(e.getMessage());
        }

        authResult;
    }

    /**
     * 获取当前用户的账户认证状态
     *
     *
     * @param merchantId 商户id
     * @author zhangshb
     * @since 2015-12-1
     */
    static def getAccountAuthStatus(def merchantId){
        BankAccountDao bankAccountDao = new BankAccountDao();
        def authResultMap = [authStatus:null, authCode:'AUTH_NOT_EXIST'];
        def account = bankAccountDao.findBankAccountByMerchantId(merchantId);
        if(account){
            def authResult = getAuthStatus(account.is_verified);
            authResultMap << [authStatus : authResult.authStatus];
            authResultMap << [authCode : authResult.authCode];
            authResultMap << [accountNo : account?.account_no];
            authResultMap << [name : account?.account_name];
            authResultMap << [bankName : account?.bank_name];
            authResultMap << [unionBankNo : account?.cnaps_no];
        }
        authResultMap;
    }

    /**
     * 账户认证
     *
     *
     * @param merchantId 商户id
     * @param accountNo 卡号
     * @param cardImg 卡号照片
     * @param name 账户名
     * @param bankName 开户行
     * @param unionBankNo 联行号
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    static def accountAuth(def merchantId, def accountNo, def cardImg, def bankName, def unionBankNo){
        MerchantDao merchantDao = new MerchantDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        DictionaryDao dictionaryDao = new DictionaryDao();
        def authResult = [isSuccess:false, authCode:'ACCOUNT_AUTH_COMMIT_FAIL']

        try{

            bankAccountDao.db = merchantDao.db;

            //校验图片信息
            def cCheckResult = checkImg(cardImg);
            if(!cCheckResult.isSuccess){
                authResult << [authCode : cCheckResult.code];
                return authResult;
            }

            //校验卡并信息
            def cardbin = dictionaryDao.findCardbin(accountNo);
            if(!cardbin){
                authResult << [authCode : 'NOT_SUPPORT_CARD']; return authResult;
            }
            if("debit" != cardbin.card_type){
                authResult << [authCode : 'CREDIT_CARD_CANOT_REGISTER']; return authResult;
            }

            //校验银行卡是否已经绑定过
            def account = bankAccountDao.findBankAccountByAccountNo(accountNo);
            if(account){
                if(account?.merchant_id!=merchantId){
                    authResult << [authCode : 'CARD_CANT_REGISTER']; return authResult;
                }
            }
            account = bankAccountDao.findBankAccountByMerchantId(merchantId);

            //校验商户是否被关停
            if(4 == account.is_verified as int){
                authResult << [authCode : 'MERCHANT_CLOSE_DOWN']; return authResult;
            }

            //校验商户是否存在
            def merchant = merchantDao.findCmMerchantById(merchantId);
            if(!merchant){
                authResult << [authCode : 'MERCHANT_NOT_FOUND']; return authResult;
            }

            //校验个人商户信息
            def cmPersonal = merchantDao.findCmPersonalById(merchantId);
            if(!cmPersonal){
                authResult << [authCode : 'MERCHANT_OPRACTOR_NOT_FOUND']; return authResult;
            }

            //上传卡号照片
            def uploadResult = uploadMerImg(cardImg, "c"+merchant.merchant_no);
            if(!uploadResult.isSuccess){
                authResult << [authCode : uploadResult.code]; return authResult;
            }

            merchantDao.withTransaction {
                //变更个人商户信息
                cmPersonal.account_reason = ''
                merchantDao.update(cmPersonal);

                //变更商户最后更新时间
                merchant.last_updated = new Timestamp(new Date().time)
                merchantDao.update(merchant)

                //变更结算银行账号信息
                bankAccountDao.updateBankAccount(accountNo, cmPersonal.name, bankName, unionBankNo, dealIssuerBankName(cardbin?.issuer_name), account?.id);
            }

            authResult << [isSuccess : true];
            authResult << [authCode : 'SUCCESS'];
        }catch(Exception e){
            log.error(e.getMessage());
        }

        authResult;
    }


    /**
    * 获取当前用户的商户认证状态
    *
    *
    * @param accountNo 账户号
    * @author zhangshb
    * @since 2015-12-2
    */
    static def getMerchantAuthStatus(def merchantId){
        MerchantDao merchantDao = new MerchantDao();
        def authStatus = null;
        def authCode = 'MERCHANT_NOT_FOUND';
        def merchant = merchantDao.findCmMerchantById(merchantId);
        if(merchant){
            def reviewStatus = merchant.review_status;
            if(reviewStatus == "submit"){
                authStatus = 3;
                authCode = 'AUTH_CHECKING';
            }else if(reviewStatus == "accept"){
                authStatus = 1;
                authCode = 'AUTH_PASSED';
            }else if(reviewStatus == "reject"){
                authStatus = 2;
                authCode = 'AUTH_FAIL';
            }else{
                authStatus = 0;
                authCode = 'AUTH_NOT_COMMIT';
            }
        }
        [authStatus:authStatus, authCode:authCode];
    }

    /**
     * 商户认证
     *
     *
     * @param merchantId 商户id
     * @param companyName 商户名称
     * @param business 营业执照
     * @param regPlace 经营地址
     * @param businessLicense 营业执照号
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    static def merchantAuth(def merchantId, def companyName, def business, def regPlace, def businessLicense){
        MerchantDao merchantDao = new MerchantDao();
        def authResult = [isSuccess:false, authCode:'MERCHANT_AUTH_COMMIT_FAIL']

        try{
            def merchant = merchantDao.findCmMerchantById(merchantId);

            //校验图片信息
            def qCheckResult = checkImg(business);
            if(!qCheckResult.isSuccess){
                authResult << [authCode : qCheckResult.code];
                return authResult;
            }

            //校验个人商户信息
            def cmPersonal = merchantDao.findCmPersonalById(merchantId);
            if(!cmPersonal){
//                authResult << [authCode : 'MERCHANT_OPRACTOR_NOT_FOUND'];
                return authResult;
            }

            //上传营业执照
            def uploadResult = uploadMerImg(business, "q"+merchant.merchant_no);
            if(!uploadResult.isSuccess){
                authResult << [authCode : uploadResult.code];
                return authResult;
            }

            //事务处理
            merchantDao.withTransaction {

                //变更商户信息
                merchant.last_updated = new Timestamp(new Date().time)
                merchant.merchant_name = companyName
                merchant.review_status = 'submit'
                merchantDao.update(merchant)

                //更新商户及时付和秒到业务商户名
                def tradeMerchant = merchantDao.findD0MerchantByMerId(merchant.id);
                if (tradeMerchant) {
                    //获取及时付商户信息
                    def merchantD0 = merchantDao.findCmMerchantById(tradeMerchant.merchant_id_tzero);
                    //更新及时付商户名
                    merchantD0.merchant_name = companyName
                    merchantDao.update(merchantD0)
                    //判断是否有秒到业务
                    if (tradeMerchant.merchant_id_dzero_second) {
                        //获取秒到业务的商户
                        def merchantD0Sec = merchantDao.findCmMerchantById(tradeMerchant.merchant_id_dzero_second);
                        //更新秒到商户名
                        merchantD0Sec.merchant_name = companyName
                        merchantDao.update(merchantD0Sec)
                    }
                }

                //变更个人商户信息
                cmPersonal.business_place = regPlace
                cmPersonal.merchant_reason = ''
                cmPersonal.business_license_code = businessLicense
                merchantDao.update(cmPersonal)
            }

            authResult << [isSuccess : true];
            authResult << [authCode : 'SUCCESS'];
        }catch(Exception e){
            log.error(e.getMessage());
        }

        authResult;
    }

    /**
     * 获取当前用户的签名认证状态
     *
     *
     * @param accountNo 账户号
     * @author zhangshb
     * @since 2015-12-2
     */
    static def getSignatureAuthStatus(def merchantId){
        def authStatus = null;
        def authCode = 'AUTH_NOT_EXIST';
        MerchantDao merchantDao = new MerchantDao();
        def personal = merchantDao.findCmPersonalById(merchantId);
        if(personal){
            def authResult = getAuthStatus(personal.is_signature);
            authStatus = authResult.authStatus;
            authCode = authResult.authCode;
        }
        [authStatus:authStatus, authCode: authCode];
    }

    /**
     * 签名认证
     *
     *
     * @param signature 签名照片
     * @return
     * @author zhangshb
     * @since 2015-12-2
     */
    static def signatureAuth(def merchantId, def signature){
        MerchantDao merchantDao = new MerchantDao();
        def authResult = [isSuccess:false, authCode:'MERCHANT_AUTH_COMMIT_FAIL']

        try{

            //校验图片信息
            def sCheckResult = checkImg(signature);
            if(!sCheckResult.isSuccess){
                authResult << [authCode : sCheckResult.code];
                return authResult;
            }

            def personal = merchantDao.findCmPersonalById(merchantId);

            //校验商户是否存在
            def merchant = merchantDao.findCmMerchantById(merchantId);
            if(!merchant){
//                authResult << [authCode : 'MERCHANT_NOT_FOUND'];
                return authResult;
            }

            //上传签名照
            def uploadResult = uploadMerImg(signature, "s"+merchant.merchant_no);
            if(!uploadResult.isSuccess){
                authResult << [authCode : uploadResult.code];
                return authResult;
            }

            merchantDao.withTransaction {
                //变更个人商户信息
                personal.is_signature = 3
                personal.signature_reason = ''
                merchantDao.update(personal)

                //变更商户最后更新时间
                merchant.last_updated = new Timestamp(new Date().time)
                merchantDao.update(merchant)
            }

            authResult << [isSuccess : true];
            authResult << [authCode : 'SUCCESS'];
        }catch(Exception e){
            log.error(e.getMessage());
        }

        authResult;
    }

    static def dealIssuerBankName(def issuerName){
        def issuerBankName = null
        try{
            def issuerNo = issuerName?.split(/\(/)[1][0..3]
            if(!issuerNo){
                issuerBankName = issuerName?.split(/\(/)[0]
            }else{
                DictionaryDao dd = new DictionaryDao();
                def issuer = dd.findIssuerByIssuerNo(issuerNo);
                issuerBankName = issuer?issuer.issuer_name:issuerName
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        issuerBankName;
    }

    static def getStrLen(def str){
        int len = 0;
        for(int i=0; i < str.length(); i++){
            String temp = str.substring(i,i+1);
            if(temp.getBytes().length == 1){
                len ++;
            } else {
                len = len + 2
            }
        }
        return len;
    }

    static def getAuthStatus(def status){
        def authStatus = status;
        def authCode = 'AUTH_NOT_COMMIT';
        if(1 == authStatus){
            authCode = 'AUTH_PASSED';
        }else if(3 == authStatus){
            authCode = 'AUTH_CHECKING';
        }else if(2 == authStatus){
            authCode = 'AUTH_FAIL';
        }else if(4 == authStatus){
            authCode = 'AUTH_CLOSEDOWN';
        }
        [authStatus:authStatus, authCode:authCode];
    }

    static def uploadMerImg(MultipartFile img, def imgName){
        def authResult = [isSuccess:false, code:'UPLOAD_FAIL'];
        try{
            def conf = Commons.getConfig();
            String imgNames = "${imgName}.png";
            def imgFile = new File(conf.path.merchat, imgNames);
            if (!imgFile.exists()) {
                imgFile.mkdirs()
            }
            img.transferTo(imgFile);
            authResult << [isSuccess : true];
            return authResult;
        }catch (Exception e){
            log.error(e.getMessage());
        }
        authResult;
    }

    static def uploadSignImg(MultipartFile img, def imgName){
        def authResult = [isSuccess:false, code:'UPLOAD_FAIL'];
        try{
            def conf = Commons.getConfig();
            String imgNames = "${imgName}.png";
            def imgFile = new File(conf.path.signature, imgNames);
//            def imgFile = new File(conf.path.merchat, imgNames);
            if (!imgFile.exists()) {
                imgFile.mkdirs()
            }
            img.transferTo(imgFile);
            authResult << [isSuccess : true];
            return authResult;
        }catch (Exception e){
            log.error(e.getMessage());
        }
        authResult;
    }

    static def checkImg(MultipartFile img){
        def authResult = [isSuccess:false, code:'IMG_FORMAT_NOT_CORRECT'];
        try{
            if(!img || img.isEmpty()){
                authResult << [code : 'ILLEGAL_ARGUMENT']; return authResult;
            }
            if(img.getSize() < 1024 || img.getSize() > 2*1024*1024){
                authResult << [code : 'IMAGE_SIZE_NOT_QUALIFIED']; return authResult;
            }
            authResult << [isSuccess:true];
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return authResult;
    }

    /**
     * 通过商户id变更账户名称
     *
     *
     * @param merchantId
     * @param name
     * @return
     */
    static def updateBankAccountByMerId(def merchantId, def name){
        BankAccountDao bankAccountDao = new BankAccountDao();
        def result = false;
        try{
            bankAccountDao.withTransaction {
                def bankAccount = bankAccountDao.findBankAccountByMerchantId(merchantId);
                if(bankAccount){
                    bankAccount.account_name = name;
                    bankAccountDao.update(bankAccount);
                }
            }
            result = true;
        }catch (Exception e){
            log.info("变更账户名称发生异常，merchantId:${merchantId},name:${name},异常信息：${e.getMessage()}");
        }

        result;
    }

}
