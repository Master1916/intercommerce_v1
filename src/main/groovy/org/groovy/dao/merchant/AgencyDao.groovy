package org.groovy.dao.merchant

import org.groovy.common.Commons
import org.groovy.dao.Dao

/**
 * 服务商相关的数据库操作
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.merchant
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/24
 * Time : 16:27
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class AgencyDao extends Dao {

    AgencyDao() {
        super(Commons.getDAO());
    }

    /**
     * 通过id获取代理商信息
     *
     *
     * @param agencyId 代理商id
     * @return
     * @author zhangshb
     * @since 2015-11-24
     */
    def findAgencyById(agencyId) {
        if (!agencyId) return null
        wrap(db.firstRow("select * from agency where id=? and enabled=", [agencyId,1]), 'agency')
    }

    /**
     * 通过代理商id获取代理商信息
     *
     *
     * @param agencyId 代理商id
     * @return
     * @author zhangshb
     * @since 2015-12-4
     */
    def findAgencyTradeInfo(agencyId) {
        if (!agencyId) return null;
        db.firstRow("""
                select * from agency a left join agency_risk_setting ars
                on a.id=ars.agency_id and ars.settle_type='D' and ars.settle_period='0'
                where a.id=? and a.enabled=?
            """, [agencyId,1]);
    }

    /**
     * 查询代理设定信息
     *
     * @param agencyId
     * @return
     */
    def agencySecurityDeposit(agencyId){
        if(!agencyId) return null;
        return db.firstRow("""
                select * from mp_agency_security_deposit ms where ms.agency_id=?
        """,[agencyId]);
    }

    /**
     * 根据商户限额来周期设置
     * @param merchantId
     * @return
     */
    def findMerchantTradeSettleInfo(merchantId){
        return db.firstRow("""
                select * from cm_merchant cm
                  left join settle_setting ss on cm.settle_setting_id=ss.id
                where cm.id=${merchantId}
        """);
    }

    /**
     * 查询代理商列表
     * @param agencyId
     * @return
     */
    def listAgencySecurityDeposit(agencyId){
        if(!agencyId) return null;
        return db.rows("""
                select * from mp_agency_security_deposit ms where ms.share_agency_id=${agencyId}
        """);
    }
}
