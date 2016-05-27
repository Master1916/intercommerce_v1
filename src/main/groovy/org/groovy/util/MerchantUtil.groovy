package org.groovy.util

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.util
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-30
 * Time : 下午7:04
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
class MerchantUtil {

    /**
     * 验证四审核是否通过
     * @param merchant
     * @param personal
     * @return
     */
    static boolean isValidate(merchant,personal,bankAccount){
        if(personal.is_certification == 1
                && personal.is_signature == 1
                && merchant.review_status == 'accept'
                && bankAccount.is_verified == 1){
            return true;
        }
        return false;
    }
}
