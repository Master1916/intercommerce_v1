package org.mobile.mpos.service.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.org.mobile.mpos.service
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-14
 * Time : 下午7:27
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface LoginService {

    /**
     * 登录接口
     *
     * @param request
     * @return
     */
    public Object login(HttpServletRequest request, HttpServletResponse response);

    /**
     * 保持心跳
     *
     * @return
     */
    public Object heartBeat();


}
