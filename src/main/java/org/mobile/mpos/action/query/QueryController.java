package org.mobile.mpos.action.query;

import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.trans.AcqCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Date : 2015/11/23
 * Time : 17:34
 */
@Controller
public class QueryController {
    @Autowired
    private AcqCommonService commonService = null;

    /**
     * 查询接口
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_QUERY_TRANS)
    public Object queryTrans(HttpServletRequest request) {
        return commonService.queryTrans(request);
    }

    /**
     * 查询交易状态
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_TRANS_STATUS)
    public Object transStatus(HttpServletRequest request) {
        return commonService.transStatus(request);
    }
}
