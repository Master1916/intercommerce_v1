package org.mobile.mpos.service.user;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Date : 15-11-14
 * Time : 下午7:27
 * To change this template use File | Settings | File and Code Templates.
 */
public interface SigninService {

    /**
     * 签到
     *
     * @param request
     * @return
     */
    public Object signIn(HttpServletRequest request);

}
