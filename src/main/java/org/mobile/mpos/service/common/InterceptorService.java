package org.mobile.mpos.service.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service
 * Author : fate
 * User : fate
 * Date : 2015/11/24
 * Time : 17:57
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface InterceptorService {


    /**
     * 拦截器：在访问controller之前需要处理的逻辑：
     * 1.校验mac是否有效
     * 2.校验接口rest模式的调用方法
     * 3.检查接口必传参数是否传递
     * 4.校验APP版本。
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

    /**
     * 验证用户信息
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean validateUser(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
}
