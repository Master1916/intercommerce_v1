package org.groovy.common

import com.alibaba.druid.pool.DruidDataSource
import net.sf.json.JSONObject
import org.apache.commons.lang.StringUtils
import org.groovy.dao.dictionary.DictionaryDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.TerminalDao
import org.groovy.dao.user.SessionDao
import org.groovy.util.ConvertUtil
import org.groovy.util.MessageUtil

import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 公共类
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.common
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/16
 * Time : 17:51
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class Commons {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, Commons.getSimpleName())
    static final CONFIG_KEY = 'config:default'
    static final DATASOURCE_KEY = 'datasource:default'
    static final Map<String, Date> RESUBMIT_KEY = new ConcurrentHashMap<String, Date>();
    static def ds = NameRegistrar.getIfExists(DATASOURCE_KEY)

    //获取数据库操作对象
    static getDAO() {
        if (!ds) {
            def conf = getConfig()
            ds = new DruidDataSource()
            ds.setDriverClassName(conf.datasource.jdbc.driver)
            ds.setUrl(conf.datasource.jdbc.url)
            ds.setUsername(conf.datasource.jdbc.user)
            ds.setPassword(conf.datasource.jdbc.password)
            ds.setMinIdle(conf.datasource.pool.MinIdle)
            ds.setMaxActive(conf.datasource.pool.MaxActive)
            ds.setMaxWait(conf.datasource.pool.MaxWait)
            ds.setTestWhileIdle(conf.datasource.pool.TestWhileIdle)
            ds.setTimeBetweenEvictionRunsMillis(conf.datasource.pool.TimeBetweenEvictionRunsMillis)
            ds.setMinEvictableIdleTimeMillis(conf.datasource.pool.MinEvictableIdleTimeMillis)
            ds.setValidationQuery(conf.datasource.pool.ValidationQuery)

            //校验那个数据库连接没有释放资源
            ds.setRemoveAbandoned(true)
            ds.setRemoveAbandonedTimeout(60)
            ds.setLogAbandoned(true)
            NameRegistrar.register(DATASOURCE_KEY, ds)
        }
        return ds
    }

    //读取配置文件
    static getConfig() {
        def conf = NameRegistrar.getIfExists(CONFIG_KEY)
        if (!conf) {
            conf = new ConfigSlurper().parse(new File('cfg/Config.groovy').toURI().toURL())
            NameRegistrar.register(CONFIG_KEY, conf)
        }
        conf
    }

    static success(map, msg = '') {
        if (!msg) msg = Constants.error_code_mapping.SUCCESS
        ret map ?: [:], true, 'SUCCESS', msg
    }

    static success(map, msg = '', Cookie cookie) {
        if (!msg) msg = Constants.error_code_mapping.SUCCESS
        ret map ?: [:], true, 'SUCCESS', msg, cookie
    }

    static fail(map, code, msg = '') {
        ret map ?: [:], false, code, msg
    }

    static ret(map, isSuccess, code, msg) {
        if (!msg) {
            msg = Constants.error_code_mapping.get(code as String, "交易失败")
        }
        def resp = [
                respTime : new Date().format(Constants.DATE_FORMAT),
                isSuccess: isSuccess,
                respCode : code,
                respMsg  : msg?.toString() ?: null
        ] << map
        log.info "response: " + JSONObject.fromObject(resp).toString(4)
        ConvertUtil.mapConvertToJson(resp);
    }

    static ret(map, isSuccess, code, msg, Cookie cookie) {
        if (!msg) {
            msg = Constants.error_code_mapping.get(code as String, "交易失败")
        }
        def resp = [
                respTime : new Date().format(Constants.DATE_FORMAT),
                isSuccess: isSuccess,
                respCode : code,
                respMsg  : msg?.toString() ?: null,
                cookie   : cookie,
        ] << map

        log.info "response: $resp"
        ConvertUtil.mapConvertToJson(resp);
    }

    /**
     * 发送短信接口
     *
     *
     * @param mobileNo 手机号
     * @param content 发送内容
     * @return
     * @author zhangshb
     * @since 2015-11-19
     */
    static newSendMsg(def mobileNo, def content) {
        boolean bool = false;
        try {
            if(!Constants.IS_SEND_MESSAGE){
                bool = true;
            }else{
                def sendResult = new MessageUtil().sendMessage(mobileNo, content);
                if (!StringUtils.isBlank(sendResult) && sendResult.equalsIgnoreCase("0")) {
                    bool = true;
                }
            }
        } catch (e) {
            log.info("send message fail:${e.getMessage()}",e);
        }
        return bool;
    }

    /**
     * 通过KsnNo获取终端机型
     *
     *
     * @param ksnNo
     * @return
     * @author zhangshb
     * @since 2015-11-20
     */
    static getModelByKsnNo(def ksnNo) {
        TerminalDao terminalDao = new TerminalDao();
        return terminalDao.findKsnbin(ksnNo)?.product_model
    }

    /**
     * 判断是否需要上传照片
     *
     *
     * @param model
     * @return
     */
