package org.groovy.dao.merchant

import org.groovy.common.Commons
import org.groovy.dao.Dao
import java.sql.Timestamp

/**
 * 商户相关数据操作
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.merchant
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/16
 * Time : 17:43
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */


class MerchantDao extends Dao {

    MerchantDao() {
        super(Commons.getDAO());
    }

    /**
     * 根据手机号查询登录名
     */
    def findLoginNameByMobileNo(mobileNo) {
        if (!mobileNo) return null
        def mobile = "%" + mobileNo
        def sql = """select o.login_name from cm_personal p left join merchant_operator o on p.id=o.merchant_id where p.mobile_no=?
        and o.login_name like ? and p.type_code !='01' and p.type_code is not null"""
        db.firstRow(sql, [mobileNo, mobile])
    }

    /**
     * 根据登录名查询用户/商户信息
     */
    def findOperatorByLoginName(loginName) {
        if (!loginName) return null
        wrap(db.firstRow("select *  from merchant_operator where login_name=?", [loginName?.toString()]), 'merchant_operator')
    }

    /**
     * 根据商户ID查询商户詳細信息
     */
    def findMerchantById(merchantId) {
        if (!merchantId) return null
        db.firstRow("select * from cm_personal c join cm_merchant m on c.id=m.id where m.id=?", [merchantId])
    }

    /**
     * 根据商户手机号查询商户詳細信息
     */
    def findMerchantByMobileNo(mobileNo) {
        if (!mobileNo) return null
        db.firstRow("select * from merchant_operator mo left join cm_merchant cm on mo.merchant_id=cm.id left join cm_personal cp on cp.id=cm.id where cp.mobile_no=? and cp.type_code !='01' and cp.type_code is not null", [mobileNo])
    }

    /**
     * 根据商户ID查询商户信息
     */
    def findCmMerchantById(id) {
        if (!id) return null
        wrap(db.firstRow("select * from cm_merchant m where m.id=?", [id]), 'cm_merchant')
    }

    /**
     * 根据商户ID获取
     * @param merchantId
     * @return
     */
    def findMerchantFinishedTradeZero(String merchantId) {
        if (!merchantId) return null;
        wrap(db.firstRow("""select * from mp_merchant_day_zero where merchant_id_tzero=? and agency_status=1 and merchant_status=1 """, [merchantId]), 'mp_merchant_day_zero')
    }

    /**
     * 根据商户ID获取
     * @param merchantId
     * @return
     */
    def findMerchantFinishedTradeZeroSec(String merchantId) {
        if (!merchantId) return null;
        wrap(db.firstRow("""select * from mp_merchant_day_zero where merchant_id_dzero_second=? and agency_status=1 and merchant_status=1""", [merchantId]), 'mp_merchant_day_zero')
    }


    /**
     * 根据手机号获取商户对应的操作人信息
     *
     *
     * @param mobileNo 手机号
     * @return
     * @author zhangshb
     * @since 2015-11-18
     */
    def findPersonByMobileNo(mobileNo) {
        if (!mobileNo) return null
        db.firstRow("select * from merchant_operator mo " +
                "left join cm_merchant cm on mo.merchant_id=cm.id left join cm_personal cp on cp.id=cm.id where cp.mobile_no=? and cp.type_code !='01' and cp.type_code is not null", [mobileNo])
    }

    /**
     * 根据商户ID查询商户是否开通了D0业务
     */
    def findMerchantTradeZero(merchantId) {
        if (!merchantId) return null;
        wrap(db.firstRow("""select * from mp_merchant_day_zero where merchant_id_tone=? and agency_status=1 and merchant_status=1 """, [merchantId]), 'mp_merchant_day_zero')
    }

    /**
     * 通过用户id更新用户的区域ID
     *
     * @param merchant
     * @return
     */
    def updateMerchantAreaIDByMerchID(def merchant) {
        if (!merchant) return null;
        db.executeUpdate("""update cm_merchant set AREA_ID=? where id=?""", [merchant.area_id as String, merchant.id as String]);
    }

