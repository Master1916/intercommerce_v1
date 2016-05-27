package org.groovy.util

import net.sf.json.JSONObject
import org.apache.commons.lang.StringUtils
import org.groovy.common.Commons
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse

/**
 * 校验工具类
 *
 * Created with IntelliJ IDEA.
 * Date: 2015/7/2
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 */
class ResponseUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, ResponseUtil.getSimpleName())

    //设定异常返回
    static void responseWithException(int statusCode, String code, String message, ServletResponse response) throws IOException {
        //构建返回错误信息json串
        JSONObject json = JSONObject.fromObject(fail(statusCode, code, message, null));
        log.info("response: " + json.toString(4));
        //设定返回定向
        HttpServletResponse res = ((HttpServletResponse) response);
        //设定返回格式以及编码机
        res.setContentType("application/json;charset=utf-8");
        //设定编码机
        res.setCharacterEncoding("UTF-8");
        //返回客户端
        byte[] out = json.getJSONObject("body").toString(4).getBytes("UTF-8");
        res.getOutputStream().write(out);
        res.flushBuffer();
    }

    //设定异常返回
    static void responseWithException(String code, String message, ServletResponse response) throws IOException {
        responseWithException(200, code, message, response);
    }

    /**
     * 返回失败信息
     *
     * @param statusCode 如果大于等于0，则按照HttpCode返回，否则通过后续返回
     * @param code 该code为body返回code，同时亦可确定httpCode
     * @param message 该信息为返回错误信息，如果未提供，则按照exception中的reason或者message返回，如果没有提供，
     *                   则按照code返回
     * @param data 该信息为返回数据，如果未提供则不显示
     * @return ResponseEntity
     */
    private static Object fail(int statusCode, String code, String message, Object data) {
        //判断message是否为空
        if (StringUtils.isBlank(message)) {
            // fallback the message
            message = "未知错误";
        }
        //返回的HTTP置不能小于0
        if (statusCode < 0) {
            // fallback the status code
            statusCode = 500;
        }
        //code信息不能为空
        if (StringUtils.isBlank(code)) {
            code = "SYSTEM_ERROR";
        }
        //构建返回的错误异常message
        Map<String, Object> response = new HashMap<String, Object>();
        //设定返回时间
        response.put("respTime", DateUtil.format(new Date()));
        //返回状态为false
        response.put("isSuccess", false);
        //返回码
        response.put("respCode", code);
        //返回错误信息
        response.put("respMsg", message);
        //如果有具体信息就加在返回参数中
        if (data != null) {
            response.put("data", data);
        }
        return new ResponseEntity<Map<String, Object>>(response,
                HttpStatus.valueOf(statusCode));
    }

    static Object failResponse(code,msg = ''){
        return Commons.fail([:],code,msg);
    }
}
