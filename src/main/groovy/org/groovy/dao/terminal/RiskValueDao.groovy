package org.groovy.dao.terminal

import org.groovy.common.Commons
import org.groovy.dao.Dao

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao.terminal
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-12
 * Time : 下午5:41
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class RiskValueDao extends Dao{
    public RiskValueDao(){
        super(Commons.getDAO());
    }

    def findRiskValueByProductIdAndModelId(String productId,String modelId){
        db.firstRow("""select * from ws_riskvalue where product_id=? and model_id=?""",[productId,modelId]);
    }
}