    /**
     * 根据代理商ID查询代理商
     *
     * @param agencyId
     * @return
     */
    def findAgencyById(agencyId) {
        if (!agencyId) return null
        wrap(db.firstRow("select * from agency where id=? and enabled=?", [agencyId, 1]), 'agency')
    }

    /**
     * 根据ID查询商户详细信息
     *
     * @param id
     * @return
     */
    def findCmPersonalById(id) {
        if (!id) return null
        wrap(db.firstRow("select * from cm_personal p where p.id=?", [id]), 'cm_personal')
    }

    /**
     * 根据代理商ID查询代理商设置信息
     *
     * @param agencyId
     * @return
     */
    public findAgencyTradeInfo(agencyId) {
        if (!agencyId) return null;
        return db.firstRow("""
                select * from agency a left join agency_risk_setting ars on a.id=ars.agency_id
                where a.id=? and a.enabled=? and ars.settle_type='D' and ars.settle_period='0' and ars.security_amount is not null
            """, [agencyId, 1]);
    }

    /**
     * 根据商户的ID查询商户的费率
     * @param merchantId
     * @param feeType
     * @return
     */
    def findMerchantFeeRate(merchantId, feeType) {
        return db.firstRow("""
                select * from merchant_fee_rate mfr
                left join fee_rate_setting frs on mfr.fee_rate_setting_id=frs.id
                where mfr.merchant_id=?  and mfr.fee_type=?
        """, [merchantId, feeType]);
    }

    /**
     * 根据SessionID查询Session信息
     *
     * @param sessionNo
     * @return
     */
    def findWSSessionBySessionNo(sessionNo) {
        wrap(db.firstRow("select * from ws_session where session_no=?", [sessionNo]), 'ws_session')
    }

    /**
     * 新增商户信息
     *
     *
     * @param id 主键
     * @param merchantNo 商户号
     * @param settleSettingId 结算设置表Id
     * @param agencyId 代理商Id
     * @param createTime 创建时间
     * @param merchantName 商户名称
     * @author zhangshb
     * @since 2015-11-24
     */
    def addMerchantInfo(id, merchantNo, settleSettingId, agencyId, createTime, merchantName, int mccId, String mccType) {
//    def addMerchantInfo(id, merchantNo, settleSettingId, agencyId, createTime, merchantName) {
        def merchantMap = [
                id                   : id,
                agency_id            : agencyId,
                contract_date        : createTime,
                date_created         : createTime,
                enabled              : true,
                merchant_no          : merchantNo,
                settle_setting_id    : settleSettingId,
                merchant_type        : 'P',
                review_status        : 'init',
                fee_rate_status      : 0,       // 进件审核-费率状态
                qualification_status : 0,  // 进件审核-企业资质状态
                settle_account_status: 0, // 进件审核-结算帐户认证状态
                last_updated         : createTime,
                merchant_name        : merchantName != null ? merchantName.trim() : 'null',//若没有传名称，则存null字符串。
                mcc_id               : mccId,
                mcc_type             : mccType
        ]
        db.dataSet('cm_merchant').add(merchantMap);
    }

    /**
     * 新增商户个人信息
     *
     *
     * @param id 主键
     * @param mobileNo 手机号
     * @param myJob 职业
     * @param name 姓名
     * @param businessLicense 经营许可证号
     * @param businessPlace 经营地址
     * @param productId 产品信息Id
     * @param signatureFlag 是否需要签名
     * @param typeCode 终端类型分类编号
     * @author zhangshb
     * @since 2015-11-24
     */
    def addPersonalInfo(id, mobileNo, myJob, name, businessLicense, businessPlace, productId, signatureFlag, typeCode) {
        def personalMap = [
                id                   : id,
                id_no                : 'NULL',
                id_type              : 'id',
                is_certification     : false,
                mobile_no            : mobileNo,
                my_job               : myJob,
                name                 : name != null ? name.trim() : 'null',
                business_license_code: businessLicense != null ? businessLicense.trim() : 'null',
                business_place       : businessPlace != null ? businessPlace.trim() : 'null',
                mobile_product_id    : productId,
                is_signature         : signatureFlag,
                is_certification     : 0,
                type_code            : typeCode
        ]
        db.dataSet('cm_personal').add(personalMap);
    }

