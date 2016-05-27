package org.mobile.mpos.service.merchant.tzauth;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service.merchant.tzauth
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/30
 * Time : 14:58
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface DZAuthService {

    /**
     * D0手持身份证半身照认证
     *
     * @param idCardFile 身份证图片
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-12-4
     */
    public Object handIdCardAuth(MultipartFile idCardFile, HttpServletRequest request);

    /**
     * 获取当前用户的手持身份证半身照认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-5
     */
    public Object getHandIdCardAuthStatus(HttpServletRequest request);

    /**
     * D0账户认证
     *
     * @param bankCardFile 银行卡图片
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-12-6
     */
    public Object dzAccountAuth(MultipartFile bankCardFile, HttpServletRequest request);

    /**
     * 获取当前用户的D0账户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-5
     */
    public Object getDZAccountAuthStatus(HttpServletRequest request);

    /**
     * 获取当前用户的D0认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-12
     */
    public Object getDZAuthStatus(HttpServletRequest request);

}
