package org.mobile.mpos.service.user;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * To change this template use File | Settings | File and Code Templates.
 */
public interface CardManagerService {

    /**
     * 绑定/解绑用户银行卡
     *
     * @param request
     * @return
     */
    public Object bindBankCard(HttpServletRequest request);

    /**
     * 获取商户绑定银行卡列表
     *
     * @param request
     * @return
     */
    public Object listBandCard(HttpServletRequest request);

}
