package org.groovy.common

import org.apache.commons.lang.BooleanUtils
import org.apache.commons.lang.SystemUtils
import org.mobile.mpos.common.Mapping

/**
 * 常量类
 *
 *
 * Create with IntelliJ IDEA
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.common
 * Author : zhangshb
 * User : Administrator
 * Date : 2015/11/16
 * Time : 17:51
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 *
 */
interface Constants {

    /*POST请求方式接口列表*/
    static final String[] REQUEST_METHOD_POST_LIST = [
            Mapping.INTERFACE_URL_LOGIN,
            Mapping.INTERFACE_URL_REGISTER,
            Mapping.INTERFACE_URL_ACTIVEANDBINDEQUIP,
            Mapping.INTERFACE_URL_GETVERIFICATECODE,
            Mapping.INTERFACE_URL_REALNAMEAUTH,
            Mapping.INTERFACE_URL_ACCOUNTAUTH,
            Mapping.INTERFACE_URL_MERCHANTAUTH,
            Mapping.INTERFACE_URL_SIGNATUREAUTH,
            Mapping.INTERFACE_URL_HANDIDCARDAUTH,
            Mapping.INTERFACE_URL_DZACCOUNTAUTH,
            Mapping.INTERFACE_URL_TRANS_STATUS,
            Mapping.INTERFACE_URL_RESET_PASSWORD,
            Mapping.INTERFACE_URL_SIGNATUREAUTH,
            Mapping.INTERFACE_URL_BIND_BANK_CARD,
            Mapping.INTERFACE_URL_SIGNIN,
            Mapping.INTERFACE_URL_SALE,
            Mapping.INTERFACE_URL_DOWNLOADFINISHED,
            Mapping.INTERFACE_URL_TRANSNOTIFY,
            Mapping.INTERFACE_URL_QUERY,
            Mapping.INTERFACE_URL_FORGET_PASSWORD,
            Mapping.INTERFACE_URL_SWIPER_CHANGE,
            Mapping.INTERFACE_URL_LOGOUT,
            Mapping.INTERFACE_URL_TRANS_MESSAGE,

    ]

    /*GET请求方式接口列表*/
    static final String[] REQUEST_METHOD_GET_LIST = [
            Mapping.INTERFACE_URL_REALNAMEAUTHSTATUS,
            Mapping.INTERFACE_URL_ACCOUNTAUTHSATUS,
            Mapping.INTERFACE_URL_MERCHANTAUTHSTATUS,
            Mapping.INTERFACE_URL_SIGNATUREAUTHSTATUS,
            Mapping.INTERFACE_URL_HANDIDCARDAUTHSTATUS,
            Mapping.INTERFACE_URL_DZACCOUNTAUTHSTATUS,
            Mapping.INTERFACE_URL_QUERY_TRANS,
            Mapping.INTERFACE_URL_BANK_QUERY,
            Mapping.INTERFACE_URL_BANK_LIST,
            Mapping.INTERFACE_URL_DOWNLOADIMG,
            Mapping.INTERFACE_URL_AUTHSTATUS,
            Mapping.INTERFACE_URL_DZAUTHSTATUS,
            Mapping.INTERFACE_URL_LIST_BANK_CARD,
            Mapping.INTERFACE_URL_MESSAGE,
            Mapping.INTERFACE_URL_SHOWHTML,
            Mapping.INTERFACE_URL_SHOWPROTOCOL,
            Mapping.INTERFACE_URL_HEART_BEAT,
            Mapping.INTERFACE_URL_TRANSD0AMOUNT,
            Mapping.INTERFACE_URL_TRANST1AMOUNT,
            Mapping.INTERFACE_URL_ERROR,
            Mapping.INTERFACE_URL_BANNER,
            Mapping.INTERFACE_URL_DOWNLOADBANNER
    ]

