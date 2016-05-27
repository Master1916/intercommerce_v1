/**
 * Apache LICENSE-2.0
 * Project name : mposp
 * Package name : org.mobile.mpos.action.manager
 * Author : Wukunmeng
 * User : wkm
 * Date : 16-1-14
 * Time : 下午5:14
 * 版权所有,侵权必究！
 */
package org.mobile.mpos.action.manager;

import net.sf.json.JSONObject;
import org.mobile.mpos.action.MposController;
import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.common.BannerService;
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
 * Package name : org.mobile.mpos.action.manager
 * Author : Wukunmeng
 * User : wkm
 * Date : 16-1-14
 * Time : 下午5:14
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class BannerController extends MposController{

    @Autowired
    private BannerService bannerService = null;

    @Autowired
    private ManagerService managerService = null;

    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_BANNER)
    public Object logout(HttpServletRequest request) {
        return bannerService.listBanners(request);
    }

    @RequestMapping(value = Mapping.INTERFACE_URL_DOWNLOADBANNER)
    public void downloadBanner(HttpServletRequest request, HttpServletResponse response) {
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

}
