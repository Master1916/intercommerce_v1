package org.groovy.dao.terminal

import org.groovy.common.Commons
import org.groovy.dao.Dao

/**
 * 设备终端相关的数据库操作
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
class TerminalDao extends Dao {

    TerminalDao() {
        super(Commons.getDAO());
    }

    /**
     * 通过商户ID查找终端
     *
     * @param merchantId
     * @return
     */
    def findTerminalByMerchantId(merchantId) {
        if (!merchantId) return null
        wrap(db.firstRow("select * from merchant_terminal where enabled=? and merchant_id=?", [1, merchantId]), 'merchant_terminal')
    }

    /**
     * 通过商户ID查询ksn号
     *
     * @param merchantId
     * @return
     */
    def findKSNByMerchantId(merchantId) {
        if (!merchantId) return null
        db.firstRow("select k.* from mcr_ksn k left join merchant_terminal t on k.terminal_id=t.id where t.merchant_id=? and k.is_used=? and k.is_activated=?", [merchantId, 1, 1])
    }

    /**
     * 通过商户ID查询ksn号
     *
     * @param merchantId
     * @return
     */
    def findKsnInfoByMerchantId(merchantId) {
        if (!merchantId) return null
        wrap(db.firstRow("select k.* from mcr_ksn k left join merchant_terminal t on k.terminal_id=t.id where t.merchant_id=? and k.is_used=? and k.is_activated=?", [merchantId, 1, 1]), 'mcr_ksn')
    }

    /**
     * 根据ksnid找到该ksn对应的激活码
     *
     * @param ksnId
     * @return
     */
    def findLicenseByKSNId(ksnId) {
        if (!ksnId) return null
        wrap(db.firstRow("select * from mcr_serial_number where ksn_id=?", [ksnId]), 'mcr_serial_number')
    }

    /**
     * 通过KsnNo获取ksnBin信息
     *
     *
     * @param ksnNo
     * @return
     * @author zhangshb
     * @since 2015-11-20
     */
    def findKsnbin(ksnNo) {
        db.firstRow("""select * from dict_ksnbin dk left join DICT_TERMINAL_MODEL dtm on dtm.id=dk.terminal_model_id
        where dk.ksn_no_length=length(?) and dk.ksnbin_code=substr(?, 0, dk.ksnbin_length)
        and dk.accepted=1 order by dk.ksnbin_length desc""", [ksnNo, ksnNo])
    }

    /**
     * 通过KsnNo获取KSN信息
     *
     *
     * @param ksnNo
     * @return
     * @author zhangshb
     * @since 2015-11-23
     */
    def findKsnByKsnNo(ksnNo) {
        if (!ksnNo) return null
        if (ksnNo.length() > 16) {
            ksnNo = ksnNo[0..13]
        }
            wrap(db.firstRow("select * from mcr_ksn where ksn_no = ?", [ksnNo]), 'mcr_ksn')
    }

    /**
     * 通过激活码获取序列号信息
     *
     *
     * @param code 激活码
     * @return
     * @author zhangshb
     * @since 2015-11-23
     */
    def findLicenseByCode(code) {
        if (!code) return null
        wrap(db.firstRow("select * from mcr_serial_number where code=?", [code]), 'mcr_serial_number')
    }

    /**
     * 通过代理商信息获取产品信息
     *
     *
     * @param agencyId
     * @return
     * @author zhangshb
     * @since 2015-11-23
     */
    def findPoductByAgencyId(agencyId) {
        if (!agencyId) return null
        db.firstRow("""select dp.* from dict_product dp left join agency_dict_product adp on adp.dict_product_id=dp.id
            left join agency a on a.id=adp.agency_products_id where a.id = ?
    """, [agencyId])
    }

    /**
     * 添加激活码终端对应信息
     *
     *
     * @param id 主键
     * @param serNumber 激活码
     * @param terminalNo 终端号
     * @param createTime 创建时间
     * @author zhangshb
     * @since 2015-11-24
     */
    def addSerNumberTerminalInfo(id, serNumber, terminalNo, createTime) {
        def serNumberTerminalMap = [
                id           : id,
                serial_number: serNumber,
                terminal_no  : terminalNo,
                date_created : createTime,
                note         : 'WS'
        ]
        db.dataSet('serial_number_terminal').add(serNumberTerminalMap);
    }

    /**
     * 获取终端号
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-24
     */
    def getTerminalNo() {
        return (db.firstRow('select seq_terminal_no.nextval n from dual').n as String).padLeft(8, '0');
    }

    /**
     * 添加商户终端信息
     *
     *
     * @param id 主键
     * @param createTime 创建时间
     * @param ksnNo
     * @param merchantId 商户id
     * @param merchantNo 商户号
     * @param terminalNo 终端号
     * @param tModelId 对应的终端型号id
     * @param terminalName 终端名称
     * @author zhangshb
     * @since 2015-11-24
     */
    def addMerchantTerminalInfo(id, createTime, ksnNo, merchantId, merchantNo, terminalNo, tModelId, terminalName) {
        def merchantTerminalMap = [
                id                : id,
                batch_no          : 1,
                trace_no          : 1,
                current_cashier_no: '01',
                date_created      : createTime,
                last_updated      : createTime,
                enabled           : true,
                machine_sn        : ksnNo,
                merchant_id       : merchantId,
                merchant_no       : merchantNo,
                terminal_no       : terminalNo,
                terminal_model_id : tModelId,
                terminal_name     : terminalName != null ? terminalName.trim() : 'null'
        ]
        db.dataSet('merchant_terminal').add(merchantTerminalMap);
    }

    /**
     * 通过产品型号获取终端信息
     *
     *
     * @param model 产品型号
     * @return
     * @author zhangshb
     * @since 2015-11-24
     */
    def findTerminalModelByProductModel(model) {
        db.firstRow("""select * from dict_terminal_model t where t.product_model = ?
        """, [model])
    }

    /**
     * 通过商户ID查找终端(非监听)
     *
     * @param merchantId
     * @return
     * @author zhangshb
     * @since 2015-12-7
     */
    def findTerminalByMerId(merchantId) {
        if (!merchantId) return null
        db.firstRow("select * from merchant_terminal where enabled=? and merchant_id=?", [1, merchantId]);
    }

    /**
     * 通过设备ID获取终端信息
     * @param model 产品型号
     * @return
     * @since 2015-11-24
     */
    def findTerminalModelByID(id) {
        db.firstRow("""select * from dict_terminal_model t where t.id =?""", [id])
    }

    def updateICPublicStatus(String ksnNo) {
        db.execute("update merchant_terminal mt set mt.ic_status='00' where mt.id in (select mk.terminal_id from mcr_ksn mk where mk.ksn_no=?) ", [ksnNo]);
    }


    def updateICPublicStatus(String ksnNo,String status){
        db.execute("update merchant_terminal mt set mt.ic_status=${status} where mt.id in (select mk.terminal_id from mcr_ksn mk where mk.ksn_no=${ksnNo}) ");
    }

}