    /*蓝牙设备列表*/
    def BLUETOOTH_TERMINAL_LIST = [
            ITRONI21B_MODEL,
            MPOS_HISENSE,
            MPOS_LANDIM35,
            ITRON15_9_MODEL,
            DH_103_MODEL,
            MPOS_LANDIM18,
            TY_105_MODEL,
            TY_204_MODEL
    ]

    /*不需要MAC地址的设备列表*/
    def NOT_NEED_MAC_ADDRESS = [
            ITRONI_MODEL
    ]

    /*不需要登录的接口*/
    def NOT_NEED_LOGIN = [
            Mapping.INTERFACE_URL_BANK_LIST,
            Mapping.INTERFACE_URL_BANK_QUERY,
            Mapping.INTERFACE_URL_LOGIN,
            Mapping.INTERFACE_URL_REGISTER,
            Mapping.INTERFACE_URL_GETVERIFICATECODE,
            Mapping.INTERFACE_URL_FORGET_PASSWORD,
            Mapping.INTERFACE_URL_SHOWHTML,
            Mapping.INTERFACE_URL_ERROR,
            Mapping.INTERFACE_URL_BANNER,
            Mapping.INTERFACE_URL_DOWNLOADBANNER
    ]

    /*不需要MAC地址的设备列表*/
    def DISABLED_FUNCTION = [
            Mapping.INTERFACE_URL_TRANSD0AMOUNT
    ]

    /*接口请求参数一览*/
    def INTERFACE_REQ_PARAM = [
            "login"             : ['loginName', 'password', 'position'],
            "sendMobileMessage" : ['mobile'],
            "activeAndBindEquip": ['ksnNo', 'activeCode', 'product', 'model'],
            "register"          : ['mobile', 'password', 'idCode'],
            "realNameAuth"      : ['name', 'idNumber'],
            "accountAuth"       : ['name', 'bankName', 'unionBankNo', 'accountNo'],
            "merchantAuth"      : ['companyName', 'regPlace', 'businessLicense'],
            "dzAccountAuth"     : ['name', 'bankDeposit', 'bankName', 'unionBankNo', 'accountNo'],
            "transStatus"       : ['reqTime', 'reqNo', 'origReqTime', 'origReqNo', 'origTransType', 'amount'],
            "transMessage"      : ['mobile', 'reqNo', 'amount', 'terminalNo', 'merchantNo', 'batchNo'],
            "resetPassword"     : ['password', 'oldPassword'],
            "bankQuery"         : ['keyWord'],
            "signin"            : ['position'],
            "sale"              : ['position'],
            "query"             : ['position'],
            "downloadImg"       : ['fileName'],
            "forgetPassword"    : ['mobile','idCode'],
            "showHtml"          : ['html'],
            "showProtocol"      : ['view'],
            "swiperChange"      : ['ksnNo', 'model'],
            "downloadBanner"    : ['fileName'],
    ]

