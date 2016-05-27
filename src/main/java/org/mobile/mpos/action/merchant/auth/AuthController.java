package org.mobile.mpos.action.merchant.auth;

import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.merchant.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * D0商户四审
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.action.merchant.auth
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/30
 * Time : 14:53
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 实名认证
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_REALNAMEAUTH)
    public Object realNameAuth(@RequestParam(value = "personal") MultipartFile personalFile, @RequestParam(value = "personalBack") MultipartFile personalBackFile, HttpServletRequest request){
        return authService.realNameAuth(personalFile, personalBackFile, request);
    }

    /**
     * 获取当前用户的实名认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_REALNAMEAUTHSTATUS)
    public Object realNameAuthStatus(HttpServletRequest request){
        return  authService.getRealNameAuthStatus(request);
    }

    /**
     * 账户认证
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_ACCOUNTAUTH)
    public Object accountAuth(@RequestParam(value = "card") MultipartFile cardFile, HttpServletRequest request){
        return authService.accountAuth(cardFile, request);
    }

    /**
     * 获取当前用户的账户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_ACCOUNTAUTHSATUS)
    public Object accountAuthStatus(HttpServletRequest request){
        return  authService.getAccountAuthStatus(request);
    }

    /**
     * 商户认证
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_MERCHANTAUTH)
    public Object merchantAuth(@RequestParam(value = "business") MultipartFile businessFile, HttpServletRequest request){
        return authService.merchantAuth(businessFile, request);
    }

    /**
     * 获取当前用户的商户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_MERCHANTAUTHSTATUS)
    public Object merchantAuthStatus(HttpServletRequest request){
        return  authService.getMerchantAuthStatus(request);
    }

    /**
     * 签名认证
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_SIGNATUREAUTH)
    public Object signatureAuth(@RequestParam(value = "signature") MultipartFile signatureFile, HttpServletRequest request){
        return authService.signatureAuth(signatureFile, request);
    }

    /**
     * 获取当前用户的商户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_SIGNATUREAUTHSTATUS)
    public Object signatureAuthStatus(HttpServletRequest request){
        return authService.getSignatureAuthStatus(request);
    }

    /**
     * 获取当前用户的四审认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_AUTHSTATUS)
    public Object authStatus(HttpServletRequest request){
        return authService.getAuthStatus(request);
    }


}
