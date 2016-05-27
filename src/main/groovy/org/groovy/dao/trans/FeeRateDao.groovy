package org.groovy.dao.trans

import org.groovy.common.Commons
import org.groovy.dao.Dao

/**
 * 费率相关的数据库操作
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.trans
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/24
 * Time : 14:01
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class FeeRateDao extends Dao {

    FeeRateDao() {
        super(Commons.getDAO());
    }

    /**
     * 添加商户结算设置信息
     *
     *
     * @param id 主键
     * @author zhangshb
     * @since 205-11-24
     */
    def addSettleSettingInfo(id, settleType = 'T', settlePeriod = 1, maxSettleAmount=50000.00, minSettleAmount=0.00) {
        def settleSettingMap = [
                id               : id,
                max_settle_amount: maxSettleAmount,
                min_settle_amount: minSettleAmount,
                settle_period    : settlePeriod,
                settle_flag      : 1,
                settle_type      : settleType
        ]
        db.dataSet('settle_setting').add(settleSettingMap);
    }

    /**
     * 添加手续费费率设置
     *
     *
     * @param id 主键
     * @param rateType 费率类型
     * @param maxFee 费率类型为封顶时的封顶手续费金额
     * @param minX 费率类型为封顶时的最小交易金额
     * @param paramsA y=ax+b公式中的参数a
     * @author zhangshb
     * @since 2015-11-24
     */
    def addFeeRateSettingInfo(id, rateType = 11, maxFee = 0.00, minX = 0.00, paramsA = 0.00) {
        def feeRateSettingMap = [
                id       : id,
                min_fee  : 0.00,
                min_x    : 0.00,
                params_b : 0.00,
                rate_type: rateType,
                max_fee  : maxFee,
                min_x    : minX,
                params_a : paramsA
        ]
        db.dataSet('fee_rate_setting').add(feeRateSettingMap);
    }

    /**
     * 添加商户手续费设置
     *
     *
     * @param id 主键
     * @param merchantId 商户id
     * @param feeRateSettingId 手续费费率设置id
     * @param feeType 费率类型
     * @author zhangshb
     * @since 2015-11-25
     */
    def addMerchantFeeRateInfo(id, merchantId, feeRateSettingId, feeType = 0) {
        def merchantFeeRateMap = [
                id                 : id,
                cardbin_type       : 0,
                merchant_id        : merchantId,
                fee_rate_setting_id: feeRateSettingId,
                fee_type           : feeType
        ]
        db.dataSet('merchant_fee_rate').add(merchantFeeRateMap);
    }

    /**
     * 通过商户id获取商户扣率信息
     *
     *
     * @param merchantId
     * @return
     * @author zhangshb
     * @since 2015-12-5
     */
    def findFeeRatesByMerchantId(merchantId) {
        db.firstRow("""select f.* from fee_rate_setting f left join merchant_fee_rate mf on mf.fee_rate_setting_id = f.id
           where mf.merchant_id = ? and mf.fee_type=0""", [merchantId]);
    }

    /**
     * 通过商户id获取单笔限额额度
     *
     *
     * @param merchantId
     * @return
     * @author zhangshb
     * @since 20160310
     */
    def findSettleSettingByMerId(merchantId){
        db.firstRow("""select ss.* from settle_setting ss left join cm_merchant cm on ss.id=cm.settle_setting_id where cm.id = ?""", [merchantId]);
    }
}