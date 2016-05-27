package org.mobile.mpos.service.common;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service.common
 * Author : Wukunmeng
 * User : wkm
 * Date : 16-1-14
 * Time : 下午5:15
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface BannerService {

    /**
     * 加载广告信息
     * @param request
     * @return
     */
    public Object listBanners(HttpServletRequest request);

}