//    static isSignature(def model){
//        def signatureFlag = 3;
//        //联迪M35不上传个人签名照片
//        if(model == Constants.MPOS_LANDIM35 || model == Constants.ITRONI_MODEL || model == Constants.ITRON15_9_MODEL
//                || model == Constants.MPOS_HISENSE
//                || model == Constants.ITRONI21B_MODEL
//        ){
//            signatureFlag = 0;
//        }
//        return 0;
//    }

    static parseRequest(HttpServletRequest req) {
        def params = [:]
        req.getParameterMap().each { k, v ->
            if (v.size() > 1) {
                params[k] = v
            } else {
                params[k] = v[0]
            }
        }
        params
    }

    static initUserRequestParams(HttpServletRequest request) {
        def params = [:]
        Enumeration<String> names = request.getAttributeNames();
        for (String name : names) {
            if (name.startsWith(Constants._INNER)) {
                params << [(name.substring(Constants._INNER.length() + 1).toLowerCase()): request.getAttribute(name) as String]
            }
        }
        params
    }

    /**
     * 按照规则判断给定的银行名是否在制定的list中
     *
     * @param issuerName 给定的银行名
     * @param codeList List<MP_HDB_BANK_CODE>
     * @return
     */
    static def cardBinRule(String issuerName, def codeList) {
        log.info "issuerName " + issuerName
        Matcher m = Pattern.compile("[\u4e00-\u9fa5]+").matcher(issuerName);
        def bankName
        if (m.find()) {
            bankName = m.group(0);
            if (bankName.contains("浦东发展银行")) {
                bankName = "浦发银行"
            } else if (bankName.contains("邮政储蓄银行")) {
                bankName = "邮储银行"
            } else if (bankName.contains("厦门银行")) {
                bankName = "厦门银行"
            }

            log.info "bankName " + bankName

            def bankCodeInfo = contains(codeList, bankName)

            log.info "bankCodeInfo " + bankCodeInfo

            if (bankCodeInfo) {
                return bankCodeInfo;
            } else {
                m = Pattern.compile("[\u4e00-\u9fa5]+[银][行]").matcher(bankName);
                if (m.find()) {
                    bankName = bankName.replace("中国", "")
                    bankCodeInfo = contains(codeList, bankName)
                    if (bankCodeInfo)
                        return bankCodeInfo;
                }
            }
            return
        } else {
            log.info("银行issuerName格式错误: ${issuerName}");
            return
        }
    }

    /**
     * 复制文件
     *
     *
     * @param fileOld 源文件
     * @param fileNew 新文件
     * @return
     */
    public static def copyFile(File fileOld, File fileNew) {
        boolean bool = false;
        try {
            log.info("fileOld=${fileOld}");
            if(null != fileOld){
                if (fileOld.exists()) {
                    if (!fileNew.exists())
                        fileNew.createNewFile();
                    FileInputStream fis = new FileInputStream(fileOld);
                    FileOutputStream fos = new FileOutputStream(fileNew);
                    int read = 0;
                    while ((read = fis.read()) != -1) {
                        fos.write(read);
                        fos.flush();
                    }
                    fos.close();
                    fis.close();
                    bool = true;
                }else{
                    log.info("原文件不存在！！！")
                }
            }
        } catch (Exception e) {
            log.error("复制图片异常:${e.getMessage()}",e);
        }
        return bool;
    }

    /**
     * 判断list中是否存在给定的银行名
     * @param bankName 给定的银行名
     * @param codeLists
     * @return 签名
     */
    public static def contains(def codeLists, String bankName) {
        def result
        codeLists.each {
            if (bankName.equalsIgnoreCase(it.bankname as String) || bankName.contains (it.bankname as String)) {
                result = it
                return;
            }
        }
        result;
    }

    /**
     * 通过手机号获取当前用户的附属信息
     *
     *
     * @param mobileNo 手机号
     * @return
     * @author zhangshb
     * @since 2015-12-8
     */
    public static def findUserInfoByMobileNo(def mobileNo) {
        MerchantDao merchantDao = new MerchantDao();
        def personal = merchantDao.findCmPersonByMobileNo(mobileNo);
        if (!personal) {
            return null;
        }
        def realName = personal.name;
        if (null == realName || 'null'.equalsIgnoreCase(realName)) {
            realName = personal.real_name;
        }
        return [merchantId     : personal?.merchant_id,
                merOperatorId  : personal?.id,
                name           : realName,
                idNumber       : personal?.id_no,
                merchantNo     : personal?.merchant_no,
                merchantName   : personal?.merchant_name,
                regPlace       : personal?.business_place,
                loginName      : personal?.login_name,
                businessLicense: personal?.business_license_code,
                merchantReason : personal?.merchant_reason ?: '',
                realReason     : personal?.real_reason ?: '',
                signatureReason: personal?.signature_reason ?: '',
                accountReason  : personal?.account_reason ?: '',
                contractDate   : personal?.contract_date ?: '',
                T1AuthFirstPass: personal?.merchant_auth_pass_flag ,
        ];
    }

    //添加Session
    public static
    def addSession(HttpServletRequest request, HttpServletResponse response, String loginName, String position, String appVersion) {
        HttpSession hs = request.getSession();
        String ip = getRemoteAddress(request);
        SessionDao sessionDao = new SessionDao();
        sessionDao.deleteSessionByLoginName(loginName);
        String sid = hs.getId() ?: UUID.randomUUID().toString().replaceAll("-", "");
        def session = sessionDao.addSession(loginName, sid, position, ip, appVersion);
        if (!session) {
            return false
        }
        response.setHeader(Constants.WS_SESSION, sid + "-" + session.id);
        return true
    }

    private static String getRemoteAddress(HttpServletRequest request) {
        if (StringUtils.isBlank(request.getHeader("x-forwarded-for"))) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }

    public static def isDebitCard(String cardNo) {
        DictionaryDao dd = new DictionaryDao();
        def cardbin = dd.findCardbin(cardNo);
        if (cardbin) {
            return "debit".equalsIgnoreCase(cardbin.card_type as String);
        }
        return false;
    }

}
