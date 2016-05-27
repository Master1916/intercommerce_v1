package org.mobile.mpos.action.manager;

import net.sf.json.JSONObject;
import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.common.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Date : 2015/11/23
 * Time : 17:34
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class ManagerController {

    //日志
    private Log log = new Log((Logger)NameRegistrar.getIfExists("logger.Q2"), ManagerController.class.getSimpleName());

    @Autowired
    private ManagerService managerService = null;

    /**
     * 重置密码
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_RESET_PASSWORD)
    public Object resetPassword(HttpServletRequest request, HttpServletResponse response) {
        return managerService.resetPassword(request, response);
    }

    /**
     * 忘记密码
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_FORGET_PASSWORD)
    public Object forgetPassword(HttpServletRequest request, HttpServletResponse response) {
        return managerService.forgetPassword(request, response);
    }

    /**
     * 联行号查询
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_BANK_QUERY)
    public Object bankQuery(HttpServletRequest request, HttpServletResponse response) {
        return managerService.bankQuery(request, response);
    }

    /**
     *获取18家银行列表
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_BANK_LIST)
    public Object bankList(HttpServletRequest request) {
        return managerService.bankList(request);
    }

    /**
     * 下载图片
     *
     *
     * @param request
     * @param response
     * @return
     * @author zhangshb
     * @since 2015-12-10
     */
    @RequestMapping(value = Mapping.INTERFACE_URL_DOWNLOADIMG)
    public void downloadImg(HttpServletRequest request, HttpServletResponse response) {
        FileInputStream fis = null;
        File file = managerService.downloadImg(request, response);
        try {
            if(file==null || !file.exists()){
                response.setContentType("application/json;charset=UTF-8");
                OutputStream out = response.getOutputStream();
                Map<String,Object> result = new HashMap<String,Object>();
                result.put("isSuccess",false);
                result.put("respTime",new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                result.put("respCode","FILE_NOT_EXIST");
                result.put("respMsg","图片不存在:" + request.getParameter("fileName"));
                log.info("file not exist,return :" + JSONObject.fromObject(result).toString(4));
                out.write(JSONObject.fromObject(result).toString().getBytes("UTF-8"));
                out.flush();
            } else {
                log.info("return image:" + file.getAbsolutePath());
                response.setContentType("image/jpeg");
                OutputStream out = response.getOutputStream();
                fis = new FileInputStream(file);
                byte[] b = new byte[fis.available()];
                fis.read(b);
                out.write(b);
                out.flush();
            }
        } catch (IOException e){
            log.error("exception:" + e.getMessage(), e);
        }finally {
            try {
                if(fis != null){
                    fis.close();
                }
            } catch (IOException e){
                log.error("exception:" + e.getMessage(), e);
            }
        }
    }

    /**
     * 退出登录
     *
     * @param request Http请求参数
     * @return
     * @since 2015-11-20
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_LOGOUT)
    public Object logout(HttpServletRequest request) {
        return managerService.logout(request);
    }


}
