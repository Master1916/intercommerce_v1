package org.groovy.dao.user

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.Dao

import java.sql.Timestamp


/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.user
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-28
 * Time : 下午5:24
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class SessionDao extends Dao {

    SessionDao() {
        super(Commons.getDAO());
    }

    //获取session
    def findSessionById(String session,String id) {
        if (!session) return null;
        wrap(db.firstRow("""select * from ws4_session where session_no=? and id=?""", [session,id]), 'ws4_session');
    }

    //获取session
    def findSessionByNoAndUserId(String session, def name) {
        if (!session) return null;
        wrap(db.firstRow("""select * from ws4_session where session_no=? and user_name=?""", [session, name]), 'ws4_session');
    }

    /**
     * 统计session 数量
     * @param session
     * @return
     */
    def countSessionById(String session,String id) {
        db.firstRow("""select count(id) c from ws4_session where session_no=? and id=?""", [session,id]).c as long;
    }

    /**
     * 删除session
     * @param session
     * @return
     */
    def deleteSessionById(String session) {
        db.execute("""delete from ws4_session where session_no=?""", [session]);
    }

    /**
     * 删除原有session
     * @param loginName
     * @return
     */
    def deleteSessionByLoginName(String loginName) {
        db.execute("""delete from ws4_session where user_name=?""", [loginName]);
    }

    /**
     * 增加session 记录
     * @param userName 用户名
     * @param sessionNo session唯一标识
     * @param position 经纬度
     * @param ip 客户端ID
     * @param appVersion 客户的版本
     * @return
     */
    def addSession(String userName, String sessionNo, String position, String ip, String appVersion) {
        def now = new Timestamp(new Date().time);
        def session = [
                id         : getSeqNextval("seq_ws4session") as long,
                user_name  : userName,
                session_no : sessionNo,
                position   : position,
                ip         : ip,
                app_version: appVersion,
                create_time: now,
                update_time: now,
                expiry_time: new Timestamp(now.time + Constants.SESSION_DELAY_TIMES),
        ]
        db.dataSet("ws4_session").add(session);
        return session;
    }

}
