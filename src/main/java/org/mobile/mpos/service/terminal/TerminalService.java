package org.mobile.mpos.service.terminal;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service
 * To change this template use File | Settings | File and Code Templates.
 */
public interface TerminalService {


    /**
     * 更换设备
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    public Object swiperChange(HttpServletRequest request);


}
