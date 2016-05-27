/**
 * Apache LICENSE-2.0
 * Project name : mposp
 * Package name : org.mobile.mpos.service.common
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-11
 * Time : 下午4:01
 * 版权所有,侵权必究！
 */
package org.mobile.mpos.service.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.service.common
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-11
 * Time : 下午4:01
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
@Component
public class SpringApplicationContext implements ApplicationContextAware{

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringApplicationContext.applicationContext = applicationContext;
    }

    public static Object getBean(String beanName){
        return SpringApplicationContext.applicationContext.getBean(beanName);
    }
}
