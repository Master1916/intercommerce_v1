package org.mobile.mpos.service.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.org.mobile.mpos.service
 * Date : 2015/11/17
 * Time : 15:50
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface ManagerService {

    /**
     * 重置密码
     *
     * @param request
     * @param response
     * @return
     */
    public Object resetPassword(HttpServletRequest request, HttpServletResponse response);


    /**
     * 忘记密码
     *
     * @param request
     * @param response
     * @return
     */
    public Object forgetPassword(HttpServletRequest request, HttpServletResponse response);

    /**
     * 联行号查询
     *
     * @param request
     * @param response
     * @return
     */
    public Object bankQuery(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取支持的18家结算银行
     *
     * @param request
     * @return
     */
    public Object bankList(HttpServletRequest request);

    /**
     * 下载图片
     *
     *
     * @param request
     * @return
     */
    public File downloadImg( HttpServletRequest request, HttpServletResponse response);

    /**
     * 签退
     * @param request
     * @return
     */
    public Object logout( HttpServletRequest request);

}