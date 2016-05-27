package org.groovy.dao.terminal

import org.groovy.common.Commons
import org.groovy.dao.Dao

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.terminal
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-11-30
 * Time : 下午7:24
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
class DeviceDao extends Dao {
    DeviceDao() {
        super(Commons.getDAO());
    }

    def findDeviceByKsn(String ksnNo) {
        if (!ksnNo) return null
        db.firstRow("""select * from mcr_ksn mk where mk.ksn_no=?""", [ksnNo]);
    }

    def findTerminalByKsn(String ksnNo) {
        if (!ksnNo) return null
        db.firstRow("""select * from merchant_terminal mt
                  left join mcr_ksn mk on mt.id=mk.terminal_id where mk.ksn_no = ?""", [ksnNo])
    }

}
