package org.mobile.mpos.service.merchant.auth;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * 商户基础四审相关服务接口入口
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service.merchant.auth
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/30
 * Time : 14:58
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface AuthService {

    /**
     * 实名认证
     *
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    public Object realNameAuth(MultipartFile personalFile, MultipartFile personalBackFile, HttpServletRequest request);

    /**
     * 获取当前用户的实名认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    public Object getRealNameAuthStatus(HttpServletRequest request);

    /**
     * 账户认证
     *
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    public Object accountAuth(MultipartFile cardFile, HttpServletRequest request);

    /**
     * 获取当前用户的账户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    public Object getAccountAuthStatus(HttpServletRequest request);

    /**
     * 商户认证
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    public Object merchantAuth(MultipartFile businessFile, HttpServletRequest request);

    /**
     * 获取当前用户的商户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    public Object getMerchantAuthStatus(HttpServletRequest request);

    /**
     * 签名认证
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    public Object signatureAuth(MultipartFile signatureFile, HttpServletRequest request);

    /**
     * 获取当前用户的商户认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    public Object getSignatureAuthStatus(HttpServletRequest request);

    /**
     * 获取当前用户的四审认证状态
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-30
     */
    public Object getAuthStatus(HttpServletRequest request);

}
