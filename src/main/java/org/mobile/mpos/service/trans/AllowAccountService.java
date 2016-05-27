package org.mobile.mpos.service.trans;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service.trans
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-30
 * Time : 下午6:22
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface AllowAccountService {

    /**
     * D0当日最大交易限额
     *
     * @param request
     * @return
     * @author zhangshb
     * @since 2015-12-10
     */
    public Object AllowAccountD0(HttpServletRequest request);



    /**
     * T1交易限额
     *
     * @param request
     * @return
     */
    public Object AllowAccountT1(HttpServletRequest request);
}
