/**
 * Apache LICENSE-2.0
 * Project name : mposp
 * Package name : org.mobile.mpos.action
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-23
 * Time : 下午6:58
 * 版权所有,侵权必究！
 */
package org.mobile.mpos.action;

import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.springframework.stereotype.Controller;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.action
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-23
 * Time : 下午6:58
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Controller
public class MposController {

    //日志输出
    protected Log log = new Log((Logger)NameRegistrar.getIfExists("logger.Q2"), this.getClass().getSimpleName());

}
