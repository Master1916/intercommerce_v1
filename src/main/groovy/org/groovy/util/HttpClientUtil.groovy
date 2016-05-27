package org.groovy.util

import org.groovy.common.Constants
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

/**
 * Created by WKM on 2015/7/2.
 */
class HttpClientUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, HttpClientUtil.getSimpleName())

    static String doGet(String targetURL) {
        log.info("targetURL:" + targetURL);
        try {
            def conParam = [connectTimeout: 5 * 1000, readTimeout: 10 * 1000];
            def ret = new URL(targetURL).getText(conParam, Constants.CHARSET_UTF_8)
            return ret;
        } catch (Exception ex) {
            log.error("request GAODE:" + ex.getMessage(), ex);
        }
        return null;
    }

}
