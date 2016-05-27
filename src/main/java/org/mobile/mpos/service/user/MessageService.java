/**
 * Apache LICENSE-2.0
 * Project name : mposp
 * Package name : org.mobile.mpos.service.user
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-13
 * Time : 下午4:37
 * 版权所有,侵权必究！
 */
package org.mobile.mpos.service.user;

import javax.servlet.http.HttpServletRequest;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service.user
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-13
 * Time : 下午4:37
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public interface MessageService {

    /**
     * 消息列表
     * @param request
     * @return
     */
    public Object messageList(HttpServletRequest request);

}
