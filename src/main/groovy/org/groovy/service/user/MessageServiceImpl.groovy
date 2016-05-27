package org.groovy.service.user

import org.apache.commons.lang.BooleanUtils
import org.groovy.common.Commons
import org.groovy.util.MessageMobile
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.user.MessageService

import javax.servlet.http.HttpServletRequest

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service.user
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-13
 * Time : 下午4:39
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class MessageServiceImpl implements MessageService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, MessageServiceImpl.getSimpleName())

    @Override
    Object messageList(HttpServletRequest request) {
        //处理请求
        def params = Commons.parseRequest(request);
        def user = Commons.initUserRequestParams(request)
        //信息ID
        String messageId = params.messageId as String
        log.info "messageId : " + messageId
        String platform = params.appVersion?.split(/\./)[0]
        def data;
        if (!messageId) {
            log.info "platform : " + platform
            data = new MessageMobile().listMessage(user.user_id as String, platform, BooleanUtils.toBoolean(params.detail as String))
        } else {
            data = new MessageMobile().modifyMessage(user.user_id as String, messageId, platform)
            data << [messageId: messageId]
        }
        log.info "data : " + data
        if(data){
            if(BooleanUtils.toBoolean(data.isSuccess as String)){
                return Commons.success(data, '成功');
            }
        }
        return Commons.fail(null, 'MESSAGE_FAILED', '获取消息失败')

    }
}
