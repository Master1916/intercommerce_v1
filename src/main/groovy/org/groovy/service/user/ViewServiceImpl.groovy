package org.groovy.service.user

import org.groovy.common.Commons
import org.groovy.dao.merchant.MerchantDao
import org.groovy.util.DateUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.user.ViewService
import org.springframework.stereotype.Service
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service
 * Date : 15-11-14
 * Time : 下午7:40
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
@Service
public class ViewServiceImpl implements ViewService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, ViewServiceImpl.getSimpleName())

    @Override
    public ModelAndView showView(HttpServletRequest request) {
        //引用数据库相关操作
        MerchantDao merchantDao = new MerchantDao();
        //处理请求参数
        def param = Commons.parseRequest(request)
        def user = Commons.initUserRequestParams(request)
        // view名
        def name = param.view + '.jsp'
        log.info("request html:view/" + name);
        //查询商户信息
        def acqUser = Commons.findUserInfoByMobileNo(user.user_id)
        ModelAndView html = new ModelAndView("/view/" + name);
        if (acqUser) {
            html.addObject("realName", acqUser.name);
            def merchantFee = merchantDao.findMerchantFeeRate(acqUser.merchantId, 0);
            if (merchantFee) {
                if (merchantFee.rate_type == 11) {
                    html.addObject("rate", merchantFee.params_a.toString())
                } else if (merchantFee.rate_type == 13) {
                    html.addObject("rate", merchantFee.params_a + "~" + merchantFee.max_fee)
                }
            }
            def date = DateUtil.resolveTime(acqUser.contractDate)
            html.addObject("year", date.year);
            html.addObject("month", date.month);
            html.addObject("day", date.day);
            html.addObject("contractDay", date.year + "年" + date.month + "月" + date.day + "日");
        }


    }

}