package org.groovy.dao

import groovy.sql.Sql

import javax.sql.DataSource
import java.beans.PropertyChangeListener
import java.sql.SQLException

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.dao
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/16
 * Time : 17:29
 * To change this template use File | Settings | File and Code Templates.
 *
 */
class Dao {

    Sql db;

    Dao(){}

    Dao(DataSource ds) {
        db = new Sql(ds)
    }

    def wrap(data, table, id = 'id') {
        if (data == null || data instanceof ObservableMap) {
            return data
        } else {
            def om = new ObservableMap(data)
            om._table = table
            om._id = id
            om._update_fields = [] as Set
            om.addPropertyChangeListener( { evt ->
                evt.source._update_fields << evt.propertyName
            } as PropertyChangeListener)
            return om
        }
    }

    def update(ObservableMap data) {
        if (!data || !data.id || !data._table) return
        if (!data._update_fields) return true
        def params = []
        String updateSql = "update ${data._table} set "
        for (String field in data._update_fields) {
            if (data[field] != null) {
                updateSql += (params ? ', ' : '') + " $field = ?"
                params << data[field]
            }
        }
        updateSql += " where ${data._id} = ?"
        params << data[data._id]
        1 == db.executeUpdate(updateSql, params)
    }

    public void withTransaction(Closure closure) throws SQLException{
        synchronized (Dao.class){
            db.withTransaction(closure);
        }
    }

    /**
     * 获取序列Nextval
     *
     *
     * @param seqName 序列名称
     * @return
     * @author zhangshb
     * @since 2015-11-24
     */
    def getSeqNextval(seqName){
        def seqSql = "select " + seqName + ".nextval id from dual";
        return db.firstRow(seqSql).id as Long
    }

}
