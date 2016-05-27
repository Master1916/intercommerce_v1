/**
 * Apache LICENSE-2.0
 * Project name : mposp
 * Package name : org.mobile.interceptor
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-28
 * Time : 下午4:45
 * 版权所有,侵权必究！
 */
package org.mobile.interceptor;

import org.mobile.mpos.service.common.InterceptorService;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.interceptor
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-28
 * Time : 下午4:45
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class UserInterceptor implements HandlerInterceptor {

    private InterceptorService interceptorService = null;

    public InterceptorService getInterceptorService() {
        return interceptorService;
    }

    public void setInterceptorService(InterceptorService interceptorService) {
        this.interceptorService = interceptorService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return getInterceptorService().validateUser(request,response,handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
