package org.groovy.util

class TransferConstants {

    def static INTEGER = "Integer"
    def static BIGDECIMAL = "BigDecimal"
    def static BIGDECIMAL100 = "BigDecimal100"

//    好贷宝接口转换器
    static INTERFACE_MAPPING = [
            INTERFACE_URL_BUYPRODUCT    : "buyProduct",
            INTERFACE_URL_CASH          : "cash",
            INTERFACE_URL_CHECKACC      : "checkAcc",
            INTERFACE_URL_GETACC        : "getAcc",
            INTERFACE_URL_GETBUYINFO    : "getBuyInfo",
            INTERFACE_URL_PRODUCTBUYINFO: "productBuyInfo",
            INTERFACE_URL_PRODUCTINFO   : "productInfo",
            INTERFACE_URL_PRODUCTLIST   : "productList",
            INTERFACE_URL_RETURNMONEY   : "returnMoney"
    ]

    static {
        //初始化特定處理列表
        VALUE_TRANSFER_PRIVATE.put(INTERFACE_MAPPING.INTERFACE_URL_PRODUCTLIST, [:])
        VALUE_NOT_TRANSFER.put(INTERFACE_MAPPING.INTERFACE_URL_PRODUCTINFO, [])
    }

    //特定接口中的字段处理方式
    def static VALUE_TRANSFER_PRIVATE = [:]

    //接口中都需要转换的字段
    def static VALUE_TRANSFER_PUBLIC = [
            "term"                 : INTEGER,
            "termcate"             : INTEGER,
            "productType"          : INTEGER,
            "productStatus"        : INTEGER,
            "repayment"            : INTEGER,
            "minbidamt"            : BIGDECIMAL100,
            "allAmount"            : BIGDECIMAL100,
            "canBuyAmount"         : BIGDECIMAL100,
            "actualamt"            : BIGDECIMAL100,
            "cashfee"              : BIGDECIMAL100,
            "amt"                  : BIGDECIMAL100,
            "cashRxbAmount"        : BIGDECIMAL100,
            "cashCanAmount"        : BIGDECIMAL100,
            "exportAmount"         : BIGDECIMAL100,
            "regularEarnedAmount"  : BIGDECIMAL100,
            "currentEarnedAmount"  : BIGDECIMAL100,
            "frozenAmount"         : BIGDECIMAL100,
            "regularDayEarndAmount": BIGDECIMAL100,
            "currentDayEarndAmount": BIGDECIMAL100,
            "canAmount"            : BIGDECIMAL100,
            "regularAmount"        : BIGDECIMAL100,
            "regularInterest"      : BIGDECIMAL100,
            "currentAmount"        : BIGDECIMAL100,

    ]

    //   特定接口中的不需要转换的字段
    def static VALUE_NOT_TRANSFER = [:]
}
