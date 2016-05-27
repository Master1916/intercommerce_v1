package org.mobile.mpos.common;

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.mobile.mpos.common
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/16
 * Time : 18:37
 * To change this template use File | Settings | File and Code Templates.
 */
public class Mapping {

    /*接口映射关系*/
    public static final String INTERFACE_URL_LOGIN = "login"; //登录

    public static final String INTERFACE_URL_HEART_BEAT = "heartBeat"; //保持心跳

    public static final String INTERFACE_URL_LOGOUT = "logout"; //退出

    public static final String INTERFACE_URL_REGISTER = "register"; //注册

    public static final String INTERFACE_URL_SIGNIN = "signin"; //签到

    public static final String INTERFACE_URL_GETVERIFICATECODE = "sendMobileMessage";//获取验证码

    public static final String INTERFACE_URL_TRANS_MESSAGE = "transMessage";//获取交易小票短信

    public static final String INTERFACE_URL_ACTIVEANDBINDEQUIP = "activeAndBindEquip";//激活绑定设备

    public static final String INTERFACE_URL_REALNAMEAUTH = "realNameAuth";//实名认证

    public static final String INTERFACE_URL_REALNAMEAUTHSTATUS = "realNameAuthStatus";//获取实名认证状态

    public static final String INTERFACE_URL_ACCOUNTAUTH = "accountAuth";//账户认证

    public static final String INTERFACE_URL_ACCOUNTAUTHSATUS = "accountAuthStatus";//获取账户认证状态

    public static final String INTERFACE_URL_MERCHANTAUTH = "merchantAuth";//商户认证

    public static final String INTERFACE_URL_MERCHANTAUTHSTATUS = "merchantAuthStatus";//获取商户认证状态

    public static final String INTERFACE_URL_SIGNATUREAUTH = "signatureAuth";//签名认证

    public static final String INTERFACE_URL_SIGNATUREAUTHSTATUS = "signatureAuthStatus";//获取商户签名状态

    public static final String INTERFACE_URL_TRANS_STATUS = "transStatus";//查询交易状态

    public static final String INTERFACE_URL_QUERY_TRANS = "queryTrans";//查询接口

    public static final String INTERFACE_URL_RESET_PASSWORD = "resetPassword";//重置密码

    public static final String INTERFACE_URL_FORGET_PASSWORD = "forgetPassword";//忘记密码

    public static final String INTERFACE_URL_HANDIDCARDAUTH = "handIdCardAuth";//D0手持身份证半身照认证

    public static final String INTERFACE_URL_HANDIDCARDAUTHSTATUS = "handIdCardAuthStatus";//获取D0手持身份证半身照认证状态

    public static final String INTERFACE_URL_DZACCOUNTAUTH = "dzAccountAuth";//D0账户认证

    public static final String INTERFACE_URL_DZACCOUNTAUTHSTATUS = "dzAccountAuthStatus";//D0账户认证

    public static final String INTERFACE_URL_BANK_QUERY = "bankQuery";//联行号查询

    public static final String INTERFACE_URL_BANK_LIST = "bankList";//获取18家结算银行

    public static final String INTERFACE_URL_BIND_BANK_CARD = "bindBankCard";//绑定/解绑用户银行卡

    public static final String INTERFACE_URL_LIST_BANK_CARD = "listBandCard";//获取商户绑定银行卡列表

    public static final String INTERFACE_URL_SALE = "sale";//消费交易

    public static final String INTERFACE_URL_QUERY = "query";//余额查询

    public static final String INTERFACE_URL_DOWNLOADFINISHED = "downloadFinished";//ICkey完成回调接口

    public static final String INTERFACE_URL_SWIPER_CHANGE = "swiperChange";//更换设备

    public static final String INTERFACE_URL_DOWNLOADIMG = "downloadImg";//下载图片

    public static final String INTERFACE_URL_TRANSNOTIFY = "transNotify";//IC回调

    public static final String INTERFACE_URL_AUTHSTATUS = "authStatus";//四审认证状态

    public static final String INTERFACE_URL_DZAUTHSTATUS = "dzAuthStatus";//及时付认证状态

    public static final String INTERFACE_URL_MESSAGE = "message";//消息处理

    public static final String INTERFACE_URL_SHOWHTML = "showHtml";//静态页面显示

    public static final String INTERFACE_URL_SHOWPROTOCOL = "showProtocol";//需要登录页面显示

    public static final String INTERFACE_URL_TRANSD0AMOUNT = "transD0Amount";//D0当日交易限额

    public static final String INTERFACE_URL_TRANST1AMOUNT = "transT1Amount";//T1当日交易总额

    public static final String INTERFACE_URL_ERROR = "error";//错误信息

    public static final String INTERFACE_URL_BANNER = "banner";//广告信息

    public static final String INTERFACE_URL_DOWNLOADBANNER = "downloadBanner";//下载广告图片
}
