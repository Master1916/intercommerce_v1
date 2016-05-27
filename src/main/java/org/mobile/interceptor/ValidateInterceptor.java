package org.mobile.interceptor;

import org.mobile.mpos.service.common.InterceptorService;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 项目拦截器
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.interceptor
 * Author : fate
 * User : fate
 * Date : 2015/11/18
 * Time : 10:53
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class ValidateInterceptor implements HandlerInterceptor {

    private InterceptorService interceptorService = null;

    public InterceptorService getInterceptorService() {
        return interceptorService;
    }

    public void setInterceptorService(InterceptorService interceptorService) {
        this.interceptorService = interceptorService;
    }

    /**
     * 拦截器：在访问controller之前需要处理的逻辑：
     * 1.校验mac是否有效
     * 2.校验接口rest模式的调用方法
     * 3.检查接口必传参数是否传递
     * 4.校验APP版本。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return getInterceptorService().preHandle(request, response, handler);
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
