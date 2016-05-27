package org.mobile.mpos.action.sale;

import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.trans.AllowAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.action.user
 * Date : 2015/11/23
 * Time : 17:33
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class TransMessageController {
    @Autowired
    private AllowAccountService queryService = null;

    /**
     * D0交易限额查询
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_TRANSD0AMOUNT)
    public Object AllowAccountD0(HttpServletRequest request) {
        return queryService.AllowAccountD0(request);
    }

    /**
     * T1交易限额
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_TRANST1AMOUNT)
    public Object AllowAccountT1(HttpServletRequest request) {
        return queryService.AllowAccountT1(request);
    }

}