    /**
     * 获取商户号
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-24
     */
    def getMerchantNo() {
        return '500' + (db.firstRow('select seq_merchant_no.nextval n from dual').n as String).padLeft(12, '0');
    }

    /**
     * 更新商户表里的业务员字段
     *
     *
     * @param merchantId
     * @return
     * @author zhangshb
     * @since 2015-11-24
     */
    def updateSalesmanId(def merchantId) {
        if (!merchantId) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("update cm_merchant cm set cm.salesman_id = (");
        builder.append("select org.leader_id from sys_org org where org.id = (");
        builder.append("select cy.sys_org_id from agency cy where cy.id = (");
        builder.append("select c.agency_id from cm_merchant c where c.id = ${merchantId}");
        builder.append("))) where cm.id = ${merchantId}");
        db.execute(builder.toString());
        db.execute("update cm_merchant cm set cm.account_manager_id = cm.salesman_id where cm.id = ${merchantId}");
    }

    /**
     * 新增商户操作员信息
     *
     *
     * @param id 主键
     * @param creatTime 创建时间
     * @param loginName 登录名
     * @param mobilePwd 手机密码
     * @param loginPwd 登录密码
     * @param merchantId 商户id
     * @param name 姓名
     * @param roleId 角色id
     * @author zhangshb
     * @since 2015-11-24
     */
    def addMerOperatorInfo(long id, Timestamp creatTime, String loginName, String mobilePwd, String loginPwd, long merchantId, long roleId) {
        def merOperatorMap = [
                id              : id,
                last_login_time : creatTime,
                login_error_time: 0,
                login_name      : loginName,
                nick_name       : "null",
                real_name       : "null",
                mobile_pwd      : mobilePwd,
                login_pwd       : loginPwd,
                merchant_id     : merchantId,
                status          : 'normal',
                sys_role_id     : roleId ?: 201
        ]
        db.dataSet('merchant_operator').add(merOperatorMap);
    }

    /**
     * 通过mcc获取mccId
     *
     *
     * @param mcc
     * @return
     * @author zhangshb
     * @since 2015-11-24
     */
    def getMccIdByMcc(mcc) {
        return db.firstRow("select id from dict_mcc where mcc=?", [mcc]).id as Long;
    }

    /**
     * 新增商户银行账户信息
     *
     *
     * @param id 主键
     * @param bankAccountId 账户信息id
     * @param merchantId 商户id
     * @author zhangshb
     * @since 2015-11-25
     */
    def addMerBankAccountInfo(id, bankAccountId, merchantId) {
        def merBankAccountMap = [
                id             : id,
                bank_account_id: bankAccountId,
                merchant_id    : merchantId
        ]
        db.dataSet('merchant_bank_account').add(merBankAccountMap);
    }

    /**
     * 通过id获取商户操作人信息
     *
     *
     * @param id
     * @return
     * @author zhangshb
     * @since 2015-12-1
     */
    def findMerOperatorById(id) {
        return wrap(db.firstRow("select * from merchant_operator mo where mo.id=?", [id]), 'merchant_operator');
    }

    /**
     * 根据手机号查询商户和商户的登录信息
     * @param mobileNo
     * @return
     */
    def findMobileAndIdNumber(def mobileNo, def idNo) {
        if (!mobileNo && !idNo) return null
        db.firstRow("select * from merchant_operator mo left join cm_merchant cm on mo.merchant_id=cm.id left join cm_personal cp on cp.id=cm.id where cp.mobile_no=? and  cp.id_no=? ", [mobileNo, idNo])
    }
    /**
     * 根据登录名和商户ID查询登录信息
     *
     * @param loginName
     * @param merchantId
     * @return
     */
    def findMerchantOperator(String loginName, String merchantId) {
        if (!loginName) return null
        if (!merchantId) return null
        wrap(db.firstRow("""select * from merchant_operator where login_name like ? and merchant_id=? order by id desc""", [loginName, merchantId]), 'merchant_operator');
    }

