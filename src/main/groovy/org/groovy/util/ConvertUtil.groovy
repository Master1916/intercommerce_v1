package org.groovy.util

import org.apache.commons.lang.StringUtils
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

import java.math.RoundingMode
import java.text.DecimalFormat


/**
 * 转换工具类
 *
 * Created with IntelliJ IDEA. 
 * User: zhangshb
 * Date: 2015/7/2 
 * Time: 19:44 
 * To change this template use File | Settings | File Templates. 
 */
class ConvertUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, ConvertUtil.getSimpleName())
    /**
     * map 转换为 Json
     * @param map
     * @return Json
     */
    static mapConvertToJson(def map){
        def json = new groovy.json.JsonBuilder()
        return json(map);
    }


    /**
     * map 转换为 String
     * @param map
     * @return String
     */
    static mapConvertToStr(def map){
       return mapConvertToJson(map).toString();
    }

    /**
     * Str 转换为 Map
     * @param str
     * @return Map
     */
    static strConvertToMap(def str){
        def jsonInput = new groovy.json.JsonSlurper();
        def map = [:];
        if(!str){
            return map;
        }
        return jsonInput.parseText(str);
    }

    /**
     * 解析流
     * @param str
     * @return Map
     */
    static read(InputStream inStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        inStream.close();
        return outputStream.toByteArray();
    }

    /**
     * Str 转换为 Map字段按照className的配置做特殊处理
     * @param str
     * @return Map
     */
    static strConvertToMap(def str, def className) {
        def jsonInput = new groovy.json.JsonSlurper();
        def map = convertValue(jsonInput.parseText(str), className);
        return map;
    }

    /**
     * String按照配置转换成制定类型
     * @return Map
     */
    static convertValue(Map data, String className) {
        //遍历数据
        data.each {
        //判断子键值对的值
            if (it.value instanceof List) {
                List valueList = (List) it.value
                //处理list类型
                convertValue(valueList, className)
            } else {
                //解析键值对
                dealWithEntry(it, data, className);
            }

        }
        return data;
    }

    static convertValue(List data, String className) {
        data.each {
            if (it instanceof List) {
            // 递归调用
                convertValue(it, className)
            } else {
                Map childMap = (Map) it
                //处理map类型
                convertValue(childMap, className)
            }
        }
    }

    /**
     * 解析键值对
     */
    static dealWithEntry(def entry, Map returnMap, String className) {
        // 判断键值对的值是否为空
        if (!(entry.value instanceof  String)){
            log.warn("字段值不是字符串 接口:" + className + ",key:" + entry.key + ",value:" + entry.value)
            entry.value = String.valueOf(entry.value)
        }
        def isBlank = StringUtils.isBlank(entry.value)
        //获取接口特定逻辑做处理方式
        def privateValue = TransferConstants.VALUE_TRANSFER_PRIVATE.get(className)?.get(entry.key)
        //判断是否按照接口特定逻辑做处理
        if (privateValue) {
            if (isBlank) {
                log.warn("私有处理不能转换 接口:" + className + ",key:" + entry.key + ",value:" + entry.value + "type:" + privateValue)
            } else {
                //执行接口特定逻辑做处理方式
                return returnMap.put(entry.key, transfer(entry.value, privateValue))
            }
        } else {
            //获取公共处理方式
            def publicValue = TransferConstants.VALUE_TRANSFER_PUBLIC.get(entry.key)
            //如果不用接口特殊处理方式，还需要判断是否需要公共处理
            if (publicValue) {
                //判断是否按照接口特定逻辑不需要处理
                if (!TransferConstants.VALUE_NOT_TRANSFER.get(className)?.contains(entry.key)) {
                    if (isBlank) {
                        log.warn("公共处理不能转换 接口:" + className + ",key:-" + entry.key + "- value:-" + entry.value + "- type:" + publicValue)
                    } else {
                        //按照公共处理方式处理
                        return returnMap.put(entry.key, transfer(entry.value, publicValue))
                    }
                }else{
                    log.info("私有处理不用转换 接口:" + className + ",key:-" + entry.key + "- value:-" + entry.value + "- type:" + publicValue)
                }
            }
        }
    }

    /**
     * 按照配置解析值
     */
    static transfer(String value, String type) {
        if (TransferConstants.INTEGER.equalsIgnoreCase(type)) {
            return parseInt(value)
        } else if (TransferConstants.BIGDECIMAL.equalsIgnoreCase(type)) {
            return parseBigDecimal(value)
        } else if (TransferConstants.BIGDECIMAL100.equalsIgnoreCase(type)) {
            return parseBigDecimalMultiply100(value)
        } else {
            return value;
        }
    }

    /**
     * String转换int
     * @param value
     * @return
     */
    static parseInt(String value) {
        return Integer.parseInt(value);
    }

    /**
     * String转换BigDecimal再乘以100 (金额处理:转换成分为单位)
     * @param str
     * @return
     */
    static parseBigDecimalMultiply100(String value) {
        return (new BigDecimal(value).multiply(new BigDecimal(100))).longValue();
    }

    /**
     * String转换BigDecimal(金额处理:转换成分为单位)
     * @param str
     * @return
     */
    static parseBigDecimal(String value) {
        return new BigDecimal(value).longValue();
    }

    /**
     * String转换BigDecimal(金额处理:转换成分为单位)
     * @param str
     * @return
     */
    static parseBigDecimalDivide100(String value) {
        return new BigDecimal(value).divide(new BigDecimal(100)).toString();
    }

    /**
     * 保留小数
     * @param val
     * @param reserveNum 保留几位小数
     * @return
     */
    static parseBigDecimalReserveNum(def val, def reserveNum){
        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(reserveNum);
        formater.setRoundingMode(RoundingMode.FLOOR);
        return  formater.format(new BigDecimal(val).doubleValue())
    }

    /**
     * 获取万份收益
     * @param val
     * @return
     */
    static long getMillionEarned(def val) {
        BigDecimal rate = new BigDecimal(val);
        BigDecimal num = rate.multiply(new BigDecimal(10000));
        BigDecimal earned = num.divide(new BigDecimal("365.2"),6,BigDecimal.ROUND_HALF_UP);
        earned = earned.setScale(0, BigDecimal.ROUND_HALF_UP);
        return earned.longValue();
    }
}
