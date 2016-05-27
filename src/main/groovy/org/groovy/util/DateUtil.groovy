package org.groovy.util

import org.apache.commons.lang.StringUtils
import org.groovy.common.Constants
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * 时间工具类
 *
 * Created with IntelliJ IDEA. 
 * User: zhangshb
 * Date: 2015/7/2 
 * Time: 15:31 
 * To change this template use File | Settings | File Templates. 
 */
class DateUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, ConvertUtil.getSimpleName())

    /**
     * 获取当前时间 yyyy-MM-dd格式
     *
     *
     * @return
     * @author zhangshb
     * @since 2015-11-18
     */
    static def getYYMMDDCurrTime() {
        return format(new Date(), Constants.YMD_DATE_FORMAT);
    }

    static Date parse(String dateTime) {
        return parse(dateTime, Constants.DATE_FORMAT)
    }

    static Date parse(String dateTime, String format) {
        return new SimpleDateFormat(format).parse(dateTime);
    }

    static String format(Date dateTime, String format) {
        return new SimpleDateFormat(format).format(dateTime);
    }

    static String format(Date dateTime) {
        return format(dateTime, Constants.DATE_FORMAT)
    }

    static boolean exceedTime(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, 7);
        //当前时间
        Calendar now = Calendar.getInstance();
        return now.getTimeInMillis() > c.getTimeInMillis();
    }

    static String getYMD() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_YMD);
        Calendar c = Calendar.getInstance();
        return dateFormat.format(c.getTime());
    }

    /**
     * 格林威治时间字符串穿转换成标准格式 eg：
     *
     * @param date
     *            格林威治时间字符串格式 如：Tue 26 May 2015 12:00 GMT
     * @param gmtFormate
     *            格林威治时间标准化格式 如 E, dd MMM yyyy HH:mm
     * @return
     * @throws ParseException
     */
    static Date GMTToDate(String date, String gmtFormate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(gmtFormate, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.parse(date);
    }

    /**
     * 格林威治时间字符串穿转换成标准格式 eg：
     *
     * @param date
     *            格林威治时间字符串格式 如：Tue 26 May 2015 12:00 GMT
     * @param gmtFormate
     *            格林威治时间标准化格式 如 E, dd MMM yyyy HH:mm
     * @return
     * @throws ParseException
     */
    static String GMTToDate(String date, String gmtFormate, String formate) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(Constants.DATE_FORMAT_SPLIT);
        if (!StringUtils.isBlank(formate))
            sf.applyLocalizedPattern(formate);
        return sf.format(GMTToDate(date, gmtFormate));
    }

    static String DateToGMTString(java.util.Date date, String gmtFormate) {
        SimpleDateFormat sdf = new SimpleDateFormat(gmtFormate, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    static Map resolveTime(String date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new SimpleDateFormat(Constants.YMD_DATE_FORMAT).parse(date));
        return [
                year : cal.get(Calendar.YEAR),
                month: cal.get(Calendar.MONTH)+1,
                day  : cal.get(Calendar.DATE),
        ]
    }

    public static void main(String[] args) {

    }

}