    /**
     * 添加D0商户关联表信息
     *
     *
     * @param id 主键
     * @param t1MerchantId t1商户id
     * @param d0MerchantId d0商户id
     * @param d0IdCardImgName d0手持身份证照片名称
     * @return
     * @author zhangshb
     * @since 2015-12-4
     */
    def addMpMerchantDayZeroInfo(long id, long t1MerchantId, long d0MerchantId, String d0IdCardImgName, long d0SecondMerchantId) {
        def mpMerchantDayZeroMap = [
                id               : id,
                merchant_id_tone : t1MerchantId,
                merchant_id_tzero: d0MerchantId,
                agency_status    : 0,
                merchant_status  : 0,
                credential_image : d0IdCardImgName,
                is_verify        : 3,
                merchant_id_dzero_second : d0SecondMerchantId
        ]
        db.dataSet('mp_merchant_day_zero').add(mpMerchantDayZeroMap);
    }

    /**
     * 根据商户ID查询商户信息
     */
    def findCmMerchantByMerId(id) {
        if (!id) return null
        db.firstRow("select * from cm_merchant m where m.id=?", [id]);
    }

    /**
     * 通过id变更D0商户关联表的代理商审核状态
     *
     *
     * @param agencyStatus
     * @param id
     * @return
     * @author zhangshb
     * @since 2015-12-08
     */
    def updateMerDayZeroById(agencyStatus, id) {
        db.executeUpdate("update mp_merchant_day_zero set agency_status=? where id=?", [agencyStatus, id]);
    }

    /**
     * 通过手机号获取商户操作员信息
     *
     *
     * @param mobileNo 手机号
     * @author zhangshb
     * @since 2015-12-8
     */
    def findCmPersonalByMobileNo(def mobileNo) {
        db.firstRow("select * from cm_personal p where p.mobile_no=?", [mobileNo]);
    }

    /**
     * 通过商户id获取个人商户信息
     *
     *
     * @param merchantId 商户id
     * @author zhangshb
     * @since 2015-12-8
     */
    def findMerOperByMerchantId(def merchantId) {
        db.firstRow("select * from merchant_operator m where m.merchant_id=?", [merchantId]);
    }

    /**
     * 根据手机号获取商户对应的操作人信息
     *
     *
     * @param mobileNo 手机号
     * @return
     * @author zhangshb
     * @since 2015-12-10
     */
    def findCmPersonByMobileNo(mobileNo) {
        if (!mobileNo) return null
        db.firstRow("select mo.merchant_id, mo.id, mo.real_name, cp.id_no, cm.merchant_no, cm.merchant_name, cp.business_place,cp.merchant_auth_pass_flag ,cp.business_license_code, mo.login_name, cp.merchant_reason, cp.real_reason, cp.signature_reason, cp.account_reason, cp.name,to_char(cm.contract_date,'YYYY-MM-DD') contract_date from merchant_operator mo left join cm_merchant cm on mo.merchant_id=cm.id left join cm_personal cp on cp.id=cm.id where cp.mobile_no=? and cp.type_code !='01' and cp.type_code is not null", [mobileNo])
    }

    /**
     * 根据ID修改操作登录名
     *
     * @param loginName
     * @param id
     * @return
     */
    def updateLoginNameByOperatorId(loginName, id) {
        if (!id || !loginName) return null
        db.execute("update merchant_operator o set o.login_name=? where o.id=?", [loginName, id])
    }

    /**
     * 根据ID获取D0商户关联信息
     *
     *
     * @param id
     * @return
     * @author zhangshb
     * @since 20160309
     */
    def findMerchantTradeZeroById(id) {
        if (!id) return null;
        wrap(db.firstRow("""select * from mp_merchant_day_zero where id=?""", [id]), 'mp_merchant_day_zero')
    }

    /**
     * 根据商户ID查询商户是否开通了D0业务
     *
     * @param merchantId 商户id
     * @return
     * @author zhangshb
     * @since 20160316
     */
    def findD0MerchantByMerId(merchantId) {
        if (!merchantId) return null;
        wrap(db.firstRow("""select * from mp_merchant_day_zero where merchant_id_tone=? """, [merchantId]), 'mp_merchant_day_zero')
    }


}
