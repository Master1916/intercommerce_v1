package org.groovy.util

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

import javax.servlet.http.HttpServletRequest
import java.util.regex.Pattern

/**
 * 校验工具�?
 *
 * Created with IntelliJ IDEA. 
 * Date: 2015/7/2
 * Time: 19:44 
 * To change this template use File | Settings | File Templates. 
 */
class ValidateUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, ValidateUtil.getSimpleName())

    /**
     * 校验设备最低支持版本
     * @param ver
     * @param product
     * @return
     */
    static checkVersion(def ver, def product) {
        def version = ver.mainVersion + "." + ver.subVersion
        def versionDouble = Double.parseDouble(version as String)
        if (product == Constants.ZFT) {
            if (versionDouble < 1.0) {
                return false
            }
            return true;
        }
        return true
    }

    /**
     *  校验手机号格式
     */
    static checkMobileNo(def mobile) {
        if (Pattern.compile('^[1][3-9][0-9]{9}\$').matcher(mobile as String).find()) {
            return true
        } else {
            return false
        }
    }

    /**
     *  校验密码格式
     */
    static checkPassword(def password) {
        //格式不限制。只限制长度
        //if (Pattern.compile('^[0-9A-Za-z]{6,16}\$').matcher(password as String).find()) {
        if (password?.length() <= 16 && password?.length() >= 6) {
            return true
        } else {
            return false
        }
    }

    /**
     *  校验接口必传参数
     */
    static checkInterfaceParams(String url, HttpServletRequest request) {
        def requestParams = Commons.parseRequest(request);
        if (!requestParams.containsKey('appVersion')) {
            return 'appVersion'
        }
        log.info("request params:${requestParams}")
        def required = Constants.INTERFACE_REQ_PARAM.get(url)
        def missParam = null
        required?.any {
            if (!(requestParams.containsKey(it))) {
                missParam = it;
            }
        }
        return missParam
    }

    /**
     * 解析平台版本信息
     *
     *
     * @param ver 平台版本信息
     * @return
     * @author zhangshb
     * @since 2015-11-17
     */
    static versionParse(String ver) {
        def v = ver.split(/\./)
        if (v.length < 5) return null
        [
                os           : v[0].toLowerCase(),
                model        : v[1].toUpperCase(),
                mainVersion  : Long.parseLong(v[2]),
                subVersion   : Long.parseLong(v[3]),
                subsubVersion: Long.parseLong(v[4]),
        ]
    }

    /**
     * 是否需要登陆
     * @param url
     * @return
     */
    static needLogin(String url) {
        if (url in Constants.NOT_NEED_LOGIN) {
            return false;
        }
        return true;
//        return false;
    }

    /**
     *  校验营业执照号格式
     *
     *
     * @param businessLicense 营业执照号
     * @return
     * @author zhangshb
     * @since 2015-12-4
     */
    static checkBusinessLicense(def businessLicense) {
        if (Pattern.compile('^{11}\$').matcher(businessLicense).find()) {
            return true
        } else {
            return false
        }
    }
}
