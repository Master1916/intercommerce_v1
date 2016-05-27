package org.groovy.dao.terminal

import org.groovy.common.Commons
import org.groovy.dao.Dao

/**
 * 手机相关的数据库操作
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.terminal
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/18
 * Time : 17:39
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class MobileDao extends Dao {

    MobileDao() {
        super(Commons.getDAO());
    }

    /**
     * 通过手机号获取验证码信息
     *
     *
     * @param mobileNo 手机号
     * @return
     * @author zhangshb
     * @since 2015-11-18
     */
    def findMobileIdentifyCodeByMobileNo(def mobileNo) {
        if (!mobileNo) return null
        wrap(db.firstRow("""select * from ws_identify_code i where i.mobile_no=?""", [mobileNo]), 'ws_identify_code')
    }

    /**
     * 获取我刷验证码递增序列
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-19
     */
    def getWSIdentifyCode() {
        return db.firstRow("select SEQ_WSIDENTIFYCODE.nextval id from dual").id as Long
    }

    /**
     * 获取我刷验证码递增序列
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-19
     */
    def addTransQueryMeaasge(mobile, trade) {
        def merchantTerminalMap = [
                id       : db.firstRow('select seq_mptransquerybymobile.nextval n from dual').n as Long,
                mobile_no: mobile,
                trade_id : trade,
        ]
        db.dataSet('mp_transquery_by_mobile').add(merchantTerminalMap);
    }

    /**
     * 通过手机号清空验证码信息
     *
     *
     * @param mobileNo 手机号
     * @return
     */
    def deleteWSIdentifyCodeByMobileNo(def mobileNo) {
        db.executeUpdate("delete from ws_identify_code where mobile_no=?", [mobileNo])
    }

    /**
     * 查询交易短信发送记录
     * @return
     * @author zhangshb
     * @since 2015-11-19
     */
    def findTransQueryMessage(mobile, id) {
        if (!(mobile && id)) return null
        wrap(db.firstRow("""select * from mp_transquery_by_mobile i where i.mobile_no=? and trade_id=?""", [mobile, id]), 'mp_transquery_by_mobile')
    }

    /**
     * 根据KSN号查找验证码
     *
     * @param ksnNo
     * @return
     */
    def findeMobileIdentifyCodeByKsnNo(ksnNo) {
        if (!ksnNo) return null
        wrap(db.firstRow("""select * from ws_identify_code i where i.ksn_no=?""", [ksnNo]), 'ws_identify_code')
    }


}
