package org.mobile.mpos.service.trans;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service.trans
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-30
 * Time : 下午6:22
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface AcqCommonService {

    /**
     * 消费接口
     * @param request
     * @param signature
     * @return
     */
    public Object sale(HttpServletRequest request,MultipartFile signature);

    /**
     * 余额查询
     * @param request
     * @return
     */
    public Object query(HttpServletRequest request);

    /**
     * 查询当前交易
     * @param request
     * @return
     */
    public Object queryCurrent(HttpServletRequest request);

    /**
     * 交易状态
     * @param request
     * @return
     */
    public Object transStatus(HttpServletRequest request);

    /**
     * 交易明细查询
     * 交易按日期查询
     * @param request
     * @return
     */
    public Object queryTrans(HttpServletRequest request);

    /**
     * IC回调
     *
     *
     * @param request
     * @return
     * @author zhangshb
     * @since 2015-12-10
     */
    public Object transNotify(HttpServletRequest request);
}
