package org.groovy.dao.secretKey

import org.groovy.common.Commons
import org.groovy.dao.Dao

/**
 * 密钥相关的数据库操作
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.dictionary
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/18
 * Time : 17:42
 * 版权所有,侵权必究！
 */
class SecretKeyDao extends Dao {

    SecretKeyDao(){
        super(Commons.getDAO());
    }

    def findICPublicRID() {
        db.rows("select * from ic_rid");
    }

    def findICPublicAID(){
        db.rows("select * from ic_aid");
    }

}
