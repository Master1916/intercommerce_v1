package org.mobile.mpos.service.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.org.mobile.mpos.service
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/17
 * Time : 15:50
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface MobileMessageService {

    /**
     * 获取验证码
     *
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-17
     */
    public Object sendMobileMessage(HttpServletRequest request, HttpServletResponse response);


    /**
     * 添加发送成功交易小票接口
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-17
     */
    public Object sendTransMessage(HttpServletRequest request, HttpServletResponse response);


}