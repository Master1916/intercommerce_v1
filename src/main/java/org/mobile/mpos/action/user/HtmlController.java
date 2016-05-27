/**
 * Apache LICENSE-2.0
 * Project name : mposp
 * Package name : org.mobile.mpos.action.user
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-16
 * Time : 下午7:33
 * 版权所有,侵权必究！
 */
package org.mobile.mpos.action.user;

import org.mobile.mpos.action.MposController;
import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.user.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.action.user
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-16
 * Time : 下午7:33
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class HtmlController extends MposController {

    @Autowired
    private ViewService viewService = null;

    /**
     * 静态页面展示
     *
     * @param request
     * @return
     */
    @RequestMapping(Mapping.INTERFACE_URL_SHOWHTML)
    public ModelAndView showHtml(HttpServletRequest request) {
        String name = request.getParameter("html");
        log.info("request html:/html/" + name);
        ModelAndView html = new ModelAndView("/html/" + name);
        return html;
    }

    /**
     * 需要登录页面显示
     *
     * @param request
     * @return
     */
    @RequestMapping(Mapping.INTERFACE_URL_SHOWPROTOCOL)
    public ModelAndView showProtocol(HttpServletRequest request) {
        return viewService.showView(request);
    }

}