    /*错误码映射关系*/
    def error_code_mapping = [
            SUCCESS                       : '成功',
            ILLEGAL_ARGUMENT              : '请求错误, 请稍候再试',
            REQUEST_METHOD_POST           : '接口请求方式需为POST',
            REQUEST_METHOD_GET            : '接口请求方式需为GET',
            APP_VERSION_ERROR             : 'APP版本格式不匹配',
            MOBILE_ERROR                  : '手机格式不匹配',
            MOBILE_NO_EXIST               : '手机号不存在',
            MOBILE_EXISTED                : '手机号已存在',
            REQUEST_TOO_OFFEN             : '验证码请求时间过于频繁,请稍后再试',
            SEND_MSG_FAILS                : '今天发送短信过多',
            INVALID_USERRULE              : '用户非法, 有问题请咨询客服',
            UPGRADE_SYSTEM                : '检测到新版本，请更新',
            ILLEGAL_LOGIN_OR_PASSWD       : '无效账户或者密码',
            ILLEGAL_ARGUMENT              : '商户错误',
            KSNNO_PSAM_NOT_MATCH          : '安全卡不适用该设备',
            SWIPER_NOT_EXIST              : '该设备不可用',
            KSNNO_ACTIVATED               : '刷卡器已经激活过',
            KSNNO_NOT_AVAILABLE           : '请确认刷卡器是新购买的, 且没有激活过',
            KSNNO_OR_LICENSECODE_NOT_EXIST: '激活码错误,请重新输入',
            ACTIVECODE_USED               : '激活码已被使用或作废,请重新输入',
            MOBILE_NOT_VALIDATE           : '该手机号没有验证',
            PROCESSING                    : '处理中...',
            PASSWORD_NOT_VALIDATE         : '密码格式不正确',
            SYSTEM_ERROR                  : '系统异常',
            UPGRADE_SYSTEM                : '检测到新版本，请更新',
            SWIPE_OR_POS_NOT_MATCH        : '激活码不匹配',
            ACTIVATION_CODE_IS_INVALID    : '激活码无效',
            IDCODE_VALIDATE_COUNT         : '验证码已过期,请重新获取',
            IDCODE_ERROR                  : '验证码输入错误',
            AUTH_NOT_EXIST                : '个人商户信息未发现',
            AUTH_PASSED                   : '认证已通过,请核实',
            AUTH_CHECKING                 : '正在审核中,请耐心等待',
            AUTH_FAIL                     : '认证失败',
            AUTH_NOT_COMMIT               : '未认证',
            AUTH_CLOSEDOWN                : '已关停',
            REALNAME_AUTH_COMMIT_FAIL     : '实名认证信息提交失败',
            IMAGE_SIZE_NOT_QUALIFIED      : '图片大小不合格',
            MEROPERATOR_NOT_FOUND         : '商户操作员信息未发现',
            ACCOUNT_AUTH_COMMIT_FAIL      : '账户认证信息提交失败',
            NOT_SUPPORT_CARD              : '该卡暂不支持,请更换',
            CREDIT_CARD_CANOT_REGISTER    : '信用卡不能注册',
            CARD_CANT_REGISTER            : '该银行卡已经被注册过,请更换',
            MERCHANT_NOT_FOUND            : '商户未发现',
            MERCHANT_OPRACTOR_NOT_FOUND   : '个人商户信息未发现',
            MERCHANT_AUTH_COMMIT_FAIL     : '商户认证信息提交失败',
            NAME_IS_TOO_LONG              : '商户名称过长，不要超过20个汉字',
            MERCHANT_NAME_NOT_EMPTY       : '商户名称不能为空',
            ADDR_IS_TOO_LONG              : '经营地址过长，不要超过64个汉字',
            ADDR_NOT_EMPTY                : '经营不能为空',
            ACTIVE_BIND_EQUIP_FAIL        : '激活绑定设备失败',
            BUSINESSLICENSE_NOT_MATCH     : '营业执照号不符合规范',
            BUSINESSLICENSE_IS_TOO_LONG   : '营业执照号需保持在7-30位',
            REQUEST_NOT_NULL              : '信息不能为空',
            NOT_SUPPORT_BUSINESS          : '及时付业务不支持,详情咨询代理商',
            APPLY_OPEN_TIMELYPAY          : '请向代理商申请开通及时付业务',
            HANDIDCARD_AUTH_COMMIT_FAIL   : '手持身份证半身照认证信息提交失败',
            NEED_AUTH_HANDIDCARD          : '请先完成及时付手持身份证半身照认证',
            DZACCOUNT_AUTH_COMMIT_FAIL    : '及时付账户认证信息提交失败',
            BANK_NAME_NOT_SUPPORT         : '该银行暂不支持,请更换',
            CARD_NOT_SUPPORT              : '输入的卡号不正确',
            CARD_TYPE_NOT_SUPPORT         : '信用卡暂不支持,请更换',
            MERCHANT_CONFIRM_NOTPASS      : '该商户认证没通过',
            TRANSNOTIFY_FAIL              : 'IC回调失败',
            REGISTER_FAIL                 : '注册失败',
            SEND_FAIL                     : '发送失败',
            DEVICE_NOT_MATCH              : '设备不匹配',
            USER_NOT_EXIST                : '用户不存在',
            UPLOAD_FAIL                   : '上传图片失败',
            IMG_FORMAT_NOT_CORRECT        : '图片格式不匹配',
            ILLEGAL_KSNNO                 : '设备已注册并使用',
            MERCHANT_CLOSE_DOWN           : '商户已关停，详情咨询代理商',
            MERCHANT_REALNAME_ERROR       : '抱歉, 实名信息与您上次录入的不符'


    ]

