package org.groovy.service.common

import net.paoding.analysis.analyzer.PaodingAnalyzer
import org.apache.commons.lang.StringUtils
import org.apache.lucene.analysis.Token
import org.apache.lucene.analysis.TokenStream
import org.cnepay.customer.exception.CustomerException
import org.cnepay.customer.request.MobileCustomerReq
import org.cnepay.customer.response.MobileCustomerInfoResp
import org.cnepay.customer.service.CustomerConst
import org.cnepay.customer.service.CustomerService
import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.dictionary.DictionaryDao
import org.groovy.dao.merchant.BankAccountDao
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.MobileDao
import org.groovy.dao.user.SessionDao
import org.groovy.util.AlgorithmUtil
import org.groovy.util.ResponseUtil
import org.groovy.util.ValidateUtil
import org.jpos.space.SpaceFactory
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.common.ManagerService
import org.mobile.mpos.service.common.SpringApplicationContext

import javax.servlet.ServletException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.sql.Timestamp

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/17
 * Time : 16:09
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
public class ManagerServiceImpl implements ManagerService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, MobileMessageImpl.getSimpleName())

    @Override
    Object resetPassword(HttpServletRequest request, HttpServletResponse response) {
        MerchantDao merchantDao = new MerchantDao();
        //处理请求
        def param = Commons.parseRequest(request)
        def user = Commons.initUserRequestParams(request)
        //旧密码
        if (!ValidateUtil.checkPassword(param.oldPassword as String)) {
            return Commons.fail(null, 'AFRESH_IDENTIFY', "原密码错误，请重新输入!")
        }
        String oldPassword = AlgorithmUtil.encodeBySha1((param.oldPassword as String) + ".woshua")
        //新密码
        if (!ValidateUtil.checkPassword(param.password as String)) {
            return Commons.fail(null, 'AFRESH_IDENTIFY', "新密码错误，请重新输入!")
        }
        String newPassword = AlgorithmUtil.encodeBySha1((param.password as String) + ".woshua")
        //获取收单商户登录信息
        def acqUser = merchantDao.findMerchantByMobileNo(user.user_id);
        try {
            CustomerService customerService = SpringApplicationContext.getBean("customerService")
            MobileCustomerReq mobileCustomerReq = new MobileCustomerReq();
            mobileCustomerReq.setMobile(user.user_id as String);
            mobileCustomerReq.setPassword(oldPassword);
            log.info '修改：校验密码：' + mobileCustomerReq.getMobile() + '===' + mobileCustomerReq.getPassword()
            //校验登录名密码
            customerService.loginByMobile(mobileCustomerReq)
            //更新用户密码
            mobileCustomerReq.setNewPassword(newPassword);
            log.info '修改：修改密码：' + mobileCustomerReq.getMobile() + '===' + mobileCustomerReq.getPassword() + '=========' + mobileCustomerReq.getNewPassword()
            customerService.resetPasswordByMobile(mobileCustomerReq)
            if (acqUser) {
                //校验4审
                def operator = merchantDao.findPersonByMobileNo(user.user_id);
                log.info("find operator=" + operator);
                if (!operator) {
                    return ResponseUtil.failResponse("USER_NOT_EXIST", "用户不存在");
                }
                log.info(oldPassword + " =pwd=" + operator.mobile_pwd)
                if (oldPassword != operator.mobile_pwd) {
                    return ResponseUtil.failResponse("ILLEGAL_USER_OR_PASSWORD", "原用户或密码错误");
                }
                changePassword(newPassword, user.user_id as String, acqUser.id as String, merchantDao)
            }
            return Commons.success(null, "修改成功")
        } catch (CustomerException e) {
            def message = e.code;
            log.info 'message :' + message
            if (CustomerConst.PROPERTY_PASSWORD_IS_NOT_CORRECT.equalsIgnoreCase(message)) {
                log.info "密码错误!!!"
                return ResponseUtil.failResponse(message, "原密码输入错误");
            }
            log.info "修改密码失败! " + e.code
            return ResponseUtil.failResponse("USER_NOT_EXIST", "修改密码失败");
        } catch (Exception e1) {
            e1.printStackTrace()
            return ResponseUtil.failResponse("SYSTEM_ERROR", "修改密码失败");
        }

    }

    @Override
    Object forgetPassword(HttpServletRequest request, HttpServletResponse response) {
        MerchantDao merchantDao = new MerchantDao();
        MobileDao mobileDao = new MobileDao();
        BankAccountDao bankAccountDao = new BankAccountDao();
        try {
            //处理请求
            def param = Commons.parseRequest(request)
            //上送验证码
            String mobileCode = param.idCode?.trim();
            //手机号
            def mobile = param.mobile?.trim()
            if (!ValidateUtil.checkMobileNo(mobile)) {
                return Commons.fail(null, 'ILLEGAL_ARGUMENT', '手机号有误')
            }
            //查询验证码
            def mobileIdCode = mobileDao.findeMobileIdentifyCodeByKsnNo(mobile);
            log.info "FORGET PASSWORD : " + (StringUtils.isBlank(mobileCode) && !StringUtils.isBlank(param.password as String))
            //验证码的发送
            if (mobileIdCode && !StringUtils.isBlank(mobileCode) && StringUtils.isBlank(param.password as String)) {
                //上送了校验码：验证码校验
                Cookie[] c = request.getCookies();
                String value = null;
                if (c) {
                    c.each { curr ->
                        if (curr.getName() == 'idCode') {
                            value = curr.getValue();
                            log.info 'COOKIE : ' + value
                        }
                    }
                }
                if (!value || value != mobileIdCode?.cookie_value) {
                    return Commons.fail(null, 'ILLEGAL_ARGUMENT', "无效的请求!")
                }
                if (mobileIdCode.validate_count > Constants.IDCODE_LIMIT_TIMES) {
                    return Commons.fail(null, 'SEND_MSG_FAILS', '今天验证次数过多,请明天重试!')
                }
                if (mobileCode != mobileIdCode.id_code) {
                    mobileIdCode.validate_count += 1
                    mobileDao.update(mobileIdCode);
                    return Commons.fail(null, 'AFRESH_IDENTIFY', "输入验证码错误!")
                }
                long now = new Date().getTime();
                long getCodeTime = mobileIdCode.date_created?.timestampValue().time;
                //验证码有效期为120秒
                if (now - getCodeTime > Constants.IDCODE_TIMEOUT_LIMIT) {
                    mobileIdCode.validate_count += 1
                    mobileDao.update(mobileIdCode);
                    return Commons.fail(null, 'AFRESH_IDENTIFY', "验证码超期,请重新获取验证码")
                }
                //校验是不是用户
                def isMerchant = false
                def acqUser = merchantDao.findMerchantByMobileNo(mobile)
                log.info("find acqUser=" + acqUser)
                if (acqUser) {
                    //获取银行卡
                    def bankAccount = bankAccountDao.findBankAccountByMerchantId(acqUser.id)
                    //校验4审
                    if (acqUser.is_certification == 1
                            && acqUser.is_signature == 1
                            && acqUser.review_status == 'accept'
                            && bankAccount?.is_verified == 1) {
                        //四审通过才是VCPOS商户
                        isMerchant = true;
                    }
                } else {
                    try {
                        CustomerService customerService = SpringApplicationContext.getBean("customerService")
                        MobileCustomerReq mobileCustomerReq = new MobileCustomerReq();
                        mobileCustomerReq.setMobile(mobile);
                        customerService.findCustomerInfoByMobile(mobileCustomerReq)
                    } catch (CustomerException e) {
                        log.info " 不存在!" + e.code
                        return ResponseUtil.failResponse("USER_NOT_EXIST", "用户不存在");
                    } catch (Exception e1) {
                        log.info "系统错误!" + e1.getMessage()
                        return ResponseUtil.failResponse("SYSTEM_ERROR", "校验失败");
                    }
                }
                //重新设置cookie
                mobileIdCode.cookie_value = new Date().format(Constants.DATE_FORMAT);
                mobileIdCode.validate_status = 0;
                merchantDao.update(mobileIdCode);
                return Commons.success([reqNo: param.reqNo ?: null, isMerchant: isMerchant], '验证成功')
            } else if (mobileIdCode && !StringUtils.isBlank(mobileCode) && !StringUtils.isBlank(param.password as String)) {
                if (mobileIdCode.validate_status != 0) {
                    return Commons.fail(null, 'AFRESH_IDENTIFY', "验证码无效，请重新获取验证码!")
                }
                if (!ValidateUtil.checkPassword(param.password as String)) {
                    return Commons.fail(null, 'AFRESH_IDENTIFY', "密码格式错误，请重新输入!")
                }
                String newPassword = AlgorithmUtil.encodeBySha1((param.password as String) + ".woshua")
                //校验是不是用户
                def acqUser = merchantDao.findMerchantByMobileNo(mobile)
                log.info("find acqUser=" + acqUser)
                if (acqUser) {
                    //获取银行卡
                    def bankAccount = bankAccountDao.findBankAccountByMerchantId(acqUser.id)
                    //校验4审
                    if (acqUser.is_certification == 1
                            && acqUser.is_signature == 1
                            && acqUser.review_status == 'accept'
                            && bankAccount?.is_verified == 1) {
                        //校验身份证信息
                        def idNumber = param.idNumber
                        def idCard = merchantDao.findMobileAndIdNumber(mobile, idNumber);
                        if (!idCard) {
                            mobileIdCode.validate_status = 0;
                            merchantDao.update(mobileIdCode);
                            return Commons.fail(null, 'AFRESH_IDENTIFY', "身份证校验未通过，请重新认证!")
                        }
                    }
                    //更新密码
                    changePassword(newPassword, mobile, acqUser.id as String, merchantDao)
                }
                try {
                    CustomerService customerService = SpringApplicationContext.getBean("customerService")
                    MobileCustomerReq mobileCustomerReq = new MobileCustomerReq();
                    mobileCustomerReq.setMobile(mobile);
                    MobileCustomerInfoResp rep = customerService.findCustomerInfoByMobile(mobileCustomerReq)
                    mobileCustomerReq.setPassword(rep.getPassword())
                    mobileCustomerReq.setNewPassword(newPassword);
                    customerService.resetPasswordByMobile(mobileCustomerReq)
                } catch (CustomerException e) {
                    log.info " CustomerException!" + e.code
                    if (!CustomerConst.PROPERTY_MOBILE_IS_NOT_EXISTING.equalsIgnoreCase(e.code)) {
                        //如果用户不存在。则只用修改老商户
                        return  Commons.success(null, "修改成功")
//                        return ResponseUtil.failResponse(e.code.toUpperCase(), '校验失败');
                    }
                } catch (Exception e1) {
                    log.info "系统错误!" + e1.getMessage()
                    return ResponseUtil.failResponse("SYSTEM_ERROR", "校验失败");
                }
                //更新验证状态
                return Commons.success(null, "修改成功")
            }
        } catch (CustomerException e) {
            log "更新密钥错误 ：" + e.code
            return Commons.fail(null, 'AFRESH_IDENTIFY', "同步密钥异常!")
        } catch (Exception e1) {
            log.error '更新密钥错误-' + e1.getMessage()
            e1.printStackTrace()
            return Commons.fail(null, "SYSTEM_ERROR", "校验失败");
        }
        return Commons.fail(null, 'AFRESH_IDENTIFY', "非法请求!")
    }

    void changePassword(String newPassword, String mobile, String merchantID, MerchantDao merchantDao) {
        String loginName = '%' + mobile;
        def operator = merchantDao.findMerchantOperator(loginName, merchantID);
        operator.last_change_pwd_time = new Timestamp(new Date().getTime());
        operator.mobile_pwd = newPassword
        merchantDao.update(operator);
    }


    @Override
    public Object bankQuery(HttpServletRequest request, HttpServletResponse response) {
        DictionaryDao dictionaryDao = new DictionaryDao();
        //处理请求
        def param = Commons.parseRequest(request)
        //获取关键字
        def keywords = param.keyWord?.split('\\s+')
        //关键字不能为空且数目不能超过5个
        if (!keywords) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '请输入关键字')
        }
        if (keywords.size() > 5) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '输入的关键字个数过多')
        }
        //分页大小
        def max = 20
        //分页页码
        def p = 1
        try {
            max = Integer.parseInt(param.max ?: '20')
            if (max > 50) max = 50
            if (max < 5) max = 5
        } catch (ignore) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', 'max param error')
        }
        try {
            p = Integer.parseInt(param.p ?: '1')
            if (p < 1) p = 1
        } catch (ignore) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', 'page param error')
        }
        //读取缓存，查看是否有本次查询记录
        def space = SpaceFactory.getSpace()
        //构建KEY
        def cache_key = AlgorithmUtil.encodeBySha1('bankQuery:' + request.parameterMap.toString())
        def cache = space.rdp(cache_key)
        if (cache) {
            log.info "load bank query result from cache, key '$cache_key'"
            return Commons.success(cache);
        }

        StringBuilder keySelectBuilder = new StringBuilder();
        keywords.each {
            if (it in ['招商', '招行']) it = '招商银行'
            if (it in ['建行', '建设']) it = '建设银行'
            if (it in ['中行']) it = '中国银行'
            if (it in ['建行']) it = '建设银行'
            if (it in ['农行']) it = '农业银行'
            if (it in ['工行']) it = '工商银行'
            if (it in ['交行']) it = '交通银行'
            keySelectBuilder.append(it + " ")
        }
        String keySelect = keySelectBuilder.toString();
        StringBuilder keyword = new StringBuilder();
        for (int index = 0; index < Constants.keyWords.size(); index++) {
            if (keySelect.contains(Constants.keyWords.get(index))) {
                keySelect = keySelect.replaceAll(Constants.keyWords.get(index), " ");
                keyword.append("/" + Constants.keyWords.get(index));
            }
        }
        log.info("keySelect=" + keySelect);
        StringBuilder sb = new StringBuilder();
        try {
            PaodingAnalyzer analyzer = new PaodingAnalyzer();
            TokenStream ts = analyzer.tokenStream("", new StringReader(keySelect));
            Token token;
            sb.setLength(0);
            while ((token = ts.next()) != null) {
                sb.append(token.termText()).append('/');
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        Set<String> keys = new HashSet<String>();
        if (StringUtils.isBlank(sb.toString().trim())) {
            if (!StringUtils.isBlank(keyword.toString())) {
                keys.addAll(Arrays.asList(keyword.toString().split('/')));
            } else {
                keys.add(keySelect)
            }
        } else {
            if (keyword.length() > 0) {
                sb.append(keyword);
            }
            keys.addAll(Arrays.asList(sb.toString().split('/')));
        }
        log.info("sb=" + sb.toString() + ",k=" + keyword.toString());
        StringBuilder sql = new StringBuilder();
        sql.append("select * from dict_cnaps_no t where ");
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String word = iterator.next();
            if (StringUtils.isBlank(word.trim())) {
                continue;
            }
            sql.append(" t.bank_name like '%");
            sql.append(word);
            sql.append("%' ");
            if (iterator.hasNext()) {
                sql.append(" and ")
            }
        }
        //起始行数目
        def offset = (p - 1) * max
        log.info("sql=" + sql.toString());
        def sqlwrap = "select * from (select page_.*, rownum rownum_ from (" + sql.toString() + ") page_ ) where rownum_ <= ${offset + max} and rownum_ > ${offset}"
        def countwrap = "select count(0) total from (" + sql + ")"
        def banks = []
        dictionaryDao.db.rows(sqlwrap)?.each {
            banks << [bankDeposit: it.bank_name, unionBankNo: it.cnaps_no]
        }
        def total = dictionaryDao.db.firstRow(countwrap)?.total as Long
        total = Math.ceil(total / max) as Long

        if (p > total && total != 0) {
            return Commons.fail(null, 'ILLEGAL_ARGUMENT', '没有更多的数据!');
        }
        def result = [
                total: total,
                tip  : total > 10 ? '结果较多, 建议使用精确关键字, 例如添加地名等' : null,
                reqNo: param.reqNo ?: null,
                banks: banks
        ]
        //将本次查询写入缓存，下次可以直接调用
        if (banks) {
            log.info "cache"
            space.out(cache_key, result, 3600000L)
        }
        return Commons.success(result)
    }

    @Override
    public Object bankList(HttpServletRequest request) {
        DictionaryDao dictionaryDao = new DictionaryDao();
        //处理参数
        def param = Commons.parseRequest(request)
        //获取接受的卡号
        def card = param.card
        log.info "card " + card
        //获取结算银行列表
        def D0BankCode = dictionaryDao.findDictBankList()
        log.info "CODE LIST " + D0BankCode
        //构建返回对象
        def result = [:];
        //判断上送的卡号是否存在，如果不存在则返回结算银行列表
        if (card) {
            //构建查询对象
            def bankCodeInfo
            //获取cardbin信息
            def cardBinMessage = dictionaryDao.findCardbin(card)
            //判断是否是已经存在的cardbin信息
            if (cardBinMessage) {
                log.info "cardBinMessage = " + cardBinMessage
                //cardbin是已知的cardbin，则判断该卡所在的银行是不是在结算银行列表中
                bankCodeInfo = Commons.cardBinRule(cardBinMessage.issuer_name, D0BankCode)
            } else {
                // 在cardbin列表找不到卡号。未知的银行卡
                log.info("未知的银行卡号: ${card}");
            }
            //不支持信用卡
            if (Constants.CARD_TYPE_CREDIT.equalsIgnoreCase(cardBinMessage?.card_type)) {
                log.info "不支持的信用卡 = " + cardBinMessage.issuer_name + " CARD:" + card
                return Commons.fail(null, 'CARD_TYPE_NOT_SUPPORT', "暂不支持信用卡，请更换")
            }
            //构建返回
            result.put("bankName", bankCodeInfo?.bankname)
            result.put("bankCode", bankCodeInfo?.bankcode)
            result.put("bankLogoIndex", bankCodeInfo?.banklogoindex)
            result.put("support", bankCodeInfo?.status == 1 ? true : false)
        } else {
            def resultList = []
            //把结算银行卡列表返回客户端
            D0BankCode.each {
                def codeMap = [:]
                codeMap.put("bankName", it?.bankname)
                codeMap.put("bankCode", it?.bankcode)
                codeMap.put("bankLogoIndex", it?.banklogoindex)
                codeMap.put("support", it?.status == 1 ? true : false)
                resultList.add(codeMap)
            }
            result.put("list", resultList)
        }
        return Commons.success(result, "成功")
    }

    /**
     * 下载图片
     *
     *
     * @param fileName
     * @param request
     * @param response
     * @return
     */
    @Override
    public File downloadImg(HttpServletRequest request, HttpServletResponse response) {
        def params = Commons.parseRequest(request);
        def fileName = params.fileName;
        def type = params.type;
        if (!fileName) {
            return null;
        }
        def conf = Commons.getConfig();
        def dir = type==null?conf.path.merchat:conf.path.banner;
        File file = new File(dir + Constants.FILE_SEPARATOR + fileName);
        try {
            log.info(file.getAbsolutePath());
            if (file == null || !file.exists()) {
                return null;
            }
            return file;
        } catch (FileNotFoundException e) {
            log.info("文件不存在");
        } catch (IOException e) {
            log.info("读写文件[${fileName}]失败");
        } catch (ServletException e) {
            log.info("服务器内部错误");
        }
        return null;
    }

    @Override
    Object logout(HttpServletRequest request) {
        def user = Commons.initUserRequestParams(request)
        SessionDao sessionDao = new SessionDao();
        sessionDao.deleteSessionByLoginName(user.user_id as String);
        return Commons.success(null, "退出成功");
    }
}
