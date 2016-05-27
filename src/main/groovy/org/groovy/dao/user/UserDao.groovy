package org.groovy.dao.user

import org.groovy.common.Commons
import org.groovy.dao.Dao

/**
 * 用户相关的数据库操作
 *
 *
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.user
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/24
 * Time : 19:32
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */

class UserDao extends Dao {

    UserDao() {
        super(Commons.getDAO());
    }

    /**
     * 通过角色编号获取角色Id
     *
     *
     * @param roleCode
     * @return
     * @author zhangshb
     * @since 2015-11-24
     */
    def findRoleIdByRoleCode(roleCode) {
        return db.firstRow("select id from sys_role r where r.role_code=?", [roleCode])?.id as Long
    }

}
