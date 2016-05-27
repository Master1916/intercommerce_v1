package org.mobile.mpos.action.user;

import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.common.MobileMessageService;
import org.mobile.mpos.service.user.LoginService;
import org.mobile.mpos.service.user.RegisterService;
import org.mobile.mpos.service.user.SigninService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.action.user
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/23
 * Time : 17:33
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class UserController {

    @Autowired
    private LoginService login = null;

    @Autowired
    private RegisterService register = null;

    @Autowired
    private MobileMessageService mobileMessageService = null;

    @Autowired
    private SigninService signin = null;

    /**
     * 登录接口
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_LOGIN)
    public Object login(HttpServletRequest request, HttpServletResponse response) {
        return login.login(request, response);
    }

    /**
     * 保持心跳
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_HEART_BEAT)
    public Object heartBeat(HttpServletRequest request, HttpServletResponse response) {
        return login.heartBeat();
    }


    /**
     * 获取验证码接口
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-17
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_GETVERIFICATECODE)
    public Object getVerificateCode(HttpServletRequest request, HttpServletResponse response) {
        return mobileMessageService.sendMobileMessage(request, response);
    }


    /**
     * 添加发送成功交易小票接口
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-17
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_TRANS_MESSAGE)
    public Object sendTransMessage(HttpServletRequest request, HttpServletResponse response){
        return mobileMessageService.sendTransMessage(request, response);
    }

    /**
     * 注册
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_REGISTER)
    public Object register(HttpServletRequest request) {
        return register.register(request);
    }

    /**
     * 签到
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_SIGNIN)
    public Object signIn(HttpServletRequest request) {
        return signin.signIn(request);
    }

}