    def trans_error_code = [
            "04":"4:没收卡，请联系收单行。",
            "21":"21:交易失败，请联系发卡行。",
            "25":"25:交易失败，请联系卡卡行。",
            "45":"45:交易失败，请插入芯片，进行交易。",
            "51":"51:余额不足，请查询。",
            "55":"55:密码错误，请重试。",
            "61":"61:金额超限。",
            "62":"62:交易失败，请联系发卡行。",
            "75":"75:密码错误次数超限。",
            "91":"91:交易失败，请稍后重试。"
    ]

    /*HTTP请求相关配置*/
    static httpcofig = [
            SO_TIMEOUT               : "30000",
            HEAD_BODY_CHECK_TIMEOUT  : "3000",
            STATUS_LINE_GARBAGE_LIMIT: "0",
            MAX_REDIRECTS            : "5",
            CONN_MANAGER_TIMEOUT     : "3000"
    ]

    def static keyWords = [
            "中国银行",
            "建设银行",
            "工商银行",
            "农业银行",
            "交通银行",
            "招商银行",
            "光大银行",
            "民生银行",
            "华夏银行",
            "中信银行",
            "恒丰银行",
            "北京银行",
            "浦东发展银行",
            "浙商银行",
            "兴业银行",
            "平安银行",
            "广东发展银行",
            "邮政储蓄银行",
            "零",
            "一",
            "二",
            "三",
            "四",
            "五",
            "六",
            "七",
            "八",
            "九"
    ];


    static String MPOS_LANDIM35 = 'landim35'; //联迪M35
    static String MPOS_LANDIM18 = 'landim18'; //联迪M18
    static String MPOS_HISENSE = 'hz-m20'; //V203
    static String ITRONI_MODEL = 'itroni21'; //V1
    static String ITRONI21B_MODEL = 'itroni21b'; //艾创102
    static String ITRON15_9_MODEL = 'itron15-9'; //V202
    static String DH_103_MODEL = 'dh-103'; //鼎和V103
    static String TY_105_MODEL = 'ty63250'; //天喻105
    static String TY_204_MODEL = 'ty71249'; //天喻204
    static String CARD_TYPE_CREDIT = 'credit';

    /*品牌*/
    static String SHZF = 'SHZF'  //上海掌富
    static String SZKM = 'SZKM'  //神州卡盟
    static String ZFT = 'ZFT'    //掌富通

    //日期格式
    static final String DATE_FORMAT = 'yyyyMMddHHmmss'
    static final String DATE_FORMAT_24 = 'yyyyMMddhh24miss'
    static final String DATE_FORMAT_YMD = 'yyyyMMdd'
    static final String DATE_FORMAT_YEAR = 'yyyy'
    static final String DATE_FORMAT_SEMICOLON = "yyyyMMddHH:mm:ss";
    static final String YMD_DATE_FORMAT = "yyyy-MM-dd";
    static final String DATE_FORMAT_COMPLEX = "MM月dd日HH:mm";
    static final String DATE_FORMAT_SPLIT = "yyyy-MM-dd HH:mm:ss";
    static final String GMT_TIME_PATTERN = "EEE, dd MMM yyyy HH:mm:ss";
    static final String DEFAULT_START_TIME = "07:00:00";
    static final String DEFAULT_END_TIME = "16:50:00";

