package org.groovy.dao.dictionary

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.Dao
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

/**
 * 字典相关的数据库操作
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.dictionary
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/18
 * Time : 17:42
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class DictionaryDao extends Dao {
    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, DictionaryDao.getSimpleName())

    DictionaryDao() {
        super(Commons.getDAO());
    }

    /**
     * 根据地区名称查询区域字典表
     *
     * @param areaName
     * @return
     */
    def findAreaInfoByAreaName(def areaName) {
        if (!areaName) return null
        db.firstRow("SELECT * FROM DICT_AREA WHERE AREA_NAME = ?", [areaName])
    }

    /**
     * 根据代理商ID查询代理商产品信息
     *
     * @param agencyId
     * @return
     */
    def findPoductByAgencyId(agencyId) {
        if (!agencyId) return null
        db.firstRow("""select dp.* from dict_product dp left join agency_dict_product adp on adp.dict_product_id=dp.id
            left join agency a on a.id=adp.agency_products_id where a.id = ?
    """, [agencyId])
    }

    /**
     * 通过卡号获取卡并信息
     *
     *
     * @param cardno 卡号
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    def findCardbin(cardno) {
        db.firstRow("select * from dict_cardbin where card_no_length=length(?) and cardbin_code=substr(?, 0, cardbin_length) order by cardbin_length desc", [cardno, cardno])
    }

    /**
     * 通过发卡机构代码获取发卡机构
     *
     *
     * @param issuerNo 发卡机构代码
     * @return
     * @author zhangshb
     * @since 2015-12-2
     */
    def findIssuerByIssuerNo(issuerNo) {
        if (!issuerNo) return null
        wrap(db.firstRow("select * from dict_issuer i where i.issuer_no=?", [issuerNo]), 'dict_issuer')
    }

    /**
     * 获取掌付通支持的18家银行
     */
    def findDictBankList() {
        db.rows("select id, status, bank_name as bankname, bank_code as bankcode, bank_logo_index as banklogoindex from mp_dict_bank_list where status = 1 order by bank_Logo_Index ")
    }

    def addBankCard(mobile, bankCard, name, bankIndex, type, bankName) {
        def bindCardInfo = [
                id               : db.firstRow("select seq_mpmobilebindcard.nextval n from dual").n as Long,
                user_id          : mobile,
                bank_card        : bankCard,
                bank_name        : bankName,
                bank_index       : bankIndex,
                card_type        : type,
                bank_account_name: name,
        ]
        db.dataSet("mp_mobile_bind_card").add(bindCardInfo);
    }
    /**
     * 添加鉴权信息
     *
     * @param mobile
     * @param bankCard
     * @param name
     * @param bankCode
     * @param bankName
     * @param userId
     * @param idNo
     * @return
     */
    def addCertAuth(mobile, bankCard, name, bankCode, bankName, userId, idNo) {
        def id = db.firstRow("select seq_mpcertauth.nextval n from dual").n as Long
        def bindCardInfo = [
                id          : id,
                user_name   : userId,
                account_no  : bankCard,
                account_name: name,
                mobile      : mobile,
                iden_no     : idNo,
                bank_name   : bankName,
                bank_code   : bankCode,
                sn          : Constants.KEYWORD_MPOSP + id,
                status      : 0
        ]
        db.dataSet("mp_cert_auth").add(bindCardInfo);
        return wrap(bindCardInfo, "mp_cert_auth");
    }

    /**
     * 获取产品
     * @param name
     * @return
     */
    def findProductByName(String name) {
        db.firstRow("""
          select * from dict_product where product_no=?
          """, [name]);
    }

    /**
     * 获取交易参数
     * @param name
     * @return
     */
    def findTransParam(String name) {
        db.firstRow("""
          select * from mp_dict_setting where name=?
          """, [name]);
    }

    /**
     * 解绑银行卡
     *
     * @param userID
     * @param cardIDs 卡号，多张卡以逗号分隔
     * @return
     */
    def unbundlingBankCards(String userID, String cardIDs) {
        String sql = " update mp_mobile_bind_card set delete_flag = 1   where user_id = '" + userID + "' and id in (" + cardIDs + ") "
        db.executeUpdate(sql)
    }

    /**
     * 查询用户卡列表
     *
     * @param userID
     * @return
     */
    def listBankCards(String userID) {
        log.info 'userID ' + userID
        db.rows("""select bind.*,auth.status from mp_mobile_bind_card bind left join mp_cert_auth auth on auth.account_no=bind.bank_card where bind.user_id=? and bind.delete_flag=0 and auth.status=1 """, [userID]);
    }

    /**
     * 查询用户卡
     *
     * @param userID
     * @return
     */
    def findBankCard(String card) {
        db.firstRow("""select auth.* from  mp_cert_auth auth where auth.account_no=? and  auth.status=1 """, [card]);
    }

    /**
     * 根据用户ID和卡号查询绑定卡
     *
     * @param userID
     * @return
     */
    def findBankCardByUserIdAndCardNo(String userID, String card) {
        db.firstRow("""select bind.*,auth.status from mp_mobile_bind_card bind left join mp_cert_auth auth on auth.account_no=bind.bank_card where bind.user_id=? and bind.bank_card=? and bind.delete_flag=0 and auth.status=1 """, [userID, card]);
    }

    /**
     * 根据ID查询鉴权信息
     *
     * @param userID
     * @return
     */
    def findCertAuthById(String id) {
        wrap(db.firstRow("""
                    select * from mp_cert_auth
                    where id=? """, [id]), "mp_cert_auth")
    }

    /**
     * 根据ID查询鉴权信息
     *
     * @param userID
     * @return
     */
    def findCertAuthByCard(String card) {
        wrap(db.firstRow("""
                    select * from mp_cert_auth
                    where account_no=? """, [card]), "mp_cert_auth")
    }

    /**
     * 查看卡片的绑定信息
     *
     * @param userID
     * @return
     */
    def findBindCardByCardNo(String card) {
        wrap(db.firstRow("""
                    select * from mp_mobile_bind_card
                    where bank_card=? """, [card]), "mp_mobile_bind_card")
    }

    /**
     * 删除银行卡绑定信息
     * @param session
     * @return
     */
    def deleteBindCardByCardAndAccount(String card,String accountName) {
        db.execute("""delete from mp_mobile_bind_card where bank_card=? and bank_account_name=?""", [card,accountName]);
    }

}
