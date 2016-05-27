package org.groovy.dao.trans

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.Dao

import java.sql.Timestamp

/**
 * 费率相关的数据库操作
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.trans
 * Date : 2015/11/24
 * Time : 14:01
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class TransDao extends Dao {

    TransDao() {
        super(Commons.getDAO());
    }

    /**
     * 添加woshua交易流水
     * @param amount
     * @param batch
     * @param trace
     * @param card
     * @param version
     * @param currency
     * @param position
     * @param signture
     * @param terminal
     * @param ksnNo
     * @param cardBin
     * @return
     */
    def addWsTrans(amount, batch, trace, card, version, currency, position, signture, terminal, ksnNo, cardBin) {
        def now = new Date()
        def trans = [
                trans_type     : 'sale',
                amount         : amount,
                batch_no       : batch,
                trace_no       : trace,
                card_no        : card[0..5] + '*****' + card[-4..-1],
                card_no_wipe   : card[0..5] + '*****' + card[-4..-1],
                app_version    : version,
                currency       : currency ?: 'CNY',
                fee            : 0,//暂时不考虑费率，给默认值0
                position       : position ?: null,
                signature_image: signture ?  signture.toString() + ".png" : "",
                terminal_no    : terminal.terminal_no,
                merchant_no    : terminal.merchant_no,
                ksn_no         : ksnNo,
                cardbin_id     : cardBin?.id as Long,
                trans_date     : now.format(Constants.DATE_FORMAT_YMD),
                time_create    : new Timestamp(now.time),
                time_update    : new Timestamp(now.time),
                comp_status    : 0, // 0:'未完成',1:'接收中',2:'已完成'
                trans_status   : 0 // 0:'未成功',1:'成功',2:'已冲正',3:'已撤销',4:'已退款'
        ]
        trans.id = db.firstRow('select ws_trans_seq.nextval n from dual').n as Long
        db.dataSet('ws_trans').add(trans)
        return wrap(trans, 'ws_trans')
    }

    /**
     * 根据交易相关信息获取交易次数
     *
     *
     * @param batch
     * @param trace
     * @param terminalNo
     * @param merchantNo
     * @return
     * @author zhangshb
     * @since 2015-12-10
     */
    def findTransCount(def batch, def trace, def terminalNo, def merchantNo) {
        return db.firstRow("""
	        select count(id) c from ws_trans where batch_no=? and trace_no=? and terminal_no=?
	    and merchant_no=? and trunc(sysdate)=trunc(time_create)
                            """, [batch, trace, terminalNo, merchantNo]).c
    }

    /**
     * 根据交易信息查询交易
     *
     *
     * @param batch
     * @param origReqNo
     * @param origTransType
     * @param tranDate
     * @param terminalNo
     * @return
     * @author zhangshb
     * @since 2015-12-10
     */
    def findTranByTranInfo(def batch, def origReqNo, def origTransType, def tranDate, def terminalNo) {
        return db.firstRow("select * from ws_trans wt where wt.batch_no=? " +
                "and wt.trace_no=? and wt.trans_type=? and wt.trans_date=? " +
                "and terminal_no=?", [batch, origReqNo, origTransType, tranDate, terminalNo]);

    }

    /* 根据终端号
     *
     * @param terminalNo
     * @return
     */

    def findQueryCountByTerminalNo(terminalNo) {
        db.firstRow("""select nvl(count(id),0) count from trans_current where terminal_no=? and trans_type='query'
            and comp_status=2
      """, [terminalNo])
    }

    /**
     * 交易查询
     * @param batch
     * @param trace
     * @param terminalNo
     * @param merchantNo
     * @return
     */
    def queryTrans(String batch, String trace, String terminalNo, String merchantNo) {
        db.firstRow("""
                select count(id) c from ws_trans
                        where batch_no=? and trace_no=? and terminal_no=?
                            and merchant_no=? and trunc(sysdate)=trunc(time_create)""",
                [batch, trace, terminalNo, merchantNo]).c as Integer;
    }

    /**
     *
     * @param terminalNo
     * @return
     */
    def sumAmountDailyByTerminalNo(String terminalNo) {
        if (!terminalNo) return null
        def amount = db.firstRow("""
          select nvl(sum(t.amount),0) sum from trans_current t left join dict_cardbin c on c.id=t.cardbin_id where
        t.terminal_no = ? and t.trans_type='sale' and t.trans_status=1 and c.card_type!='debit'
        """, [terminalNo]).sum as Long
        return amount
    }

    /**
     *
     * @param terminalNo
     * @return
     */
    def sumAmountMonthByTerminalNo(String terminalNo) {
        if (!terminalNo) return null
        def amountCurrent = sumAmountDailyByTerminalNo(terminalNo);
        def amountHistory = db.firstRow("""
          select nvl(sum(t.amount),0) sum from trans_history t left join dict_cardbin c on c.id=t.cardbin_id where
            t.terminal_no = ?  and t.trans_type='sale' and t.trans_status=1 and c.card_type!='debit'
            and t.trans_date_time >= TRUNC(SYSDATE, 'MM')  and t.trans_date_time <= last_day(SYSDATE)
        """, [terminalNo]).sum as Long
        return amountCurrent + amountHistory

    }

    /**
     * 根据商户统计交易量
     * @param merchantId
     * @return
     */
    def findSumAmountDailyByMerchant(merchantId) {
        if (!merchantId) return null;
        def amount = db.firstRow("""
            select nvl(sum(t.amount),0) sum from trans_current t
                left join cm_merchant cm on t.merchant_no = cm.merchant_no
                left join settle_setting ss on cm.settle_setting_id=ss.id
            where ss.settle_type='D' and ss.settle_period=0
                and cm.id =${merchantId}
                and t.trans_type='sale' and t.trans_status=1
            group by cm.id
        """);
        if (!amount) {
            return 0;
        }
        return amount.sum;
    }

    /**
     * 根据代理商统计交易
     * @param agencyId
     * @return
     */
    def findSumAmountDailyByAgency(agencyId) {
        if (!agencyId) return null;
        def amount = db.firstRow("""
            select nvl(sum(t.amount),0) sum from trans_current t
                left join cm_merchant cm on t.merchant_no = cm.merchant_no
                left join settle_setting ss on cm.settle_setting_id=ss.id
            where ss.settle_type='D' and ss.settle_period=0
                and cm.agency_id =${agencyId}
                and t.trans_type='sale' and t.trans_status=1
            group by cm.agency_id
        """);
        if (!amount) {
            return 0;
        }
        return amount.sum;
    }

    /**
     * 根据代理商统计交易
     * @param agencys
     * @return
     */
    def findSumAmountDailyByAgencys(String agencys) {
        if (!agencys) return null;
        String sql = "select nvl(sum(t.amount),0) sum from trans_current t " +
                "                left join cm_merchant cm on t.merchant_no = cm.merchant_no " +
                "                left join settle_setting ss on cm.settle_setting_id=ss.id " +
                "            where ss.settle_type='D' and ss.settle_period=0 " +
                "                and cm.agency_id in (${agencys}) " +
                "                and t.trans_type='sale' and t.trans_status=1 ";
        Commons.log.info "sql: ${sql}";
        def amount = db.firstRow(sql);
        if (!amount) {
            return 0;
        }
        return amount.sum;
    }

    /**
     *
     * @param terminalNo
     * @param cardNo
     * @return
     */
    def findLastTransByTerminalNo(String terminalNo, String cardNo) {
        db.firstRow("""select * from trans_current t where t.terminal_no=? and t.card_no=?
        and t.trans_type='sale' and t.trans_status='1' order by id desc""", [terminalNo, cardNo])
    }

    /**
     *
     *
     * @param terminalNo
     * @param cardNo
     * @return
     */
    def findTrans(String batch, String trace, String terminalNo, long amount) {
        wrap(db.firstRow(""" select * from ws_trans
                            where batch_no=? and trace_no=? and terminal_no=? and amount = ?
                        """, [batch, trace, terminalNo, amount]), 'ws_trans')
    }

    /**
     *
     *
     * @param terminalNo
     * @param cardNo
     * @return
     */
    def findTransCurrent(String batch, String trace, String terminalNo, String merchantNo, long amount) {
        wrap(db.firstRow(""" select * from trans_current
                            where batch_no=? and trace_no=? and terminal_no=? and amount = ? and merchant_no=?
                        """, [batch, trace, terminalNo, amount, merchantNo]), 'ws_trans')
    }

    /**
     *
     * @param terminalNo
     * @param cardNo
     * @return
     */
    def findTrans(String referenceNo, String batch, String trace, String terminalNo, long amount) {
        wrap(db.firstRow(""" select * from trans_current
                            where  reference_no=? or (batch_no=? and trace_no=? and terminal_no=? and amount = ? and trans_type = 'sale')
                        """, [referenceNo, batch, trace, terminalNo, amount]), 'ws_trans')
    }

    /**
     * 按照代理商统计交易量
     * @param agencyId
     * @param cardNo
     * @return
     */
    def findSumAmountDailyByAgencyAndCard(String agencyId, String cardNo) {
        if (!agencyId) return null;
        def amount = db.firstRow("""
            select nvl(sum(t.amount),0) sum from trans_current t
                left join cm_merchant cm on t.merchant_no = cm.merchant_no
                left join settle_setting ss on cm.settle_setting_id=ss.id
            where ss.settle_type='D' and ss.settle_period=0
                and cm.agency_id =${agencyId} and t.card_no='${cardNo}'
                and t.trans_type='sale' and t.trans_status=1
            group by cm.agency_id
        """);
        if (!amount) {
            return 0;
        }
        return amount.sum;
    }

    /**
     * 计算商户当日D0交易额
     * @param merchantId
     * @return
     */
    def sumAmountD0DailyByMerchant(merchantId, Date date) {
        if (!merchantId) return null;
        def amount = db.firstRow("""
            select nvl(sum(t.amount),0) sum from trans_current t
                left join cm_merchant cm on t.merchant_no = cm.merchant_no
                left join settle_setting ss on cm.settle_setting_id=ss.id
            where ss.settle_type='D' and ss.settle_period=0
                and cm.id =? and t.date_created >= to_timestamp (?,?)
                and t.trans_type='sale' and t.trans_status=1
            group by cm.id
        """, [merchantId, date.format(Constants.DATE_FORMAT_YMD), Constants.DATE_FORMAT_YMD]);
        if (!amount) {
            return 0;
        }
        return amount.sum;
    }
    /**
     * 计算T1商户的结算金额
     * @param merchantId
     * @return
     */
    def sumAmountT1DailyByMerchant(merchantId, Date date) {
        if (!merchantId) return null;
        def amount = db.firstRow("""
            select nvl(sum(t.amount),0) sum from trans_current t
                left join cm_merchant cm on t.merchant_no = cm.merchant_no
                left join settle_setting ss on cm.settle_setting_id=ss.id
            where cm.id =? and t.date_created >= to_timestamp (?,?)
                and t.trans_type='sale' and t.trans_status=1
            group by cm.id
        """, [merchantId, date.format(Constants.DATE_FORMAT_YMD), Constants.DATE_FORMAT_YMD]);
        if (!amount) {
            return 0;
        }
        return amount.sum;
    }

    /**
     * 计算代理商当日D0交易额
     * @param merchantId
     * @return
     */
    def sumAmountDailyByAgency(agencyId) {
        if (!agencyId) return null;
        def amount = db.firstRow("""
            select nvl(sum(t.amount),0) sum from trans_current t
                left join cm_merchant cm on t.merchant_no = cm.merchant_no
                left join settle_setting ss on cm.settle_setting_id=ss.id
            where ss.settle_type='D' and ss.settle_period=0
                and cm.agency_id =?
                and t.trans_type='sale' and t.trans_status=1
            group by cm.agency_id
        """, [agencyId]);
        if (!amount) {
            return 0;
        }
        return amount.sum;
    }

    /**
     * 根据商户ID查询交易设置
     *
     * @param merchantId
     * @return
     */
    def findMerchantTradeSettleInfo(merchantId) {
        return db.firstRow("""
                select * from cm_merchant cm
                  left join settle_setting ss on cm.settle_setting_id=ss.id
                where cm.id=?
        """, [merchantId]);
    }
    /**
     * 根据代理商ID查询交易设置列表
     *
     * @param agencyId
     * @return
     */
    def listAgencySecurityDeposit(agencyId) {
        if (!agencyId) return null;
        return db.rows("""
                select * from mp_agency_security_deposit ms where ms.share_agency_id=${agencyId}
        """);
    }

    /**
     * 查询代理商所有当日D0交易
     *
     * @param agencys
     * @return
     */
    def sumAmountDailyByAgencys(String agencys) {
        if (!agencys) return null;
        String sql = "select nvl(sum(t.amount),0) sum from trans_current t " +
                "                left join cm_merchant cm on t.merchant_no = cm.merchant_no " +
                "                left join settle_setting ss on cm.settle_setting_id=ss.id " +
                "            where ss.settle_type='D' and ss.settle_period=0 " +
                "                and cm.agency_id in (" + agencys + ") " +
                "                and t.trans_type='sale' and t.trans_status=1 ";
        def amount = db.firstRow(sql);
        if (!amount) {
            return 0;
        }
        return amount.sum;
    }

    /**
     * 根据代理商以及卡号，统计交易量
     * @param agencyId
     * @param cardNo
     * @return
     */
    def findSumAmountDailyByAgencyAndCard(agencyId, String cardNo) {
        if (!agencyId) return null;
        def amount = db.firstRow("""
            select nvl(sum(t.amount),0) sum from trans_current t
                left join cm_merchant cm on t.merchant_no = cm.merchant_no
                left join settle_setting ss on cm.settle_setting_id=ss.id
            where ss.settle_type='D' and ss.settle_period=0
                and cm.agency_id =${agencyId} and t.card_no='${cardNo}'
                and t.trans_type='sale' and t.trans_status=1
            group by cm.agency_id
        """);
        if (!amount) {
            return 0;
        }
        return amount.sum;
    }

}
