#intercommerce_v1接口规范
> **Beta:**
> 该API仍处于 Beta 阶段

```
1. 该接口适用对象：中汇商户通APP
2. 该接口实现功能：用户收单
3. 该接口调用规范：采用HTTP请求与中汇的前置POSP进行通信
```
> **注：**
> 文中所有 `<>` 标注的字段，均需根据你的实际情况替换（无需 `<>` 符号，仅作标注之用）
> 文中所有 `:id` 标注的字段，均需根据该资源的实际 `id` 值替换
> 文中所有 `{x|y|...}` 标注的字段，均需根据你的实际情况用其中一个 `x` 或者 `y`（ `|` 分割）替换

## API 接口地址
```
暂无 # 生产环境
http://mposp.21er.tk # 测试环境
```

## 标准请求
```sh
curl -X POST \
    http://mposp.21er.tk/<资源路径> \
    # 其他可选参数，参数以键值对呈现...
```

## 标准响应
* 订单校验通过的情况下  
```
HTTP/1.1 200 OK
Server: Nginx
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

...body...
```

* 订单校验未通过的情况下  
```
HTTP/1.1 401 Unauthorized
```
* 请求 Method 不被支持的情况下  
```
HTTP/1.1 405 Method Not Allowed
```
* 请求参数不正确的情况下  
```
HTTP/1.1 403 Forbidden
```
<a id="content-title"></a>
## 功能路径列表
| 资源名称     | 路径                                     | Content-Type         | 请求方式     | 维护人     | 是否需要登录|
|-------------|-----------------------------------------|----------------------|---------------|---------------|---------------|
| 获取验证码| [/sendMobileMessage](#sendMobileMessage)                      | urlencoded           | POST   | 张树彬     | 否   |
| 登录| [/login](#login)                      | urlencoded           | POST      | 李飞     | 否   |
| 退出| [/logout](#logout)                      | urlencoded           | POST      | 李飞     | 是   |
| 注册| [/register](#register)                      | urlencoded           | POST   |  李飞     | 否   |
| 签到| [/signin](#signin)                      | urlencoded           | POST   | 李飞     | 是   |
| 保持心跳| [/heartBeat](#heartBeat)                      | urlencoded           | POST   | 李飞     | 是   |
| D0当日交易剩余金额 | [/transD0Amount](#transD0Amount)                      | urlencoded           | GET   | 李飞     | 是   |
| T1当日交易总额 | [/transT1Amount](#transT1Amount)                      | urlencoded           | GET   | 李飞     | 是   |
| ICkey回调接口| [/downloadFinished](#downloadFinished)                      | urlencoded           | POST   | 李飞     | 是   |
| 修改密码| [/resetPassword](#resetPassword)                      | urlencoded           | POST   | 李飞     | 是   |
| 忘记密码| [/forgetPassword](#forgetPassword)                      | urlencoded           | POST   | 李飞     | 否   |
| 查询交易状态| [/transStatus](#transStatus)                      | urlencoded           | POST   | 李飞     | 是   |
| 发送交易小票接口| [/transMessage](#transMessage)                      | urlencoded           | POST   | 李飞     | 是   |
| 查询交易| [/queryTrans](#queryTrans)                      | urlencoded           | GET   | 李飞     | 是   |
| 联行号查询| [/bankQuery](#bankQuery)                      | urlencoded           | GET   | 李飞     | 否   |
| 获取18家结算银行| [/bankList](#bankList)                      | urlencoded           | GET   | 李飞     | 否   |
| 绑定/解绑用户银行卡| [/bindBankCard](#bindBankCard)                      | urlencoded           | GET   | 李飞     | 是   |
| 获取用户银行卡列表| [/listBandCard](#listBandCard)                      | urlencoded           | GET   | 李飞     | 是   |
| 激活绑定设备| [/activeAndBindEquip](#activeAndBindEquip)                      | urlencoded           | POST   | 张树彬     | 是   |
| 实名认证| [/realNameAuth](#realNameAuth)                      | urlencoded           | POST   | 张树彬     | 是   |
| 实名认证信息回显| [/realNameAuthStatus](#realNameAuthStatus)                      | urlencoded           | GET   | 张树彬  | 是   |
| 商户认证| [/merchantAuth](#merchantAuth)                      | urlencoded           | POST   | 张树彬     | 是   |
| 商户认证信息回显| [/merchantAuthStatus](#merchantAuthStatus)                      | urlencoded           | GET   | 张树彬  | 是   |
| 账户认证| [/accountAuth](#accountAuth)                      | urlencoded           | POST   | 张树彬     | 是   |
| 账户认证信息回显| [/accountAuthStatus](#accountAuthStatus)                      | urlencoded           | GET   | 张树彬    | 是   |
| 签名认证| [/signatureAuth](#signatureAuth)                      | urlencoded           | POST   | 张树彬    | 是   |
| 签名认证信息回显| [/signatureAuthStatus](#signatureAuthStatus)                      | urlencoded           | GET   | 张树彬| 是   |
| 及时付身份证认证| [/handIdCardAuth](#handIdCardAuth)                      | urlencoded           | POST   | 张树彬     | 是   |
| 及时付身份证认证回显| [/handIdCardAuthStatus](#handIdCardAuthStatus)                 | urlencoded      | GET   | 张树彬 | 是   |
| 及时付账户认证| [/dzAccountAuth](#dzAccountAuth)                      | urlencoded           | POST   | 张树彬     | 是   |
| 及时付账户认证信息回显| [/dzAccountAuthStatus](#dzAccountAuthStatus)                      | urlencoded    | GET   | 张树彬 | 是   |
| 消费 | [/sale](#sale)                      | urlencoded           | POST   | 李飞     | 是   |
| 余额查询 | [/query](#query)                      | urlencoded           | POST   | 李飞     | 是   |
| 四审认证状态查询 | [/authStatus](#authStatus)                      | urlencoded           | GET   | 张树彬     | 是   |  
| 及时付认证状态查询 | [/dzAuthStatus](#dzAuthStatus)                      | urlencoded           | GET   | 张树彬     | 是   |
| 认证图片下载 | [/downloadImg](#downloadImg)                      | urlencoded           | GET   | 张树彬     | 是   |
| IC回调 | [/transNotify](#transNotify)                      | urlencoded           | POST   | 张树彬     | 是   |
| 更换设备 | [/swiperChange](#swiperChange)                      | urlencoded           | POST   | 李飞     | 是   |
| 静态页面显示 | [/showHtml](#showHtml)                      | urlencoded           | GET   | 李飞     | 否   |
| 需要登录页面显示 | [/showProtocol](#showProtocol)                      | urlencoded           | GET   | 李飞     | 是   |
| 获取消息接口/更新消息状态 | [/message](#message)                      | urlencoded           | GET   | 李飞     | 否   |
| 获取广告位信息 | [/banner](#banner)                      | urlencoded           | GET   | 张树彬     | 否   |
| 广告位图片下载 | [/downloadBanner](#downloadBanner)                      | urlencoded           | GET   | 张树彬     | 否   |
----------------------------------------------------------------------------------
<a id="sendMobileMessage"></a>
### 获取验证码  /sendMobileMessage
#### 1\. 通过手机号获取验证码
请求：  
```
POST /sendMobileMessage HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
mobile: "15801376995"
type: "registe" //注册、忘记密码必传(registe/forget)  (非必传项)
```
响应：  
```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
   "respTime":"20151125161740",
   "isSuccess":true,
   "respCode":"SUCCESS",
   "respMsg":"发送验证码成功,注意查收"
   
}
```
##### [返回目录↑](#content-title)
<a id="login"></a>
### 登录  /login
#### 1\. 手机号登录
请求：  
```
POST /login HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"position": "116.379062,39.97077"
"password": "qqqqqq"
"appVersion": "android.ZFT.1.2.143"
"loginName": "18911156118"
"reqTime": "20151228143806"

```
响应：  
```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime": "20151228143800",
    "isSuccess": true,
    "respCode": "SUCCESS",
    "respMsg": "登录成功",
    "isMobileMerchant": true, //是否为手机商户
    "isPosMerchant": false, //是否为POS商户
    "posStatus": 0 //POS认证状态 (0未绑定 ,1待刷卡，2待认证,3实名认证通过)
}
```

##### [返回目录↑](#content-title)
<a id="logout"></a>
### 退出  /logout
#### 1\. 退出
请求：  
```
POST /logout HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

    "appVersion": "ios.未知.1.1.813"

```
响应：  
```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151126184737",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"退出成功."
}
```
##### [返回目录↑](#content-title)
<a id="register"></a>
### 注册  /register
#### 1\. 通过手机号注册
请求：  
```
POST /register HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

mobile: "15801376995"
password: "123456"
appVersion: "ios.未知.1.1.813"
idCode: 1234 //验证码

```
响应：  
```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151126184737",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"祝贺您成功注册."
}
```
##### [返回目录↑](#content-title)
<a id="downloadFinished"></a>
### ICkey完成回调接口  /downloadFinished
#### 1\. ICkey完成回调接口
请求：  
```
POST /downloadFinished HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```
响应：  
```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151126184737",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"已更新状态."
}
```
##### [返回目录↑](#content-title)
<a id="signin"></a>
### 签到  /signin
#### 1\. 签到
请求：  
```
POST /signin HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

    "appVersion":"ios.未知.1.1.813",
    "position":"117.194778,39.113809",

```
响应：  
```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime": "20151228155553",
    "isSuccess": true,
    "respCode": "SUCCESS",
    "respMsg": "签到成功",
    "businessT1":     {
        "isMerchantT1": true,
        "info":         {
            "status": "1111",
            "name": "沈陈",
            "cardTail": "2371",
            "bluetoothName": "MPOS5093000113",
            "serialType": "0.78--26",
            "merchantReason": "",
            "realReason": "",
            "signatureReason": "",
            "accountReason": "",
            "feeRate": "0.78~26"
        }
    },
    "model": "dh-103",
    "needUpdateIC": false,
    "device":     {
        "keyType": "3DES",
        "keyValue": "BF820AAA6F5407579EB76817B50721D6",
        "checkValue": "EF95B52C",
        "pinKeyType": "RSA",
        "pinKeyValue": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhF3+leaXzmmTzZNua2xv+k2/Z76cFIQ13XfSrXgNuGArnlyKUZR7z6zjGbtEG2u/JH9MDJOADlsn9HsGie9YvXvuUv7I0OhUB6MSWnofyJTBuXhDCHzrQveVtX26HbduQELvfduZOBmPMgC5vFxnnhFcYQn9ns9onR8mk+uy6/hgmWNeNHjhwgr/kbeuOTRiw6gtChnuPcOITpIb2eYfiK/a3+30teuCalXJZwXO30VtcUaaL69luLIofIGH9Lu4wClrxHoostCE5Anc/1qJbWsNGXmXHYTNw6vv7wasMSPPyf6OqmaeuTx/GLaxrWtUAVPEylbTDI3z6EycfkrThwIDAQAB",
        "isBluetooth": true,
        "macAddress": "00:0C:BF:0C:92:16",
        "ksnNo": "850093000113"
    },
    "agencyTrade": true,
    "traceNo": 80,
    "businessD0":     {
        "isMerchantD0": true,
        "isHoliday": false,
        "startTime": 1451239503000,
        "endTime": 1451311627000,
        "info":         {
            "merchantTradeStatus": 3,
            "accountD0Status": 1,
            "merchantFeeRate": "0.78~26",
            "merchantAddFeeRate": "0"
        }
    }
}

```
##### [返回目录↑](#content-title)
<a id="resetPassword"></a>
### 修改密码  /resetPassword
#### 1\. 修改密码
请求：  
```
POST /resetPassword HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

password: "123456"
oldPassword: "354689"
appVersion: "ios.未知.1.1.813"
```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "reqNo": 645254,
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"验证成功"
}
```
##### [返回目录↑](#content-title)
<a id="forgetPassword"></a>
### 忘记密码  /forgetPassword
#### 1\. 忘记密码
请求：  
```
POST /forgetPassword HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

password: "123456"
idNumber: "413023199101259999" //身份证
mobile: "15801376995"
idCode: "5741"//验证码
appVersion: "ios.未知.1.1.813"
```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "reqNo": 645254,
    "isMerchant": true,    //是否需要输入身份证
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"验证成功"
}
```
##### [返回目录↑](#content-title)
<a id="heartBeat"></a>
### 保持心跳  /heartBeat
#### 1\. 保持心跳
请求：  
```
POST /heartBeat HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"成功"
}
```
##### [返回目录↑](#content-title)
<a id="transD0Amount"></a>
### D0当日交易剩余金额  /transD0Amount
#### 1\. D0当日交易剩余金额
请求：  
```
GET /transD0Amount HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "merchantAccount":5000000,//当日商户剩余可用金额
    "agencyAccount":2999992790,
    "showAccount":5000000,
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"成功"
}
```
##### [返回目录↑](#content-title)
<a id="transT1Amount"></a>
### T1当日交易总额  /transT1Amount
#### 1\. T1当日交易总额
请求：  
```
GET /transT1Amount HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "amount":5000000,
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"成功"
}
```
##### [返回目录↑](#content-title)

<a id="transStatus"></a>
### 查询交易状态  /transStatus
#### 1\. 查询交易状态
请求：  
```
POST /transStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

amount: "11111"
origTransType: "sale"
origReqNo: "1111"
origReqTime: "20151124111059"
reqNo: "qqq"
reqTime: "20151124111059"
appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "reqNo": 645254,
    "merchantName": "数目数目",
    "merchantNo": 111111111,
    "terminalNo": 22222,
    "operatorNo": 01,
    "cardNoWipe": 645***254,
    "amount": 1234,
    "currency": "CNY",
    "issuer": "XX银行",
    "voucherNo": 2222,
    "batchNo": 123,
    "transTime": 20151130125253,
    "refNo": 1234,
    "authNo": 1234,
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"验证成功"
}
```
##### [返回目录↑](#content-title)
<a id="transMessage"></a>
### 发送交易小票接口  /transMessage
#### 1\. 发送交易小票接口
请求：  
```
POST /transMessage HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

amount: "11111"
terminalNo: "44444"
merchantNo: "2333"
batchNo: "12"
reqNo: "1111"
mobile: "13500001111"
origReqTime: "20151124111059"
appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
   "respTime":"20151125161740",
   "isSuccess":true,
   "respCode":"SUCCESS",
   "respMsg":"短信发送成功,注意查收"
}
```
##### [返回目录↑](#content-title)

<a id="queryTrans"></a>
### 查询接口  /queryTrans
#### 1\. 查询接口
请求：  
```
POST /queryTrans HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

date: "20151201"
respNo: "1111"
appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

respNo: "111"
merchantName: "啊啊啊啊"
merchantNo: "1111"
terminalNo: "1111"
transactions: 
{
    "reqNo": 645254,
    "transType": "sale",
    "voucherNo": 2222,//交易流水号
    "respCode": "00",
    "amount": 1234,
    "merchantNo": 111111111,
    "terminalNo": 22222,
    "currency": "CNY",    
    "immediatePay": false,    //是否是DO交易
    "transTime": 20151130125253,
    "refNo": 1234,
    "authNo": 1234    
    "cardTail": "6666",//卡号后四位    
    "cardNoWipe": 645***254,
    "operatorNo": 01,
    "issuer": "XX银行",
    "batchNo": 123
}，
{
    "reqNo": 645254,
    "transType": "sale",
    "voucherNo": 2222,//交易流水号
    "respCode": "00",
    "amount": 1234,
    "merchantNo": 111111111,
    "terminalNo": 22222,
    "currency": "CNY",    
    "immediatePay": false,    //是否是DO交易
    "transTime": 20151130125253,
    "refNo": 1234,
    "authNo": 1234    
    "cardTail": "6666",//卡号后四位    
    "cardNoWipe": 645***254,
    "operatorNo": 01,
    "issuer": "XX银行",
    "batchNo": 123

}
"respTime":"20151130125253",
"isSuccess":true,
"respCode":"SUCCESS",
"respMsg":"验证成功",

```
##### [返回目录↑](#content-title)
<a id="bankQuery"></a>
### 联行号查询  /bankQuery
#### 1\. 联行号查询
请求：  
```
GET /bankQuery HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

keyWord: "中国 银行"
reqNo: "111"
max: "111"
p: "111"
reqTime: "20151124111059"
appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "total": 100,
    "tip": "",
    "reqNo": 100,
    "banks": {[bankDeposit:"2222", unionBankNo: "333"],[bankDeposit:"33", unionBankNo: "444"]},
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"验证成功"
}
```

##### [返回目录↑](#content-title)
<a id="bankList"></a>
### 获取18家结算银行  /bankList
#### 1\. 获取18家结算银行
请求：  
```
GET /bankList HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

card : XXXXXX
reqTime: "20151124111059"
appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "list": {
    	[bankName:"2222", bankCode: "333", bankLogoIndex: "333", support: "true"],
    	[bankName:"4444", bankCode: "555", bankLogoIndex: "666", support: "true"]},
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"验证成功"
}
```
##### [返回目录↑](#content-title)

<a id="bindBankCard"></a>
### 绑定/解绑用户银行卡  /bindBankCard
#### 1\. 绑定/解绑用户银行卡
请求：  
```
POST /bindBankCard HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

isDelete: "false" //绑定、解绑参数
cardIds: "XXX YYYY"//解绑卡列表 空格区分
bankCard: "XXXX"
mobile: "13777775555"
reqTime: "20151124111059"
appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "bankCard": "XXXX",
    "bankName": "xx银行",
    "bankIndex": 11,
    "card_type": "借记卡",
    "bankAccountName": XX银行,
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"验证成功"
}
```
##### [返回目录↑](#content-title)

<a id="listBandCard"></a>
### 获取商户绑定银行卡列表  /listBandCard
#### 1\. 获取商户绑定银行卡列表
请求：  
```
POST /listBandCard HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
list:[
{
    "accountNo": "XXXX",
    "bankName": "xx银行",
    "bankIndex": 1,
    "status": 1,//认证状态
    "cardId": 1
},{
    "accountNo": "XXXX",
    "bankName": "xx银行",
    "bankIndex": 3,
    "status": 1,//认证状态
    "cardId": 2
}]
]
```
##### [返回目录↑](#content-title)
<a id="activeAndBindEquip"></a>
### 激活绑定设备  /activeAndBindEquip
#### 1\. 激活绑定设备
请求：  
```
POST /activeAndBindEquip HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

ksnNo: "5010100000023402"
activeCode: "11C718FF1FD14531"
product: "ZFT" //产品型号
model: "landim35" //设备型号
macAddress:"XX:XX:XX:XX"
appVersion: "ios.未知.1.1.813"
```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"激活绑定设备成功"
}
```
##### [返回目录↑](#content-title)
<a id="realNameAuth"></a>
### 实名认证  /realNameAuth
#### 1\. 实名认证
请求：  
```
POST /realNameAuth HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
name: "狗剩"
idNumber: "341225199005063896"
personal: 图片 //身份证正面照
personalBack: 图片 //身份证背面照
```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"实名认证信息已提交,请耐心等待"
}
```
##### [返回目录↑](#content-title)
<a id="realNameAuthStatus"></a>
### 实名认证信息回显  /realNameAuthStatus
#### 1\. 实名认证信息回显
请求：  
```
GET /realNameAuthStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "authStatus":(0:未认证, 1:认证成功, 2:认证失败, 3:审核中),
    "name":"张三",//真实姓名
    "idNumber":"341225199005063796",//身份证
    "personal":"d500000000620995.png",//身份证正面照图片名称
    "personalBack":"b500000000620995.png",//身份证背面照图片名称
    "realReason":"用户签名与身份证名字不符",//认证失败原因
    "idCard":"341225199005063796",//仅当为POS商户时返回
    "realName":"张三",仅当为POS商户时返回
    "respMsg":"查询成功"
}
```
##### [返回目录↑](#content-title)
<a id="merchantAuth"></a>
### 商户认证  /merchantAuth
#### 1\. 商户认证
请求：  
```
POST /merchantAuth HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
companyName: "企业名称"
regPlace: "经营地址"
businessLicense: "营业执照号"
business: 图片 //营业执照照片
```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"商户认证信息已提交,请耐心等待"
}
```
##### [返回目录↑](#content-title)
<a id="merchantAuthStatus"></a>
### 商户认证信息回显  /merchantAuthStatus
#### 1\. 商户认证信息回显
请求：  
```
GET /merchantAuthStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "authStatus":(0:未认证, 1:认证成功, 2:认证失败, 3:审核中),
    "companyName":"大众食品商店",//企业名称
    "business":"q500000000620995.png",//营业执照图片名称
    "regPlace":"兴华大道",//经营地址
    "businessLicense":"5DSF5SDFS5DF",//营业执照号
    "merchantReason":"商户不行",//认证失败原因
    "respMsg":"查询成功"
}
```
##### [返回目录↑](#content-title)
<a id="accountAuth"></a>
### 账户认证  /accountAuth
#### 1\. 账户认证
请求：  
```
POST /accountAuth HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
name: "账户名称"
bankName: "银行名称"
unionBankNo: "联行号"
accountNo: "银行卡号"
card: 图片 //银行卡图片
```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"账户认证信息已提交,请耐心等待"
}
```
##### [返回目录↑](#content-title)
<a id="accountAuthStatus"></a>
### 账户认证信息回显  /accountAuthStatus
#### 1\. 账户认证信息回显
请求：  
```
GET /accountAuthStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "authStatus":(0:未认证, 1:认证成功, 2:认证失败, 3:审核中),
    "accountNo":"6217000010012052348",//银行卡号
    "card":"c500000000620995.png",//银行卡图片名称
    "name":"张三",//账户名
    "bankName":"建设银行",//银行名称
    "unionBankNo":"56SDFSD56SDF",//联行号
    "accountReason":"账户信誉差",//认证失败原因
    "respMsg":"查询成功"
}
```
##### [返回目录↑](#content-title)
<a id="signatureAuth"></a>
### 签名认证  /signatureAuth
#### 1\. 签名认证
请求：  
```
POST /signatureAuth HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
signature: 图片 //签名图片
```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"签名认证信息已提交,请耐心等待"
}
```
##### [返回目录↑](#content-title)
<a id="signatureAuthStatus"></a>
### 签名认证信息回显  /signatureAuthStatus
#### 1\. 签名认证信息回显
请求：  
```
GET /signatureAuthStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```
响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "name":"张三",
    "authStatus":(0:未认证, 1:认证成功, 2:认证失败, 3:审核中),
    "signature":"s500000000620995.png",//签名图片名称
    "signatureReason":"签名丑",//认证失败原因
    "respMsg":"查询成功"
}
```
##### [返回目录↑](#content-title)
<a id="handIdCardAuth"></a>
### 及时付身份证认证  /handIdCardAuth
#### 1\. 及时付身份证认证
请求：  
```
POST /handIdCardAuth HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
idCard: 图片 //手持身份证图片
```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "isOldCard": true/false, //true：t1银行卡在十八家银行之内; false：不在
    "respMsg":"手持身份证半身照认证信息已提交,请耐心等待"
}
```
##### [返回目录↑](#content-title)
<a id="handIdCardAuthStatus"></a>
### 及时付身份证认证信息回显  /handIdCardAuthStatus
#### 1\. 及时付身份证认证信息回显
请求：  
```
GET /handIdCardAuthStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "authStatus":(0:未认证, 1:认证成功, 2:认证失败, 3:审核中),
    "idCard":"id500000000620995.png",//身份证图片名称
    "handIdCardReason":"人丑",//认证失败原因
    "respMsg":"查询成功"
}
```
##### [返回目录↑](#content-title)
<a id="dzAccountAuth"></a>
### 及时付账户认证  /dzAccountAuth
#### 1\. 及时付账户认证
请求：  
```
POST /dzAccountAuth HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
name："张三"
bankDeposit: "银行注册网点"
bankName: "开户行名称"
unionBankNo: "联行号"
accountNo: "银行卡号" 
bankCard: 图片 //银行卡图片
```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"账户认证信息已提交,请耐心等待"
}
```
##### [返回目录↑](#content-title)
<a id="dzAccountAuthStatus"></a>
### 及时付账户认证信息回显  /dzAccountAuthStatus
#### 1\. 及时付账户认证信息回显
请求：  
```
GET /dzAccountAuthStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "authStatus":(0:未认证, 1:认证成功, 2:认证失败, 3:审核中),
    "accountNo":"6217000010012052348",//银行卡号
    "card":"c500000000620995.png",//银行卡图片名称
    "name":"张三",//账户名
    "bankName":"建设银行",//银行名称
    "unionBankNo":"56SDFSD56SDF",//联行号
    "bankDeposit":"建设大街19号",//开户网点
    "accountReason":"账户信誉差",//认证失败原因
    "respMsg":"查询成功"
}
```
##### [返回目录↑](#content-title)
<a id="sale"></a>
### 消费  /sale
#### 1\. 消费
请求：  
```
POST /sale HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813",
ksnNo: "800090000004",
reqNo: "129",
position: "117.194778,39.113809",
currency: "CNY",
amount: "300",
cardSerialNum: "001",
icData: "XXXXXXXX",
encPinblock: "XXXXX",
encTracks: "TRACK2",
checksum:"XXX"

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "reqNo":"129",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "merchantName":"XXX",
    "merchantNo":"111",    
    "terminalNo":"22222",    
    "operatorNo":"01",    
    "resultCode":"00",    
    "cardNoWipe":"333**8233",    
    "amount":"300",    
    "currency":"CNY",    
    "voucherNo":"000130",   
    "batchNo":"001234",   
    "transTime":"20151212125959",   
    "refNo":"666666",
    "authNo":"666666777777",
    "script":"ic55"
}
```
##### [返回目录↑](#content-title)
<a id="query"></a>
### 余额查询  /query
#### 1\. 余额查询
请求：  
```
POST /query HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813",
ksnNo: "800090000004",
reqNo: "129",
cardSerialNum: "001",
icData: "XXXXXXXX",
encPinblock: "XXXXX",
encTracks: "TRACK2",
checksum:"XXX"

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100
{
    "reqNo":"129",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "balance":"XXX",
    "resultCode":"00",    
    "currency":"CNY",    
    "issuer":"XX银行",
    "cardNoWipe":"4444****888",
    "transTime":"20151212125959", 
    "script":"ic55"
}
```
##### [返回目录↑](#content-title)
<a id="authStatus"></a>
### 四审认证状态查询  /authStatus
#### 1\. 四审认证状态查询
请求：  
```
GET /authStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "status":"1111", //认证状态 (第一位：实名认证状态, 第二位：商户认证状态, 第三位：账户认证状态, 第四位：签名认证状态)
    "merchantReason":"商户信誉差",//商户认证失败原因
    "realReason":"用户信誉差",//实名认证失败原因
    "accountReason":"账户信誉差",//账户认证失败原因
    "signatureReason":"签名丑",//签名认证失败原因
    "respMsg":"查询成功"
}
```
##### [返回目录↑](#content-title)
<a id="dzAuthStatus"></a>
### 及时付认证状态查询  /dzAuthStatus
#### 1\. 及时付认证状态查询
请求：  
```
GET /dzAuthStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "status":"11", //认证状态 (第一位：手持身份证半身照认证状态, 第二位：账户认证状态)
    "handIdCardReason":"人丑",//手持身份证半身照认证失败原因
    "accountReason":"账户信誉差",//账户认证失败原因
    "respMsg":"查询成功"
}
```

##### [返回目录↑](#content-title)
<a id="downloadImg"></a>
### 认证图片下载  /downloadImg
#### 1\. 认证图片下载
请求：  
```
GET /downloadImg HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
fileName  : "c5000000000000000.png" //图片名称

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "file"：byte数组,//图片流
    "respMsg":"查询成功"
}
```

##### [返回目录↑](#content-title)
<a id="transNotify"></a>
### IC回调  /transNotify
#### 1\. IC回调
请求：  
```
POST /transNotify HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
reqNo:"1234"
origTransTime:"20151212070809"
origTransType:"sale"
origReqNo:"1234"
icData:"asfakfjasklfdsa"
cardNo:"622266000000"
cardSerialNum:"01"
```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"查询成功"
}
```
##### [返回目录↑](#content-title)
<a id="swiperChange"></a>
### 更换设备  /swiperChange
#### 1\. 更换设备
请求：  
```
POST /swiperChange HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
reqNo:"1234"
ksnNo:"XXXXXX"
model:"landim35"
macAddress:"XXXXXXX"
```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"查询成功"
}
```

##### [返回目录↑](#content-title)
<a id="showHtml"></a>
### 静态页面显示  /showHtml
#### 1\. 静态页面显示
请求：  
```
POST /showHtml HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

html: "XXX.html"

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
	HTML页面
}
```
##### [返回目录↑](#content-title)

<a id="showProtocol"></a>
### 需要登录页面显示  /showProtocol
#### 1\. 需要登录页面显示
请求：  
```
GET /showProtocol HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

view: "XXX" //offline_product_protocol

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
	HTML页面
}
```
##### [返回目录↑](#content-title)

<a id="message"></a>
### 获取消息接口/更新消息状态  /message
#### 1\. 获取消息接口/更新消息状态
请求：  
```
GET /message HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

messageId: "56711f5b84aea1954cade27b"
detail: false

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
     
    "isSuccess":true,
    "head":{"hasUnRead":false,"totalCount":0,"readCount":0,"unReadCount":0} 
    "body":{"des":
		[
		{"businessType":"2","content":"消息内容2","createTime":1450254226000,"hasLink":"0","isRead":1,"linkAddress":"www.sohu.com","linkText":"搜狐","newsId":"56711f8184aea1954cade27c","newsType":"0","readTime":1450768873990,"title":"测试2"},
		{"businessType":"1,2","content":"消息内容1","createTime":1450254187000,"hasLink":"0","isRead":0,"linkAddress":"www.sina.com","linkText":"新浪","newsId":"56711f5b84aea1954cade27b","newsType":"0","title":"测试1"}
		]
		},
		
    "respTime":"20151130125253",
    "isSuccess":true,
    "respCode":"SUCCESS",
    "respMsg":"查询成功"
}
```

##### [返回目录↑](#content-title)

<a id="banner"></a>
### 获取广告位信息  /banner
#### 1\. 获取广告位信息
请求：  
```
GET /message HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion : ios.ZFT.1.1.813

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    "respTime": "20160115104632", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "head": {
        "total": "4"
    }, 
    "body": [
        {
            "title": "广告位1", 
            "message": "广告信息1", 
            "imageUrl": "http://image.21er.tk/1.jpg",  //图片路径
            "targetUrl": "http://image.21er.tk/11.jpg" //图片跳转的路径
        }, 
        {
            "title": "广告位2", 
            "message": "广告信息2", 
            "imageUrl": "http://image.21er.tk/2.jpg", 
            "targetUrl": "http://image.21er.tk/21.jpg"
        }, 
        {
            "title": "广告位3", 
            "message": "广告信息3", 
            "imageUrl": "http://image.21er.tk/3.jpg", 
            "targetUrl": "http://image.21er.tk/31.jpg"
        }, 
        {
            "title": "广告位4", 
            "message": "广告信息4", 
            "imageUrl": "http://image.21er.tk/4.jpg", 
            "targetUrl": "http://image.21er.tk/41.jpg"
        }
    ]
}
```

##### [返回目录↑](#content-title)
<a id="downloadBanner"></a>
### 广告位图片下载  /downloadBanner
#### 1\. 广告位图片下载
请求：  
```
GET /downloadImg HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
fileName  : "b500000000620994.png" //图片名称

```

响应： 

```
HTTP/1.1 200 OK
Server: Nginx
Date: Thu, 09 Apr 2015 11:36:53 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Cache-Control: no-cache
Content-Length: 100

{
    字节流
}
```

