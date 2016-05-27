package org.mobile.mpos.action.terminal;

import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.terminal.ActiveAndBindEquipService;
import org.mobile.mpos.service.terminal.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.action.terminal
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/23
 * Time : 17:34
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class TerminalController {

    @Autowired
    private ActiveAndBindEquipService activeAndBindEquipService = null;

    @Autowired
    private TerminalService terminalService = null;

    /**
     * 激活绑定设备
     *
     * @param request Http请求参数
     * @return
     * @author zhangshb
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_ACTIVEANDBINDEQUIP)
    public Object activeAndBindEquip(HttpServletRequest request) {
        return activeAndBindEquipService.activeAndBindEquip(request);
    }


    /**
     * 更换设备
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_SWIPER_CHANGE)
    public Object swiperChange(HttpServletRequest request) {
        return terminalService.swiperChange(request);
    }


    /**
     * ic公钥下载完成回调接口
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_DOWNLOADFINISHED)
    public Object downloadFinished(HttpServletRequest request) {
        return activeAndBindEquipService.downloadFinished(request);
    }

}
