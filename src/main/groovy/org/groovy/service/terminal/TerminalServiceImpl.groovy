package org.groovy.service.terminal

import org.groovy.common.Commons
import org.groovy.common.Constants
import org.groovy.dao.merchant.MerchantDao
import org.groovy.dao.terminal.TerminalDao
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.terminal.TerminalService

import javax.servlet.http.HttpServletRequest

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service
 * To change this template use File | Settings | File and Code Templates.
 */
class TerminalServiceImpl implements TerminalService {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, TerminalServiceImpl.getSimpleName())

    /**
     * 设备更换
     * @param request Http请求参数
     * @return
     */
    @Override
    Object swiperChange(HttpServletRequest request) {
        //引用数据库操作
        MerchantDao merchantDao = new MerchantDao();
        TerminalDao terminalDao = new TerminalDao();
        terminalDao.db = merchantDao.db;

        //获取请求参数
        def user = Commons.initUserRequestParams(request);
        def params = Commons.parseRequest(request);
        def ksnNo = params.ksnNo;//获取新设备ksnNo参数

        //查询登录商户信息
        def acqUser = merchantDao.findCmPersonByMobileNo(user.user_id);
        if (!acqUser) {
            return Commons.fail(null, "USER_NOT_EXIST", "用户不存在")
        }

        //获取老设备ksn信息
        def oldKsn = terminalDao.findKsnInfoByMerchantId(acqUser.merchant_id)

        //老设备激活码
        def license = terminalDao.findLicenseByKSNId(oldKsn.id)

        //新设备ksn
        def newKsn = terminalDao.findKsnByKsnNo(ksnNo)

        try {
            log.info "old: $oldKsn", "new: $newKsn"
            if (!newKsn || newKsn.is_used || newKsn.is_activated) {
                return Commons.fail(null, "SWIPER_NOT_EXIST", "请确认刷卡器和序列号是新购买的, 且没有激活过")
            }

            //新的KSN如果是老设备的KSN就不能置换
            if (oldKsn.id == newKsn.id) {
                return Commons.fail(null, 'ILLEGAL_ARGUMENT', '请使用新刷卡器')
            }

            //响应终端提示语
            def repNote = '已替换, 新KSN ' + "${newKsn.ksn_no}"
            oldKsn.note ? oldKsn.note + '\n\n' + repNote : repNote

            //通过KSN获取新设备的型号
            def model = Commons.getModelByKsnNo(ksnNo)

            //上送的新设备型号
            def reqModel = params.model

            //设备型号不一致不能替换
            if (reqModel && reqModel != model) {
                log.info("reqModel=${reqModel},model=${model}")
                return Commons.fail(null, 'PERMISSION_DENIED', '抱歉, 您不能进行此操作')
            }

            //联迪M35手刷设备必须上传MAC地址
//            if (model == Constants.MPOS_LANDIM35 || model == Constants.MPOS_HISENSE || model == Constants.ITRON15_9_MODEL || model == Constants.ITRONI21B_MODEL || model == Constants.DH_103_MODEL) {
            if(!(model in Constants.NOT_NEED_MAC_ADDRESS)){
                if (!params.macAddress) {
                    log.info("macAddress=" + params.macAddress);
                    return Commons.fail(null, 'PERMISSION_DENIED', '未识别MAC地址!')
                }
                newKsn.mac_address = params.macAddress
            }

            //新ksn激活修改状态
            newKsn.is_activated = true
            newKsn.is_used = true
            newKsn.terminal_id = oldKsn.terminal_id

            //激活码对应的ksn和终端号变化
            license.ksn_id = newKsn.id
            license.terminal_id = oldKsn.terminal_id

            //老KSN修改终端号和状态
            oldKsn.is_used = true
            oldKsn.is_activated = false
            oldKsn.terminal_id = null

            def isUpdate = false;
            def loginName = acqUser.login_name
            def name = loginName.split(/\./)
            def terminal = terminalDao.findTerminalByMerchantId(acqUser.merchant_id)
            if (reqModel != name[1]) {
                if ((reqModel in Constants.BLUETOOTH_TERMINAL_LIST) && (name[1] in Constants.BLUETOOTH_TERMINAL_LIST)) {
                    loginName = name[0] + "." + reqModel + "." + name[2]
                    terminal.terminal_model_id = terminalDao.findTerminalModelByProductModel(reqModel)?.id
                    isUpdate = true;
                } else {
                    log.info("reqModel=${reqModel}, name[1]=${name[1]}")
                    return Commons.fail(null, 'PERMISSION_DENIED', '抱歉, 您不能进行此操作');
                }
            }

            //返回信息
            def result = [
                    respNo: params.respNo ?: null,
            ]

            merchantDao.withTransaction {

                //修改操作登录名、修改终端类型
                if(isUpdate){
                    merchantDao.updateLoginNameByOperatorId(loginName, acqUser.id)
                    terminalDao.update(terminal)
                }

                //更新老激活码状态
                terminalDao.updateICPublicStatus(oldKsn.ksn_no as String, "11");

                //变更ksn激活码状态
                terminalDao.update(oldKsn)
                terminalDao.update(newKsn)
                terminalDao.update(license)
            }
            return Commons.success(result, "置换设备成功");
        } catch(Exception e) {
            log.info("置换设备失败：${e.getMessage()}");
            return Commons.fail(null, '', '置换设备失败');
        }
    }
}