    //HTTP请求方式
    static final String REQUEST_METHOD_POST = 'post'
    static final String REQUEST_METHOD_GET = 'get'

    //联迪设备M35 KEY
//    static String LANDIM35_KEY = "9B8A6C3D7E2F0C3D"

    //短信接口相关参数
    static final String MSG_URL = "http://3tong.net/http/sms/Submit";
    static final String MSG_ACCOUNT = "dh26021";  //dh24551
    static final String MSG_PASSWORD = "Gfwc@0^1";//4h~1~O!A
    static final String MSG_MESSAGEID = "";
    static final String MSG_SUBCODE = "";
    static final String MSG_SENDTIME = "";
    //4要素认证通道--回调
    static final String MESSAGE_AUTH_RECEIVER_URL = Commons.config.url.message_receiver;//"http://172.16.1.221:9090/certauth/notify.do"
    //4要素认证通道--认证
    static final String MESSAGE_AUTH_URL = Commons.config.url.message_auth;//"http://106.37.206.154:12862/handler"
    //交易短信查看地址
    static final String TRANS_MESSAGE_CHECK = Commons.config.url.message_notify_url;//"http://branchbts.21er.net:15080/html/smssearch.html"
    //消息访问路径
    static final String MESSAGE_CENTER = Commons.config.url.message;//"http://172.16.1.221:9090/news/doNewsPost.do"
    //加密key
    static final String MESSAGE_CENTER_KEY = "123456";
    //加密MERCHANT_ID
    static final String MESSAGE_AUTH_MERCHANT_ID = Commons.config.url.message_auth_merchant_id// "110000000000019";

    //字符编码
    static final String CHARSET_UTF_8 = "utf-8"
    //会话标志
    static final String WS_SESSION = "WSSESSION";
    //移除会话标志
    static final String REMOVE_WSHSNO = "REMOVE-WSSESSION";

    //接口访问时间校验：超时限定时间（30min）
    public static final long INTERFACE_TIMEOUT_LIMIT = 30 * 60 * 1000;
    //短信验证码超时限定时间（3min）
    public static final long IDCODE_TIMEOUT_LIMIT = 3 * 60 * 1000;
    //短信验证码允许的输入错误次数(5次)
    public static final long IDCODE_LIMIT_TIMES = 5;
    //session 延迟时间(30min)
    public static final long SESSION_DELAY_TIMES = 1000 * 60 * 30;

    //trans time out 1min
    static final long TRANS_TIMEOUT = 60 * 1000L;
    //短信一天的最大发送次数(5)
    static final long MESSAGE_MAX_SENDCOUNT_DAY = 5;
    //余额查询次数(10)
    static final long QUERY_COUNT_LIMIT = 10;

    //provider
    static final String PROVIDER = "com.sun.crypto.provider.SunJCE";
    //key store
    static final String KEY_STORE_KEY = "keyStore";
    //hsm
    static final String SMADAPTER_KEY = "hsm";
    //front zpk
    static final String FRONT_ZPK_KEY = "internal.zpk";
    //mux key
    static final String ACQ_MUX_KEY = "mux.ts_mux";
    //user id
    static final String _INNER_USER_ID = "_INNER_USER_ID"
    //MERCHANTID
    static final String _INNER = "_INNER"
    //文件分隔符
    static final String FILE_SEPARATOR = SystemUtils.FILE_SEPARATOR;
    //四要素验证关键字
    static final String KEYWORD_MPOSP = "MPOSP"
    //是否真正发送短信
    static final boolean IS_SEND_MESSAGE = BooleanUtils.toBoolean(Commons.config.controller.is_send_message);
    //是否校验银行卡4要素审核
    static final boolean IS_CARD_VALIDATE = BooleanUtils.toBoolean(Commons.config.controller.is_card_validate);

}
