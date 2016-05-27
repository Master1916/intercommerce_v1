package org.mobile.mpos.service.user;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

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
public interface ViewService {

    /**
     * 登录页面展示
     *
     * @param request
     * @return
     */
    public ModelAndView showView(HttpServletRequest request);

}
