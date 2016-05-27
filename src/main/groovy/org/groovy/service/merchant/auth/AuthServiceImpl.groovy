package org.groovy.service.merchant.auth

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.merchant.BankAccountDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.service.user.SigninServiceImpl
import org.groovy.util.PosUtil
import org.groovy.util.merchant.auth.AuthUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.merchant.auth.AuthService

import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletRequest
import java.util.regex.Pattern

/**
 * 商户四审相关的服务
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service.merchant.auth
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/30
 * Time : 15:16
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
public class AuthServiceImpl implements AuthService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, AuthServiceImpl.getSimpleName())

    /**
     * 实名认证
     *
     *
     * @param request
     * @return
     * @author zhangshb
     * @since 2015-12-01
     */
    @Override
    public Object realNameAuth(MultipartFile personalFile, MultipartFile personalBackFile, HttpServletRequest request) {
        //获取请求参数
        def params = Commons.parseRequest(request);
        def attrNames = Commons.initUserRequestParams(request);
        def mobile = attrNames.user_id;
        def personal = Commons.findUserInfoByMobileNo(mobile);
        def merchantId = personal?.merchantId;
        def merOperatorId = personal?.merOperatorId;
        def name = URLDecoder.decode(params.name?.trim() as String, Constants.CHARSET_UTF_8);
        def idNumber = params.idNumber?.trim();
        //校验姓名、身份证信息
        if (!name || !idNumber || !mobile) {
            return Commons.fail(null, 'REQUEST_NOT_NULL', Constants.error_code_mapping.REQUEST_NOT_NULL);
        }

        //当 上传信息是本人信息 或者 4审状态是首次审核 则可以修改商户4审信息
        if (personal.T1AuthFirstPass == 1 && !(name.equals(personal.name) && idNumber.equals(personal.idNumber))) {
            log.info "personal.name : " + personal.name + " personal.idNumber:" + personal.idNumber + " personal.T1AuthFirstPass:" + personal.T1AuthFirstPass
            return Commons.fail(null, 'MERCHANT_REALNAME_ERROR', Constants.error_code_mapping.MERCHANT_REALNAME_ERROR);
        }

        //校验实名认证状态
        def authStatusInfoList = AuthUtil.getRealNameAuthStatus(merchantId);
        log.info("authStatusInfoList=${authStatusInfoList}")
        if (null == authStatusInfoList.authStatus || 1 == authStatusInfoList.authStatus || 3 == authStatusInfoList.authStatus) {
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }

        //调用实名认证入口
        def authResultList = AuthUtil.realNameAuth(merOperatorId, merchantId, name, idNumber, personalFile, personalBackFile);
        log.info("authResultList=${authResultList}")
        if (!authResultList.isSuccess) {
            return Commons.fail(null, authResultList.authCode, Constants.error_code_mapping."${authResultList.authCode}");
        }

        return Commons.success(null, '实名认证信息已提交,请耐心等待');
    }

    /**
     * 获取当前用户的实名认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @Override
    public Object getRealNameAuthStatus(HttpServletRequest request) {
        def attrNames = Commons.initUserRequestParams(request);
        def mobileNo = attrNames.user_id;
        def personal = Commons.findUserInfoByMobileNo(mobileNo);
        def authStatusInfoList = AuthUtil.getRealNameAuthStatus(personal?.merchantId);
        if (null == authStatusInfoList.authStatus) {
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }
        def authResultList = [authStatus: authStatusInfoList.authStatus];

        //校验传统POS实名认证信息是否通过
        def authMap = PosUtil.posRealNameAuthStatus(mobileNo);
        if (!(0 != authMap.size() && 0 == authMap.code)) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', "实名认证失败");
        }
        if (1 == authMap.status || 2 == authMap.status) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', "请先完成传统POS实名认证");
        }
        if (3 == authMap.status) {
            authResultList << [idCard: authMap.idCard, realName: authMap.realName];
        }

        if (0 != authStatusInfoList.authStatus) {
            String fPersonal = "f${personal?.merchantNo}.png";
            String personalBack = "b${personal?.merchantNo}.png";
            authResultList << [name: personal?.name, idNumber: personal?.idNumber, personal: fPersonal, personalBack: personalBack, realReason: personal?.realReason];
        }
        return Commons.success(authResultList, '查询成功');
    }

    /**
     * 账户认证
     *
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    @Override
    public Object accountAuth(MultipartFile cardFile, HttpServletRequest request) {

        //获取请求参数
        def params = Commons.parseRequest(request);
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def merchantId = personal?.merchantId;
        def accountNo = params.accountNo?.trim();
//        def name = URLDecoder.decode(params.name?.trim().replaceAll(Pattern.compile("\r|\n"), ''), Constants.CHARSET_UTF_8);
        def bankName = URLDecoder.decode(params.bankName?.trim(), Constants.CHARSET_UTF_8);
        def unionBankNo = params.unionBankNo?.trim();

        //校验请求参数
        if (!accountNo || !bankName || !unionBankNo) {
            return Commons.fail(null, 'REQUEST_NOT_NULL', Constants.error_code_mapping.REQUEST_NOT_NULL);
        }

        //校验账户认证状态
        def authStatusInfoList = AuthUtil.getAccountAuthStatus(merchantId);
        if (null == authStatusInfoList.authStatus || 1 == authStatusInfoList.authStatus || 3 == authStatusInfoList.authStatus) {
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }

        //调用账户认证入口
        def authResultList = AuthUtil.accountAuth(merchantId, accountNo, cardFile, bankName, unionBankNo)
        if (!authResultList.isSuccess) {
            return Commons.fail(null, authResultList.authCode, Constants.error_code_mapping."${authResultList.authCode}");
        }

        return Commons.success(null, '账户认证信息已提交,请耐心等待');
    }

    /**
     * 获取当前用户的账户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    @Override
    public Object getAccountAuthStatus(HttpServletRequest request) {
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def authStatusInfoList = AuthUtil.getAccountAuthStatus(personal?.merchantId);
        if (null == authStatusInfoList.authStatus) {
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }
        def authResultList = [authStatus: authStatusInfoList.authStatus];
        if (0 != authStatusInfoList.authStatus) {
            String card = "c${personal?.merchantNo}.png";
            authResultList << [accountNo: authStatusInfoList.accountNo, name: authStatusInfoList.name, bankName: authStatusInfoList.bankName, unionBankNo: authStatusInfoList.unionBankNo, card: card, accountReason: personal?.accountReason];
        }
        return Commons.success(authResultList, '查询成功');
    }

    /**
     * 商户认证
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @Override
    public Object merchantAuth(MultipartFile businessFile, HttpServletRequest request) {

        //获取请求参数
        def params = Commons.parseRequest(request);
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def merchantId = personal?.merchantId;
        def companyName = URLDecoder.decode(params.companyName?.trim(), Constants.CHARSET_UTF_8);
        def regPlace = URLDecoder.decode(params.regPlace?.trim(), Constants.CHARSET_UTF_8);
        def businessLicense = params.businessLicense?.trim();

        //校验请求参数
        if (!companyName || !regPlace || !businessLicense) {
            return Commons.fail(null, 'REQUEST_NOT_NULL', Constants.error_code_mapping.REQUEST_NOT_NULL);
        }

        //校验商户信息
        //校验商户名称
        def companyNameLen = AuthUtil.getStrLen(companyName);
        if (companyNameLen > 40) {
            return Commons.fail(null, 'NAME_IS_TOO_LONG', Constants.error_code_mapping.NAME_IS_TOO_LONG);
        }

        //校验经营地址
        def regPlaceLen = AuthUtil.getStrLen(regPlace);
        if (regPlaceLen > 128) {
            return Commons.fail(null, 'ADDR_IS_TOO_LONG', Constants.error_code_mapping.ADDR_IS_TOO_LONG);
        }

        //校验营业执照号
        def businessLicenseLen = AuthUtil.getStrLen(businessLicense);
        if (!(businessLicenseLen >= 7 && businessLicenseLen <= 30)) {
            return Commons.fail(null, 'BUSINESSLICENSE_IS_TOO_LONG', Constants.error_code_mapping.BUSINESSLICENSE_IS_TOO_LONG);
        }

        //校验商户认证状态
        def authStatusInfoList = AuthUtil.getMerchantAuthStatus(merchantId);
        if (null == authStatusInfoList.authStatus || 1 == authStatusInfoList.authStatus || 3 == authStatusInfoList.authStatus) {
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }

        //调用商户认证入口
        def authResultList = AuthUtil.merchantAuth(merchantId, companyName, businessFile, regPlace, businessLicense);
        if (!authResultList.isSuccess) {
            return Commons.fail(null, authResultList.authCode, Constants.error_code_mapping."${authResultList.authCode}");
        }

        return Commons.success(null, '商户认证信息已提交,请耐心等待');

    }

    /**
     * 获取当前用户的商户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @Override
    public Object getMerchantAuthStatus(HttpServletRequest request) {
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def authStatusInfoList = AuthUtil.getMerchantAuthStatus(personal?.merchantId);
        if (null == authStatusInfoList.authStatus) {
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }
        def authResultList = [authStatus: authStatusInfoList.authStatus];
        if (0 != authStatusInfoList.authStatus) {
            String business = "q${personal?.merchantNo}.png";
            authResultList << [companyName: personal?.merchantName, business: business, regPlace: personal?.regPlace, businessLicense: personal?.businessLicense, merchantReason: personal?.merchantReason];
        }
        return Commons.success(authResultList, '查询成功');
    }

    /**
     * 签名认证
     *
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-12-2
     */
    @Override
    public Object signatureAuth(MultipartFile signatureFile, HttpServletRequest request) {

        //获取请求参数
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def merchantId = personal?.merchantId;

        //校验签名认证状态
        def authStatusInfoList = AuthUtil.getSignatureAuthStatus(merchantId);
        if (null == authStatusInfoList.authStatus || 1 == authStatusInfoList.authStatus || 3 == authStatusInfoList.authStatus) {
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }

        //调用签名认证入口
        def authResultList = AuthUtil.signatureAuth(merchantId, signatureFile)
        if (!authResultList.isSuccess) {
            return Commons.fail(null, authResultList.authCode, Constants.error_code_mapping."${authResultList.authCode}");
        }

        return Commons.success(null, '签名认证信息已提交,请耐心等待');
    }

    /**
     * 获取当前用户的商户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @Override
    public Object getSignatureAuthStatus(HttpServletRequest request) {
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def authStatusInfoList = AuthUtil.getSignatureAuthStatus(personal?.merchantId);
        if (null == authStatusInfoList.authStatus) {
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }
        def authResultList = [authStatus: authStatusInfoList.authStatus, name: personal?.name];
        if (0 != authStatusInfoList.authStatus) {
            String signature = "s${personal?.merchantNo}.png";
            authResultList << [signature: signature, signatureReason: personal?.signatureReason];
        }
        return Commons.success(authResultList, '查询成功');
    }

    /**
     * 获取当前用户的四审认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @Override
    public Object getAuthStatus(HttpServletRequest request) {
        MerchantDao merchantDao = new MerchantDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def merchant = merchantDao.findMerchantById(personal?.merchantId)
        def cmPersonal = merchantDao.findCmPersonalById(personal?.merchantId)
        def bankAccount = bankAccountDao.findBankAccountByMerchantId(personal?.merchantId)

        if (!merchant || !cmPersonal || !bankAccount) {
            return Commons.fail(null, 'MERCHANT_OPRACTOR_NOT_FOUND', Constants.error_code_mapping.MERCHANT_OPRACTOR_NOT_FOUND);
        }

        def realStatus = cmPersonal?.is_certification as int
        def merchantStatus = new SigninServiceImpl().realStatusMap.get(merchant?.review_status);
        def accountStatus = bankAccount.is_verified as int
        def signatureStatus = cmPersonal?.is_signature as int
        def status = realStatus + merchantStatus + accountStatus + signatureStatus
        return Commons.success([status: status, merchantReason: personal?.merchantReason, realReason: personal?.realReason, signatureReason: personal?.signatureReason, accountReason: personal?.accountReason], '查询成功');

    }
}
