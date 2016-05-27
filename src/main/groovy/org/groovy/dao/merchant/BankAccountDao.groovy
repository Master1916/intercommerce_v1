package org.groovy.dao.merchant

import org.groovy.common.Commons
import org.groovy.dao.Dao

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.merchant
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-30
 * Time : 下午7:15
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
class BankAccountDao extends Dao {

    BankAccountDao() {
        super(Commons.getDAO());
    }

    def findBankAccountByMerchantId(def merchantId) {
        wrap(db.firstRow("""select ba.* from bank_account ba
                left join merchant_bank_account mba on ba.id = mba.bank_account_id
                where mba.merchant_id=?""", [merchantId]), 'bank_account');
    }

    /**
     * 通过账户号获取商户银行账户信息
     *
     *
     * @param accountNo 账户号
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    def findBankAccountByAccountNo(accountNo) {
        db.firstRow("select * from mcr_ksn ksn left join merchant_terminal mo on ksn.terminal_id = mo.id" +
                " left join merchant_bank_account mba on mo.merchant_id=mba.merchant_id" +
                " left join bank_account ba on ba.id=mba.bank_account_id where  ba.account_no=? and ksn.is_activated='1' and ksn.is_used='1'", [accountNo])
    }

    /**
     * 变更账户信息
     *
     *
     * @param accountNo
     * @param accountName
     * @param bankName
     * @param unionBankNo
     * @param issuerName
     * @param id
     * @return
     */
    def updateBankAccount(def accountNo, def accountName, def bankName, def unionBankNo, def issuerName, id) {
        db.executeUpdate("update bank_account set account_no=?, account_name=?, bank_name=?, cnaps_no=?, issuer_bank_name=?, is_verified=3 where id=?", [accountNo, accountName, bankName, unionBankNo, issuerName, id])
    }

    /**
     * 获取掌付通支持的18家银行
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-12-5
     */
    def findDictBankList() {
        db.rows("select id, status, bank_name as bankname, bank_code as bankcode, bank_logo_index as banklogoindex from mp_dict_bank_list where status = 1 order by bank_Logo_Index ")
    }

    /**
     * 添加结算银行账号信息
     *
     *
     * @param id 主键
     * @author zhangshb
     * @since 2015-11-25
     */
    def addBankAccountInfo(id, accountNo = 'NULL', accountType = 'private', accountName = 'NULL', bankName = 'NULL', cnapsNo = '', issuerBankName = '', settleRate = 100, isVerified = 0, enabled = 0) {
        def bankAccountMap = [
                id              : id,
                account_no      : accountNo,
                account_type    : accountType,
                account_name    : accountName,
                bank_name       : bankName,
                cnaps_no        : cnapsNo,
                issuer_bank_name: issuerBankName,
                settle_rate     : settleRate,
                is_verified     : isVerified,
                enabled         : enabled
        ]
        db.dataSet('bank_account').add(bankAccountMap);
    }

    /**
     * 通过商户id获取商户账户关联信息
     *
     *
     * @param merchantId 商户id
     * @return
     * @author zhangshb
     * @since 2015-12-5
     */
    def findMerBankAccountByMerchentId(merchantId) {
        wrap(db.firstRow("""select * from merchant_bank_account where merchant_id=? """, [merchantId]), 'merchant_bank_account')
    }

    /**
     * 更新商户账户关联信息
     *
     *
     * @param mba 商户账户信息
     * @return
     * @author zhangshb
     * @since 2015-12-5
     */
    def updateMerchantBankAccount(def mba) {
        db.execute("update merchant_bank_account set bank_account_id=? ,merchant_id=?  where id=? ", [mba.bank_account_id, mba.merchant_id, mba.id])
    }

    /**
     * 判断商户的结算卡是否已经被注册
     *
     *
     * @param account_no 卡号
     * @param merchantIDD0 商户id
     * @return
     * @author zhangshb
     * @since 2015-12-6
     */
    def findD0BankAccountByAccountNo(def account_no, def merchantIDD0) {
        if (!account_no || !merchantIDD0) return null
        db.rows("select a.* from bank_account a join merchant_bank_account mb on mb.bank_account_id=a.id " +
                "where a.account_no = ? and mb.merchant_id in (select merchant_id_tzero from mp_merchant_day_zero where merchant_id_tzero <> ? ) ", [account_no, merchantIDD0])
    }
}
