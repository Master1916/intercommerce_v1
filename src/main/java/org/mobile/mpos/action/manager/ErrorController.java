/**
 * Apache LICENSE-2.0
 * Project name : mposp
 * Package name : org.mobile.mpos.action.manager
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-23
 * Time : 下午5:18
 * 版权所有,侵权必究！
 */
package org.mobile.mpos.action.manager;

import net.sf.json.JSONObject;
import org.mobile.mpos.action.MposController;
import org.mobile.mpos.common.Mapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
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
 * Date : 15-12-23
 * Time : 下午5:18
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class ErrorController extends MposController{
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_ERROR)
    public Object logout(HttpServletRequest request) {
        String code = request.getParameter("code");
        log.info("无法处理你的请求:ERROR-CODE:" + code);
        Map<String,Object> result = new HashMap<String,Object>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        result.put("isSuccess",false);
        result.put("respCode",code);
        result.put("respMsg","无法处理你的请求:" + code);
        result.put("respTime",format.format(new Date()));
        log.info("response:" + JSONObject.fromObject(result).toString(4));
        return result;
    }
}
