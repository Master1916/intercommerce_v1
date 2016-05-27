/**
 * Apache LICENSE-2.0
 * Project name : mposp
 * Package name : org.mobile.mpos.action.user
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-13
 * Time : 下午4:33
 * 版权所有,侵权必究！
 */
package org.mobile.mpos.action.user;

import org.mobile.mpos.common.Mapping;
import org.mobile.mpos.service.user.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.action.user
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-13
 * Time : 下午4:33
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService = null;

    /**
     * 消息中心接口
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = Mapping.INTERFACE_URL_MESSAGE)
    public Object message(HttpServletRequest request) {
        return messageService.messageList(request);
    }


}


