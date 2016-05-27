package org.groovy.service.merchant.dzauth

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.merchant.BankAccountDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.util.ConvertUtil
import org.groovy.util.merchant.auth.DZAuthUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.merchant.tzauth.DZAuthService
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletRequest
import java.util.regex.Pattern

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service.merchant.dzauth
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/12/4
 * Time : 18:14
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
public class DZAuthServiceImpl implements DZAuthService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, DZAuthServiceImpl.getSimpleName())

    /**
     * D0手持身份证半身照认证
     *
     * @param idCardFile 身份证图片
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @Override
    public Object handIdCardAuth(MultipartFile idCardFile, HttpServletRequest request) {

        //获取请求参数
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def merchantId = personal?.merchantId;

        //校验 D0手持身份证半身照认证状态
        def authStatusInfoList = DZAuthUtil.getHandIdCardAuthStatus(merchantId);
        if(null==authStatusInfoList.authStatus || 1==authStatusInfoList.authStatus || 3==authStatusInfoList.authStatus){
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }

        //调用 D0手持身份证半身照认证入口
        def authResultList = DZAuthUtil.handIdCardAuth(merchantId, idCardFile);
        if(!authResultList.isSuccess){
            return Commons.fail(null, authResultList.authCode, Constants.error_code_mapping."${authResultList.authCode}");
        }

        return Commons.success([isOldCard : authResultList.isOldCard], '手持身份证半身照认证信息已提交,请耐心等待');
    }

    /**
     * 获取当前用户的手持身份证半身照认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-5
     */
    @Override
    public Object getHandIdCardAuthStatus(HttpServletRequest request) {
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def authStatusInfoList = DZAuthUtil.getHandIdCardAuthStatus(personal?.merchantId);
        if(null == authStatusInfoList.authStatus){
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }
        def authResultList = [authStatus:authStatusInfoList.authStatus];
        if(0 != authStatusInfoList.authStatus){
            String idCard = "id${personal?.merchantNo}.png";
            authResultList << [idCard:idCard];
            def failReson = authStatusInfoList.failReson;
            if(failReson){
                def failResonMap = ConvertUtil.strConvertToMap(failReson);
                if (failResonMap['2']){
                    authResultList << [handIdCardReason:failResonMap.get('2')];
                }
            }
        }
        return Commons.success(authResultList, '查询成功');
    }


    /**
     * D0账户认证
     *
     * @param bankCardFile 银行卡图片
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-12-6
     */
    @Override
    public Object dzAccountAuth(MultipartFile bankCardFile, HttpServletRequest request) {

        //获取请求参数
        def params = Commons.parseRequest(request);
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def merchantId = personal?.merchantId;
//        def name = URLDecoder.decode(params.name?.trim().replaceAll(Pattern.compile("\r|\n"), ''), 'UTF-8');
        def bankDeposit = URLDecoder.decode(params.bankDeposit?.trim(), 'UTF-8');
        def bankName = params.bankName;
        def unionBankNo = params.unionBankNo?.trim();
        def accountNo = params.accountNo?.trim();
        //校验请求参数
        if( !bankDeposit || !bankName || !unionBankNo || !accountNo){
            return Commons.fail(null, 'REQUEST_NOT_NULL', Constants.error_code_mapping.REQUEST_NOT_NULL);
        }
        //校验 D0账户认证状态
        def authStatusInfoList = DZAuthUtil.getDZAccountAuthStatus(merchantId);
        if(null==authStatusInfoList.authStatus || 1==authStatusInfoList.authStatus || 3==authStatusInfoList.authStatus){
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }
        //调用 D0账户认证入口
        def authResultList = DZAuthUtil.dzAccountAuth(merchantId, bankCardFile, personal.name, bankDeposit, bankName, unionBankNo, accountNo,attrNames.user_id);
        if(!authResultList.isSuccess){
            return Commons.fail(null, authResultList.authCode, Constants.error_code_mapping."${authResultList.authCode}");
        }
        return Commons.success(null, '账户认证信息已提交,请耐心等待');
    }

    /**
     * 获取当前用户的D0账户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-5
     */
    @Override
    public Object getDZAccountAuthStatus(HttpServletRequest request) {
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def authStatusInfoList = DZAuthUtil.getDZAccountAuthStatus(personal?.merchantId);
        if(null == authStatusInfoList.authStatus){
            return Commons.fail(null, authStatusInfoList.authCode, Constants.error_code_mapping."${authStatusInfoList.authCode}");
        }
        def authResultList = [authStatus:authStatusInfoList.authStatus];
        if(0 != authStatusInfoList.authStatus){
            String bankCard = "c${authStatusInfoList.bankCard}.png";
            def accountNo = authStatusInfoList.accountNo;
            authResultList << [bankCard:bankCard,
                               name:authStatusInfoList.name,
                               accountNo:accountNo,
                               bankName:DZAuthUtil.getBankNameByAccountNo(accountNo)?:authStatusInfoList.bankDeposit,
                               unionBankNo:authStatusInfoList.unionBankNo,
                               bankDeposit:authStatusInfoList.bankName
            ];
            def failReson = authStatusInfoList.failReson;
            if(failReson){
                def failResonMap = ConvertUtil.strConvertToMap(failReson);
                if (failResonMap['3']){
                    authResultList << [accountReason:failResonMap.get('3')];
                }
            }
        }
        return Commons.success(authResultList, '查询成功');
    }

    /**
     * 获取当前用户的D0认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-12
     */
    @Override
    public Object getDZAuthStatus(HttpServletRequest request) {
        MerchantDao merchantDao = new MerchantDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        def attrNames = Commons.initUserRequestParams(request);
        def personal = Commons.findUserInfoByMobileNo(attrNames.user_id);
        def merchantDayZero = merchantDao.findD0MerchantByMerId(personal?.merchantId);
        if(!merchantDayZero){
            log.info("merchantId=${personal?.merchantId}")
            return Commons.fail(null, 'MERCHANT_OPRACTOR_NOT_FOUND', Constants.error_code_mapping.MERCHANT_OPRACTOR_NOT_FOUND);
        }
        def d0BankAccount = bankAccountDao.findBankAccountByMerchantId(merchantDayZero.merchant_id_tzero);
        def handCardStatus = merchantDayZero?.is_verify as String
        def bankAccountStatus = d0BankAccount?.is_verified as String
        def status = handCardStatus + bankAccountStatus
        def result = [status:status];
        def failMap = ConvertUtil.strConvertToMap(merchantDayZero?.fail_reson);
        if(failMap['2']){
            result << [handIdCardReason: failMap.get('2')]
        }
        if(failMap['3']){
            result << [accountReason: failMap.get('3')]
        }
        return Commons.success(result, '查询成功');
    }
}
