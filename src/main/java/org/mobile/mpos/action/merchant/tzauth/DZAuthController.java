package org.mobile.mpos.action.merchant.tzauth;

import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.merchant.tzauth.DZAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * D0二审
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.action.merchant.tzauth
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/30
 * Time : 14:53
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class DZAuthController {

    @Autowired
    private DZAuthService dzAuthService;

    /**
     * D0手持身份证半身照认证
     *
     * @param idCardFile 身份证图片
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_HANDIDCARDAUTH)
    public Object handIdCardAuth(@RequestParam(value = "idCard") MultipartFile idCardFile, HttpServletRequest request){
        return dzAuthService.handIdCardAuth(idCardFile, request);
    }

    /**
     * 获取当前用户的手持身份证半身照认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_HANDIDCARDAUTHSTATUS)
    public Object handIdCardAuthStatus(HttpServletRequest request){
        return  dzAuthService.getHandIdCardAuthStatus(request);
    }

    /**
     * D0账户认证
     *
     * @param bankCardFile 银行卡图片
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_DZACCOUNTAUTH)
    public Object dzAccountAuth(@RequestParam(value = "bankCard") MultipartFile bankCardFile, HttpServletRequest request){
        return dzAuthService.dzAccountAuth(bankCardFile, request);
    }

    /**
     * 获取当前用户的D0账户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_DZACCOUNTAUTHSTATUS)
    public Object dzAccountAuthStatus(HttpServletRequest request){
        return dzAuthService.getDZAccountAuthStatus(request);
    }

    /**
     * 获取当前用户的D0认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-12
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_DZAUTHSTATUS)
    public Object dzAuthStatus(HttpServletRequest request){
        return dzAuthService.getDZAuthStatus(request);
    }




}
