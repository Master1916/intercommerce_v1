package org.groovy.util

import org.apache.commons.lang.StringUtils
import org.groovy.common.Constants
import org.jpos.core.CardHolder
import org.jpos.iso.BaseChannel
import org.jpos.iso.ISOException
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOUtil
import org.jpos.iso.MUX
import org.jpos.tlv.TLVList
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.util
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-5
 * Time : 上午10:47
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
class TransUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, TransUtil.getSimpleName())

    /**
     * 组装交易报文
     * @param batch     批次号
     * @param trace     流水号
     * @param cardHolder    银行卡信息
     * @param amount        金额
     * @param pinBlock      没有可以传 null
     * @param merchantNo    商户号
     * @param terminalNo    终端号
     * @param cardSerialNum  银行卡序列号  没有可以传null
     * @param icData         IC域数据   没有可以传null
     * @return
     */
    static packageSaleMessage(String batch,String trace,CardHolder cardHolder,long amount,
                              String pinBlock,String merchantNo,String terminalNo,String cardSerialNum,String icData){
        try {
            ISOMsg message = new ISOMsg();
            message.set(0,"0200");
            message.set(2,cardHolder.getPAN());
            message.set(3,"000000");
            message.set(4,"${amount}".padLeft(12, "0"));
            message.set(11, trace);
            if(cardHolder.EXP){
                message.set(14, cardHolder.EXP);
            }
            if(icData){
                message.set(22, pinBlock ? "051" : "052" );
            } else {
                message.set(22, pinBlock ? "021" : "022" );
            }

            if(cardSerialNum){
                message.set(23, cardSerialNum);
            }
            message.set(25, "00");
            if(pinBlock){
                message.set(26, "06");
            }
            message.set(35, cardHolder.getTrack2());
            message.set(41, terminalNo);
            message.set(42, merchantNo);
            message.set(49, "156");
            if (pinBlock) {
                message.set(52, ISOUtil.hex2byte(pinBlock))
            }
            message.set(53, "2000000000000000");
            if(icData){
                message.set(55, ISOUtil.hex2byte(icData));
            }
            message.set(60, "22" + batch  + "000500");
            message.set(64, new byte[8]);
            return message;
        } catch (ISOException e){
            log.error("ISOException:" + e.getMessage());
        }
        return  null;
    }

    /**
     * 发送交易并收响应消息
     * @param msg
     * @param transLog
     * @param productNo
     * @return
     */
    static sendAndRecive(ISOMsg message) {
        try {
            def mux = NameRegistrar.get(Constants.ACQ_MUX_KEY) as MUX;
            def channel = NameRegistrar.getIfExists('channel.ts_channel') as BaseChannel;
            if(!channel){
                channel = NameRegistrar.get('channel.ts_channel0') as BaseChannel;
            }
            message.setPackager(channel.getPackager());
            message.setDirection(ISOMsg.OUTGOING);
            channel.setHeader("600005000803100310000");

            ISOMsg resp = mux.request(message, Constants.TRANS_TIMEOUT);
            if (resp && resp.hasField(38)) {
                def f38 = resp.getString(38).trim();
                if (!f38) resp.unset(38);
            }

            return resp;
        } catch (e) {
            log.error("package trans exception:" + e.getMessage());
            message.set(39, "96");
        }
        return message;
    }

    /**
     * 打包消费通知
     * @param traceNo        交易号
     * @param orgTrans       原始交易
     * @param cardNo         卡号
     * @param cardSerialNum  卡序列号
     * @param icData         IC域数据
     * @return
     */
    static packageSaleNotifyMessage(String traceNo,def orgTrans, String cardNo, String cardSerialNum, String icData){
        try {
            ISOMsg msg = new ISOMsg()
            msg.set(0, "0620");
            msg.set(2, cardNo);
            msg.set(3, "000000");
            msg.set(4, "${orgTrans.amount}".padLeft(12, "0"));
            msg.set(11, traceNo);
            if (cardSerialNum) {
                msg.set(23, cardSerialNum);
            }

            msg.set(37, orgTrans.reference_no);     //检索参考号
            if (orgTrans.auth_no) {
                msg.set(38, orgTrans.auth_no);      //授权标识授权码
            }
            msg.set(41, orgTrans.terminal_no);
            msg.set(42, orgTrans.merchant_no);
            msg.set(49, "156");
            if (icData) {
                msg.set 55, ISOUtil.hex2byte(icData);
            }
            msg.set(60, "00" + orgTrans.batch_no + "951500");
            msg.set(61, orgTrans.batch_no + orgTrans.trace_no + orgTrans.trans_date[-4..-1]);
            msg.set(64, new byte[8]);
            return  msg;
        }catch (e) {
            log.error("package notify exception:" + e.getMessage());
        }
        return null;
    }

    /**
     * 判断是否脚本回调
     * @param icData
     * @return
     */
    static boolean needScriptNotify(String icData){
        try {
            TLVList tlv = new TLVList();
            tlv.unpack(ISOUtil.hex2byte(icData));
            String send = tlv.getString(57137);
            return StringUtils.isBlank(send);
        } catch (e) {
            log.error("解析55域数据异常:" + e.getMessage(),e);
        }
        return false;
    }

    static packageQueryAmountMessage(String batch,String trace,CardHolder cardHolder,String merchantNo,String terminalNo,String pinBlock,String cardSerialNum,String icData){
        try {
            ISOMsg message = new ISOMsg();
            message.set(0, "0200");
            message.set(2, cardHolder.pan);
            message.set(3, "310000");
            message.set(11, trace);
            if(cardHolder.EXP){
                message.set(14, cardHolder.EXP);
            }
            if(icData){
                message.set(22, pinBlock ? "051" : "052" );
            } else {
                message.set(22, pinBlock ? "021" : "022" );
            }
            if(cardSerialNum){
                message.set(23, cardSerialNum);
            }
            message.set(25, "00")
            if(pinBlock){
                message.set(26, "06");
            }
            message.set(35, cardHolder.track2);
            message.set(41, terminalNo);
            message.set(42, merchantNo);
            message.set(49, "156");
            if (pinBlock) {
                message.set(52, ISOUtil.hex2byte(pinBlock));
                message.set(53, "2000000000000000");
            } else {
                message.set(53, "0000000000000000");
            }
            if(icData){
                message.set(55, ISOUtil.hex2byte(icData));
            }
            message.set(60, "01" + batch + "000500");
            message.set(64, new byte[8]);
            return message;
        } catch (e) {
            log.error("package query exception:" + e.getMessage());
        }
        return null;
    }
}
