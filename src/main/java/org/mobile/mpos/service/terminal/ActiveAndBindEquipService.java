package org.mobile.mpos.service.terminal;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/23
 * Time : 19:40
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface ActiveAndBindEquipService {

    /**
     * 激活绑定设备服务类
     *
     *
     * @param request Http请求参数
     * @param request
     * @return
     * @author zhangshb
     * @since 2015-11-20
     */
    public Object activeAndBindEquip(HttpServletRequest request);

    /**
     * 激活绑定设备服务类
     *
     *
     * @param request Http请求参数
     * @param request
     * @return
     * @since 2015-11-20
     */
    public Object downloadFinished(HttpServletRequest request);



}
