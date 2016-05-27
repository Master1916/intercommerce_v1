package org.mobile.mpos.action.sale;

import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.trans.AcqCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.action.user
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/23
 * Time : 17:33
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class SaleController {
    @Autowired
    private AcqCommonService trasnsService = null;

    /**
     * 消费接口
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_SALE)
    public Object sale(@RequestParam(value = "signature") MultipartFile signature,HttpServletRequest request) {
        return trasnsService.sale(request,signature);
    }

    /**
     * IC回调接口
     *
     *
     * @param request
     * @return
     * @author zhangshb
     * @since 2015-12-10
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_TRANSNOTIFY)
    public Object transNotify(HttpServletRequest request) {
        return trasnsService.transNotify(request);
    }

    /**
     * 余额查询
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_QUERY)
    public Object query(HttpServletRequest request) {
        return trasnsService.query(request);
    }

}
