package org.groovy.service.common

import net.sf.json.JSONObject
import org.apache.commons.lang.StringUtils
import org.groovy.common.Constants
import org.groovy.dao.user.SessionDao
import org.groovy.util.DateUtil
import org.groovy.util.PosUtil
import org.groovy.util.ResponseUtil
import org.groovy.util.StringUtil
import org.groovy.util.ValidateUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.common.Mapping
import org.mobile.mpos.service.common.InterceptorService
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.sql.Timestamp
import java.text.ParseException

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service
 * Date : 15-11-14
 * Time : 下午7:40
 * To change this template use File | Settings | File and Code Templates.
 *
 */
@Service
public class InterceptorServiceImpl implements InterceptorService {

    private Log log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, InterceptorServiceImpl.getSimpleName())

    /**
     * 拦截器：在访问controller之前需要处理的逻辑：
     * 1.校验mac是否有效
     * 2.校验接口rest模式的调用方法
     * 3.检查接口必传参数是否传递
     * 4.校验APP版本。
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取交易路径
        String url = request.getRequestURI();
        //获取交易参数传递方法
        String methodName = request.getMethod();
        log.info("---------------- " + url + "[" + methodName + "]----------------");
        //请求响应设定编码集
        request.setCharacterEncoding(Constants.CHARSET_UTF_8);
        response.setCharacterEncoding(Constants.CHARSET_UTF_8);
        response.setHeader("Expires", "-1");
        response.setHeader("Pragma", "no-cache");
        //如果路径为空，则转换到响应首页
        if (StringUtils.isBlank(url) || StringUtils.length(url) < 2) {
            log.info("响应首页...")
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return false;
        }
        //打印请求参数
        printRequest(request)
        //时间校验
        def reqTime = request.getHeader("Date");
        if (reqTime) {
            try {
                //获取并且解析当前时间
                long time = DateUtil.GMTToDate(reqTime, Constants.GMT_TIME_PATTERN).getTime();
                //时间必须在10min内
                long currentTime = System.currentTimeMillis();
                if (Math.abs(currentTime - time) > Constants.INTERFACE_TIMEOUT_LIMIT) {
                    log.info("手机时间与服务器时间不一致:" + reqTime + ",server:" + currentTime + ",client" + time);
                    ResponseUtil.responseWithException("ILLEGAL_ARGUMENT", "手机时间与服务器时间不一致", response);
                    return false;
                }
            } catch (ParseException e) {
                //解析时间格式异常
                log.info("请求时间解析错误:" + e.getMessage());
                ResponseUtil.responseWithException("ILLEGAL_ARGUMENT", "请求时间格式不正确", response);
                return false;
            }
        } else if(!Mapping.INTERFACE_URL_ERROR.equalsIgnoreCase(url) && !"/downloadBanner.action".equalsIgnoreCase(url)){
            log.info("没有请求时间:Date=" + reqTime);
            ResponseUtil.responseWithException("ILLEGAL_ARGUMENT", "缺少请求参数Date", response);
            return false;
        }
        //获取接口名称
        url = url.substring(1, url.indexOf("."))
        //判断是否是post接口
        if (StringUtil.contains(Constants.REQUEST_METHOD_POST_LIST, url)) {
            //效验请求方法是否为POST
            if (!(Constants.REQUEST_METHOD_POST).equalsIgnoreCase(request.getMethod())) {
                log.info("接口(" + url + ")请求方式需为POST");
                ResponseUtil.responseWithException("REQUEST_METHOD_POST", "接口(" + url + ")请求方式需为POST", response);
                return false;
            }
            //判断是否是get接口
        } else if (StringUtil.contains(Constants.REQUEST_METHOD_GET_LIST, url)) {
            //效验请求方法是否为GET
            if (!(Constants.REQUEST_METHOD_GET).equalsIgnoreCase(request.getMethod())) {
                log.info("接口(" + url + ")请求方式需为GET");
                ResponseUtil.responseWithException("REQUEST_METHOD_GET", "接口(" + url + ")请求方式需为GET", response);
                return false;
            }
        } else {
            //未知接口类型
            log.info("未知接口" + url);
            ResponseUtil.responseWithException("UNKNOWN_URL", "未知接口(" + url + ")", response);
            return false;
        }
        //校验是否上送了必传参数
        String miss = (String) ValidateUtil.checkInterfaceParams(url, request);
        if (miss != null) {
            log.debug("miss required param: " + miss);
            ResponseUtil.responseWithException("ILLEGAL_ARGUMENT", "缺少参数" + miss, response);
            return false;
        }
        //解析上送版本信息
        def ver = ValidateUtil.versionParse(request.getParameter("appVersion"));
        if (ver != null && ver.size() <= 0 && !Mapping.INTERFACE_URL_ERROR.equalsIgnoreCase(url)) {
            log.debug("miss required param: " + request.getParameter("appVersion"));
            ResponseUtil.responseWithException("APP_VERSION_ERROR", "APP版本格式不匹配", response);
            return false;
        }
        String position = request.getParameter("position");
        if (position && position.length() > 70) {
            ResponseUtil.responseWithException("POSITION_ERROR", "定位数据非法", response);
            return false;
        }

        return true;
    }

    //打印请求参数
    private void printRequest(HttpServletRequest request) {
        JSONObject req = new JSONObject();
        Enumeration<String> headers = request.getHeaderNames();
        if (headers != null) {
            JSONObject head = new JSONObject();
            for (String headName : headers) {
                head.put(headName, request.getHeader(headName));
            }
            req.put("headers", head);
        }
        Map<String, String[]> pms = request.getParameterMap();
        if (pms != null) {
            JSONObject params = new JSONObject();
            for (Map.Entry<String, String[]> entry : pms.entrySet()) {
                StringBuilder values = new StringBuilder();
                values.append("【");
                if (entry.value != null) {
                    for (String v : entry.value) {
                        values.append(v + ",");
                    }
                    values.deleteCharAt(values.length() - 1);
                }
                values.append("】");
                params.put(entry.getKey(), values.toString());
            }
            req.put("params", params);
        }
        log.info(req.toString(4));
    }

    @Override
    public boolean validateUser(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String allUrl = request.getRequestURI();
        String url = allUrl.substring(1, allUrl.indexOf("."));
        log.info("request:" + allUrl + ", need login:" + ValidateUtil.needLogin(url))
        if (ValidateUtil.needLogin(url)) {
            String session = request.getHeader(Constants.WS_SESSION);
            if (!session) {
                response.setHeader(Constants.REMOVE_WSHSNO,"true");
                ResponseUtil.responseWithException("NEED_LOGIN", "请登录 后使用", response);
                return false;
            }
            String[] sessions = session.split("-");
            if (!sessions || sessions.length != 2) {
                response.setHeader(Constants.REMOVE_WSHSNO,"true");
                ResponseUtil.responseWithException("NEED_LOGIN", "数据有误,请重新登录", response);
            }
            SessionDao sd = new SessionDao();
            if (sd.countSessionById(sessions[0], sessions[1]) != 1) {
                sd.deleteSessionById(session);
                response.setHeader(Constants.REMOVE_WSHSNO,"true");
                ResponseUtil.responseWithException("NEED_LOGIN", "请重新登录 ", response);
                return false;
            }

            def userSession = sd.findSessionById(sessions[0], sessions[1]);
            if (!userSession) {
                response.setHeader(Constants.REMOVE_WSHSNO,"true");
                ResponseUtil.responseWithException("NEED_LOGIN", "请登录后使用", response);
                return false;
            }
            //超时判断
            if (!userSession.expiry_time) {
                response.setHeader(Constants.REMOVE_WSHSNO,"true");
                ResponseUtil.responseWithException("NEED_LOGIN", "已超时,请重新登录 ", response);
                return false;
            }
            long c = System.currentTimeMillis();
            if (c > userSession.expiry_time.timestampValue().getTime()) {
                sd.deleteSessionById(session);
                response.setHeader(Constants.REMOVE_WSHSNO,"true");
                ResponseUtil.responseWithException("NEED_LOGIN", "已超时,请重新登录 ", response);
                return false;
            }
            //延期SES
            userSession.expiry_time = new Timestamp(c + Constants.SESSION_DELAY_TIMES);
            sd.update(userSession);

            def pos = PosUtil.posRealNameAuthStatus(userSession.user_name);
            if(!pos.isEmpty()){
                response.setHeader("posStatus",String.valueOf(pos.status));
            }

            //用户
            request.setAttribute(Constants._INNER_USER_ID, userSession.user_name);
        }
        return true
    }
}