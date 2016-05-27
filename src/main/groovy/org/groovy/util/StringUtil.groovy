package org.groovy.util

import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

/**
 * 字符串工具类
 *
 * Created with IntelliJ IDEA. 
 * Date: 2015/7/2
 * Time: 15:31 
 * To change this template use File | Settings | File Templates. 
 */
class StringUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, ConvertUtil.getSimpleName())

    public static boolean contains(String[] array, String matcher) {
        if (array && matcher) {
            for (String content : array) {
                if (content.equalsIgnoreCase(matcher.trim())) {
                    return true
                }
            }
        }
        return false;
    }
}
