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
http://mposp.21er.tk:29008 # 测试环境
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
Cookie: "***********"
WSSESSION: ""//session信息
isPosMerchant: false, //是否为POS商户
posStatus: 0 //POS认证状态 (0未绑定 ,1待刷卡，2待认证,3实名认证通过)
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
| 获取验证码| [/sendMobileMessage.action](#sendMobileMessage)                    | urlencoded           | POST   | 张树彬     | 否   |
| 校验短信验证码| [/checkMobileMessage.action](#checkMobileMessage)            | urlencoded           | POST   | 张树彬     | 否   |
| 注册| [/register.action](#register)                      | urlencoded           | POST   |  张树彬     | 否   |
| 登录| [/login.action](#login)                      | urlencoded           | POST      | 张树彬     | 否   |
| 找回密码| [/forgetPassword.action](#forgetPassword)              | urlencoded           | POST      | 张树彬     | 否   |
| 重置密码| [/resetPassword.action](#resetPassword)              | urlencoded           | POST      | 张树彬     | 是   |
| 修改密码| [/updatePassword.action](#updatePassword)                      | urlencoded           | POST      | 张树彬     | 是   |
| 退出登录| [/logout.action](#logout)                      | urlencoded           | POST      | 张树彬     | 是   |
| 获取商户交易列表| [/queryTransList.action](#queryTransList)                      | urlencoded           | GET |李飞| 是   |
| 获取商户名列表| [/listMerchant.action](#listMerchant)                      | urlencoded           | GET |李飞| 是   |
| 获取商户交易详细信息|[/queryTransInfo.action](#queryTransInfo)                      | urlencoded           | GET |李飞| 是   |
| 获取已交易金额|[/getCurrentAmount.action](#getCurrentAmount)                      | urlencoded           | GET |李飞| 是   |
| 获取已绑定POS商户列表|[/getBindingPosMerchantList.action](#getBindingPosMerchantList)  | urlencoded    | GET |李飞| 是   |
| 是否有新消息|[/isNewMessage.action](#isNewMessage)                      | urlencoded           | GET |张树彬| 是   |
| 获取消息列表/变更消息阅读状态|[/message.action](#message)                      | urlencoded           | GET |张树彬| 是   |
| 变更手机全部消息阅读状态|[/messageReaded.action](#messageReaded)                      | urlencoded           | POST |张树彬| 是   |
| 获取结算列表|[/settleList.action](#settleList)                      | urlencoded           | GET |张树彬| 是   |
| 获取结算详情|[/settleInfo.action](#settleInfo)                      | urlencoded           | GET |张树彬| 是   |
| 获取广告信息|[/banner.action](#banner)                      | urlencoded           | GET |张树彬| 否   |
| 图片下载 | [/downloadImg.action](#downloadImg)                      | urlencoded           | GET   | 张树彬     | 否   |
| 获取活动列表|[/campaign.action](#campaign)                      | urlencoded           | GET |张树彬| 是   |
| 静态页面显示 | [/showHtml.action](#showHtml)                      | urlencoded           | GET   | 张树彬     | 否   |
| 获取未绑定手刷设备的POS商户列表 | [/unbindTerminalPosMerchant.action](#unbindTerminalPosMerchant)| urlencoded | GET  | 张树彬 |是|
| 获取已绑定手刷的商户设备信息 | [/bindMerchantTerminalInfo.action](#bindMerchantTerminalInfo)| urlencoded | GET  | 张树彬 |是|
| 获取用户已绑和未绑的附属信息 | [/userSatelliteInfo.action](#userSatelliteInfo)| urlencoded | GET  | 张树彬 |是|
| 获取终端IC公钥 | [/getTerminalICKey.action](#getTerminalICKey)| urlencoded | GET  | 张树彬 |是|
| 签到 | [/signIn.action](#signIn)| urlencoded | GET  | 张树彬 |是|
| 更新终端IC公钥状态 | [/updateTerminalICKeyStatus.action](#updateTerminalICKeyStatus)| urlencoded | POST  | 张树彬 |是|
| 绑定手刷设备 | [/activeAndBindEquip.action](#activeAndBindEquip)| urlencoded | POST  | 张树彬 |是|
| 发送交易小票 | [/transMessage.action](#transMessage)| urlencoded | POST  | 张树彬 |是|
| 消费 | [/sale.action](#sale)                      | urlencoded           | POST   | 李飞     | 是   |
| 查询交易状态| [/transStatus.action](#transStatus)                      | urlencoded           | POST   | 李飞     | 是   |
| IC回调 | [/transNotify.action](#transNotify)                      | urlencoded           | POST   | 李飞     | 是   |
| 获取强制更新的参数 | [/getForceUpdate.action](#getForceUpdate)    | urlencoded           | GET   | 李飞     | 否   |
| 获取回单商户列表 | [/listMerchant.action](#listMerchant)       | urlencoded           | GET   | 李飞     | 是   |
| 获取回单信息列表 | [/listReceipts.action](#listReceipts)    | urlencoded           | GET   | 李飞     | 是   |
| 回单 | [/receipt.action](#receipt)    | urlencoded           | POST   | 张树彬     | 是   |
| 获取回单详情 | [/receiptInfo.action](#receiptInfo)    | urlencoded           | GET   | 张树彬     | 是   |
| 获取商户信息 | [/getMerchantInfo.action](#getMerchantInfo)    | urlencoded   | POST   | 张攀攀     | 是   |
| 商户四要素认证和匹配 | [/authenticationMerchant.action](#authenticationMerchant)    | urlencoded  | POST   | 张攀攀     | 是   |

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

appVersion: "android.ZFT.1.2.143"
mobile: "15801376995"
type: "forget" //"找回密码":"forget", "重置密码":"reset", "注册":"register"
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
<a id="checkMobileMessage"></a>
### 校验短信验证码  /checkMobileMessage
#### 1\. 通过手机号校验短信验证码
请求：  
```
POST /checkMobileMessage HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "android.ZFT.1.2.143"
mobile: "15801376995"
idCode: "8764" //验证码
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
   "respMsg":"验证成功"
   
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
appVersion: "android.ZFT.1.2.143"

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
"registId": "101d855909475902a45"
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
    --"bindPosMerchantNum": 11,//已绑定的POS商户数量//已移除
    --"bindTerminalNum": 0,//已绑定成功的手刷终端数量//已移除
    --"passTerminalNum": 0//手刷设备增机申请成功的终端数量//已移除
    "posStatus": 0 //POS认证状态 (0未绑定 ,1待刷卡，2待认证,3实名认证通过)
    --"idCard":"341225199005063894", //身份证号(当为POS商户时,必返)//已移除
    --"realName":"张树彬",//真实姓名(当为POS商户时,必返)//已移除
}
```

##### [返回目录↑](#content-title)
<a id="forgetPassword"></a>
### 找回密码  /forgetPassword
#### 1\. 通过手机号找回密码
请求：  
```
POST /forgetPassword HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"password": "qqqqqq"
"appVersion": "android.ZFT.1.2.143"
"mobile": "15801376995"

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
    "respMsg": "密码变更成功"
}
```

##### [返回目录↑](#content-title)
<a id="resetPassword"></a>
### 重置密码  /resetPassword
#### 1\. 通过手机号重置密码
请求：  
```
POST /resetPassword HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"password": "qqqqqq"
"appVersion": "android.ZFT.1.2.143"

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
    "respMsg": "密码变更成功"
}
```

##### [返回目录↑](#content-title)
<a id="updatePassword"></a>
### 修改密码  /updatePassword
#### 1\. 修改密码
请求：  
```
POST /updatePassword HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"
"oldPasswd": "qqqqqq" //原密码
"passwd": "qqqqqq" //新密码

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
    "respMsg": "密码变更成功,请重新登陆"
}
```

##### [返回目录↑](#content-title)
<a id="logout"></a>
### 退出登录  /logout
#### 1\. 退出登录
请求：  
```
POST /logout HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"

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
    "respMsg": "退出成功"
}
```
##### [返回目录↑](#content-title)


<a id="listMerchant"></a>
### 获取商户名列表  /listMerchant
#### 1\. 获取商户名列表
请求：  
```
GET /listMerchant HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",
"type":1, --商户类型  1： pos  2：vcpos

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
    "respMsg": "成功",
    "hasPosMerchant":true,  
    "merchantList": [ 
      {
        "merchantId": 676453,--商户ID
        "merchantNo": "500000000621891",--商户编码
	"merchantName": "小丸子3" 
      },
      ....
     ]
}
```
##### [返回目录↑](#content-title)



<a id="queryTransList"></a>
### 获取商户交易列表  /queryTransList
#### 1\. 获取商户交易列表
请求：  
```
GET /queryTransList HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",
"merchantNo": "500000000621891", --商户编码
"lastID": "", --上次请求最后一笔交易的ID
"startTime": "2016-06-06", --起始时间 yyyy-MM-dd格式
"endTime": "2016-06-06", --结束时间 yyyy-MM-dd格式 
"type":2, --商户类型  1： pos  2：vcpos

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
    "respMsg": "成功",
    "count": 134,
    "amount": 13684228,
    "tranList": [    
      {
        "tranid": 676453,--交易id
        "transType": "sale",--交易类型 sale-消费/sale_void-撤销/auth_comp-预授权完成/auth_comp_cancel-预授权完成撤销/refund-退货 
        "transTime": "2016-05-15 15:56:25",--交易时间
        "amount": 100 --交易金额(分)
      },
    ...
    ]
}
```
##### [返回目录↑](#content-title)

<a id="getCurrentAmount"></a>
### 获取已交易金额  /getCurrentAmount
#### 1\. 获取已交易金额
请求：  
```
GET /getCurrentAmount HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",

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
    "respMsg": "成功",
    "currentAmount": 50010, //金额（分）
}
```

##### [返回目录↑](#content-title)

<a id="queryTransInfo"></a>
### 获取商户交易详细信息  /queryTransInfo
#### 1\. 获取商户交易详细信息
请求：  
```
GET /queryTransInfo HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",
"transId": "", --交易ID
"type":2, --商户类型  1： pos  2：vcpos

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
    "respMsg": "成功",
	"merchantNo": "500000000621891",//商户编号
	"merchantName": "xxx",//商户名称
	"transTime": "2016-05-15 15:56:25",//交易时间
	"batchNo": "000001",//交易批次
	"voucherNo": "000001",//交易流水号
	"terminalNo": "XXXXX",//交易终端号
	"cardNoWipe": "62226******5655",//带星号卡号
	"transType": "sale",//交易类型 -- sale-消费/sale_void-撤销/auth_comp-预授权完成/auth_comp_cancel-预授权完成撤销/refund-退货 
	"transStatus": 1,//交易状态 -- 0未知/1正常/2已冲正/3已撤销/4已退款
	"amount": 11111,//交易金额(分)
    
}
```

##### [返回目录↑](#content-title)

<a id="getBindingPosMerchantList"></a>
### 获取已绑定POS商户列表  /getBindingPosMerchantList
#### 1\. 获取已绑定POS商户列表
请求：  
```
GET /getBindingPosMerchantList HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",
"lastID": "" //最后一条绑定记录ID,

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
    "respMsg": "成功",
    "bindingCount": 13,
    "bindingList": [
    {
    	    "recordId": "1007",
            "merchantName": "郑******",
            "merchantNo": "Z08000000026875",
            "status": 2,//pos机验证状态 1,等待认证，2认证成功，3认证失败，4解绑
            "urlPath": "appMerchant/posInfoValidation.htm?id=1007"
    }
    ...
    ]
}
```

##### [返回目录↑](#content-title)

<a id="isNewMessage"></a>
###  是否有新消息 /isNewMessage
#### 1\. 是否有新消息
请求：  
```
GET /isNewMessage HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"

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
    "respMsg": "成功",
    "isNewMessage": true //是否有新消息(true:有, false:无)
}
```

##### [返回目录↑](#content-title)

<a id="message"></a>
###  获取消息列表/变更消息阅读状态 /message
#### 1\. 获取消息列表/变更消息阅读状态
请求：  
```
GET /message HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"

//获取消息列表
"detail": true,//获取消息

//获取消息头
"detail": false,//获取消息

//变更消息阅读状态
"messageId": "5753d725737f247007d596db"//消息id

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

//获取消息列表
{
    "respTime": "20160606142841", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "body": [
        {
            "content": "就问你怕不怕！！！", //消息内容
            "linkAddress": "", //链接地址
            "title": "商户通内部测试", //消息标题
            "newsId": "575513d784aeddcc333a7a10",//消息id 
            "linkText": "", //链接地址文本
            "hasLink": false, //是否有链接
            "createTimeStr": "20160606140803",//创建时间 
            "businessType": "1", //消息所属业务(0：理财, 1：vcPos, 2：Pos)
            "newsType": "0", //消息类型(0：公告, 1：通知)
            "isRead": 0//是否已阅读(0:"否", 1:"是")
        }, 
        {
            "content": "通知测试之所有", 
            "linkAddress": "", 
            "title": "通知测试之所有", 
            "newsId": "5753d725737f247007d596db", 
            "linkText": "", 
            "hasLink": false, 
            "createTimeStr": "20160605153917", 
            "readTimeStr": "20160605162312", 
            "businessType": "1", 
            "newsType": "1", 
            "isRead": 1
        }, 
        {
            "content": "通知测试之安卓", 
            "linkAddress": "", 
            "title": "通知测试之安卓", 
            "newsId": "5753d711737f247007d596da", 
            "linkText": "", 
            "hasLink": false, 
            "createTimeStr": "20160605153857", 
            "businessType": "1", 
            "newsType": "1", 
            "isRead": 0
        }
    ], 
    "head": {
        "hasUnRead": true, //是否有未读消息
        "unReadNotice": false,//是否有未读通知
        "unReadBulletin": false,//是否有未读公告
        "totalCount": 3, //消息总条数
        "readCount": 1, //已阅读数
        "unReadCount": 2//未阅读数
    }
}

//获取消息头
{
    "respTime": "20160606145626", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "head": {
        "hasUnRead": true, 
        "totalCount": 3, 
        "readCount": 1, 
        "unReadCount": 2
    }
}

//变更消息阅读状态
{
    "respTime": "20160606145829", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "des": "状态修改成功!", 
    "messageId": "575513d784aeddcc333a7a10"
}
```

##### [返回目录↑](#content-title)

<a id="messageReaded"></a>
###  变更手机全部消息阅读状态 /messageReaded
#### 1\. 通过手机号变更全部消息的阅读状态
请求：  
```
POST /messageReaded HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"

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
    "respMsg": "成功",
    "isUpdateComplete": true //是否变更完成(true:是, false:否)
}
```

##### [返回目录↑](#content-title)

<a id="settleList"></a>
###  获取结算列表 /settleList
#### 1\. 获取结算列表
请求：  
```
GET /settleList HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"
"merchantNo": "500000000876550",//商户号
"startTime": "2016-3-14",//起始时间
"endTime": "2016-3-16",//结束时间
"type": 2,//查询类型(1:POS, 2:VCPOS)
"uniqueRecord":"10790-6228480010970642611-995100-d0"//最后一条记录的唯一标识(非必传项)

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
    "respTime": "20160612174733", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "settleList": [
        {
            "sid": 10794, //结算id
            "status": 2, //结算状态(1:成功, 2:失败)
            "transAmount": 29853, //交易金额(单位:分)
            "settleMoney": 29853, //结算金额(单位:分)
            "accountNum": "6228480010970642611", //结算账户 
            "merchantType": "d0", //结算类型
            "merchantName": "旧数据企业", //商户名称
            "merchantNo": "500000000876552", //商户号
            "settleDate": "2016-03-15", //结算日期
            "uniqueRecord": "10794-6228480010970642611-298530-d0"//唯一标识
        }, 
        {
            "sid": 10793, 
            "status": 2, 
            "transAmount": 89559, 
            "settleMoney": 89559, 
            "accountNum": "6228480010970642611", 
            "merchantType": "d0", 
            "merchantName": "旧数据企业", 
            "merchantNo": "500000000876552", 
            "settleDate": "2016-03-15", 
            "uniqueRecord": "10793-6228480010970642611-895590-d0"
        }
    ], 
    "settleCount": 7, //总条数(下拉刷新才会返回)
    "settleAmount": 6413673//结算总额(单位:分)(下拉刷新才会返回)
}

```

##### [返回目录↑](#content-title)

<a id="settleInfo"></a>
### 获取结算详细信息  /settleInfo
#### 1\. 获取结算详细信息
请求：  
```
GET /settleInfo HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",
"sid": "", //结算ID
"merchantType":d0, //商户类型  d0:D0商户 tn:TN商户

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
    "respMsg": "成功",
    "status": "已付款", //付款状态
    "transAmount": 29853, //交易金额(单位:分)
    "settleMoney": 29853, //结算金额(单位:分)
    "accountNum": "6228480010970642611", //结算账户 
    "merchantName": "旧数据企业", //商户名称
    "merchantNo": "500000000876552", //商户号
    "settleDate": "2016-03-15", //结算日期
}
```

##### [返回目录↑](#content-title)

<a id="banner"></a>
### 获取广告信息  /banner
#### 1\. 获取广告信息
请求：  
```
GET /banner HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"

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
    "respTime": "20160616114625", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "body": [
        {
            "title": "广告2", //广告名称
            "imageUrl": "http://localhost:29008/downloadBanner.action?fileName=main_home_ad_2.png&appVersion=ios.ZFT.1.2.143&type=banner", //图片路径
            "targetUrl": "www.baidu.com"//跳转地址
        }
    ], 
    "head": {
        "total": 1//广告总条数
    }
}
```

##### [返回目录↑](#content-title)

<a id="downloadImg"></a>
### 图片下载  /downloadImg
#### 1\. 图片下载
请求：  
```
GET /downloadImg HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

appVersion: "ios.未知.1.1.813"
fileName  : "b500000000620994.png" //图片名称
type	  : "advertisement"//获取类型(BANNER:"广告", CAMPAIGN:"活动", TRANS_RISK:("签购单","台账"))
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

##### [返回目录↑](#content-title)

<a id="campaign"></a>
###  获取活动列表 /campaign
#### 1\. 获取活动列表
请求：  
```
GET /campaign HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"
"campaignCount": "5"//每页显示的条数(非必传项)
"lastId":"54"//最后一条活动的id(非必传项)

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
    "respTime": "20160616194028", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "campaignSumCount": 6, //活动总条数(下拉刷新才会返回)
    "campaignList": [
        {
            "cid": 58, //活动id
            "title": "活动5", //活动标题
            "imageUrl": "http://192.168.1.240:29008/downloadBanner.action?fileName=201606/58/picIos.jpg&appVersion=ios.ZFT.1.2.143&type=campaign", //图片路径
            "linkText": "百度", //链接文字
            "targetUrl": "https://www.baidu.com",//跳转地址
            "linkContent": "活动5"//活动内容
            
        }, 
        {
            "cid": 57, 
            "title": "活动4", 
            "imageUrl": "http://192.168.1.240:29008/downloadBanner.action?fileName=201606/57/picIos.jpg&appVersion=ios.ZFT.1.2.143&type=campaign", 
            "linkText": "百度", 
            "targetUrl": "https://www.baidu.com",
            "linkContent": "活动5"//活动内容
        }, 
        {
            "cid": 56, 
            "title": "公共活动3", 
            "imageUrl": "http://192.168.1.240:29008/downloadBanner.action?fileName=201606/56/picIos.jpg&appVersion=ios.ZFT.1.2.143&type=campaign", 
            "linkText": "百度", 
            "targetUrl": "https://www.baidu.com",
            "linkContent": "活动5"//活动内容
        }, 
        {
            "cid": 55, 
            "title": "活动2", 
            "imageUrl": "http://192.168.1.240:29008/downloadBanner.action?fileName=201606/55/picIos.jpg&appVersion=ios.ZFT.1.2.143&type=campaign", 
            "linkText": "百度", 
            "targetUrl": "https://www.baidu.com",
            "linkContent": "活动5"//活动内容
        }, 
        {
            "cid": 54, 
            "title": "公共活动1", 
            "imageUrl": "http://192.168.1.240:29008/downloadBanner.action?fileName=201606/54/picIos.jpg&appVersion=ios.ZFT.1.2.143&type=campaign", 
            "linkText": "百度", 
            "targetUrl": "https://www.baidu.com",
            "linkContent": "活动5"//活动内容
        }, 
        {
            "cid": 53, 
            "title": "就问你怕不怕", 
            "imageUrl": "http://192.168.1.240:29008/downloadBanner.action?fileName=201606/53/picIos.jpg&appVersion=ios.ZFT.1.2.143&type=campaign", 
            "linkText": "详情猛戳", 
            "targetUrl": "https://www.baidu.com",
            "linkContent": "活动5"//活动内容
        }
    ]
}

```

##### [返回目录↑](#content-title)

<a id="showHtml"></a>
### 静态页面显示  /showHtml
#### 1\. 静态页面显示
请求：  
```
GET /showHtml HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

html: "XXX.html"//请求的静态页面(agreement.html:移动支付业务服务协议, register-agreement.html:中汇商户通用户协议)

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

<a id="unbindTerminalPosMerchant"></a>
### 获取未绑定手刷设备的POS商户列表  /unbindTerminalPosMerchant
#### 1\. 获取未绑定手刷设备的POS商户列表
请求：  
```
GET /unbindTerminalPosMerchant HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"

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

//请求失败
{
    "respTime": "20160726102050", 
    "isSuccess": false, 
    "respCode": "UNBIND_POS_MERCHANT", //1.未绑定POS商户
    "respCode": "UNADD_TERMINAL", //2.未增机
    "respCode": "ALL_ADD_TERMINAL_USED", //3.增机名额已全部被使用
    "respMsg": "..."
}

//请求成功
{
    "respTime": "20160726103914", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "unBindPosMerchant": [ //未绑定设备的POS商户列表
        {
            "posMerchantId": 675151,  //POS商户ID
            "merchantNo": "Z08000000620593", //商户号
            "merchantName": "六******",  //商户名称
            "rateInfos": [ //结算模式信息
                {
                    "mId": 5,  //增机主表ID
                    "settlementInterval": "T+1",//结算模式 
                    "availableBindNum": 2 //剩余可绑数量
                }, 
                {
                    "mId": 4, 
                    "settlementInterval": "T+1", 
                    "availableBindNum": 2
                }
            ]
        }, 
        {
            "posMerchantId": 675292, 
            "merchantNo": "Z08000000620776", 
            "merchantName": "三******", 
            "rateInfos": [
                {
                    "mId": 3, 
                    "settlementInterval": "T+1", 
                    "availableBindNum": 2
                }
            ]
        }
    ]
}
```

##### [返回目录↑](#content-title)

<a id="bindMerchantTerminalInfo"></a>
### 获取已绑定手刷的商户设备信息  /bindMerchantTerminalInfo
#### 1\. 获取已绑定手刷的商户设备信息
请求：  
```
GET /bindMerchantTerminalInfo HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"

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

//请求失败
{
    "respTime": "20160726102050", 
    "isSuccess": false, 
    "respCode": "UNBIND_POS_MERCHANT", //1.未绑定POS商户
    "respCode": "UNBIND_TERMINAL", //2.收款需要绑定刷卡设备配合使用，立即去绑定吧！
    "respMsg": "..."
}

//请求成功
{
    "respTime": "20160729202905", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "bindTerminalInfo": [
        {
            "mId": 765782, //商户Id
            "merchantName": "掌富通",//商户名称 
            "merchantNo": "500000000720172", //商户号
            "ksnNo": "7000100000008177", //ksn号
            "bluetoothName": "AC079158", //蓝牙名称
            "macAddress": "8C:DE:52:C3:51:0D", //mac地址
            "model":"hz-m20"//终端型号
        },
        ...
    ]
}
```

##### [返回目录↑](#content-title)

<a id="userSatelliteInfo"></a>
### 获取用户已绑和未绑的附属信息  /userSatelliteInfo
#### 1\. 获取用户已绑和未绑的附属信息
请求：  
```
GET /userSatelliteInfo HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"

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
    "respTime": "20160729202905", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "bindPosMerchantNum": 11,//绑定的POS商户数量
    "bindTerminalNum": 0,//绑定成功的终端数量
    "passTerminalNum": 0//增机申请成功的数量
    "bindTerminalInfo": [ //已绑设备的信息
        {
            "mId": 765782, //商户Id
            "merchantName": "掌富通",//商户名称 
            "merchantNo": "500000000720172", //商户号
            "ksnNo": "7000100000008177", //ksn号
            "bluetoothName": "AC079158", //蓝牙名称
            "macAddress": "8C:DE:52:C3:51:0D" //mac地址
        },
        ...
    ],
    "unBindPosMerchant": [ //未绑定设备的POS商户列表
        {
            "posMerchantId": 675151,  //POS商户ID
            "merchantNo": "Z08000000620593", //商户号
            "merchantName": "六******",  //商户名称
            "rateInfos": [ //结算模式信息
                {
                    "mId": 5,  //增机主表ID
                    "settlementInterval": "T+1",//结算模式 
                    "availableBindNum": 2 //剩余可绑数量
                }, 
                {
                    "mId": 4, 
                    "settlementInterval": "T+1", 
                    "availableBindNum": 2
                }
            ]
        }, 
        {
            "posMerchantId": 675292, 
            "merchantNo": "Z08000000620776", 
            "merchantName": "三******", 
            "rateInfos": [
                {
                    "mId": 3, 
                    "settlementInterval": "T+1", 
                    "availableBindNum": 2
                }
            ]
        }
    ]
}
```

##### [返回目录↑](#content-title)

<a id="getTerminalICKey"></a>
### 获取终端IC公钥  /getTerminalICKey
#### 1\. 获取终端IC公钥
请求：  
```
GET /getTerminalICKey HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",

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
    "respTime": "20160830110207", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "icKey": {
        "rids": {
            "A00000006514": "9F0605A0000000659F220114DF050420221231DF060101DF070101DF0281F8AEED55B9EE00E1ECEB045F61D2DA9A66AB637B43FB5CDBDB22A2FBB25BE061E937E38244EE5132F530144A3F268907D8FD648863F5A96FED7E42089E93457ADC0E1BC89C58A0DB72675FBC47FEE9FF33C16ADE6D341936B06B6A6F5EF6F66A4EDD981DF75DA8399C3053F430ECA342437C23AF423A211AC9F58EAF09B0F837DE9D86C7109DB1646561AA5AF0289AF5514AC64BC2D9D36A179BB8A7971E2BFA03A9E4B847FD3D63524D43A0E8003547B94A8A75E519DF3177D0A60BC0B4BAB1EA59A2CBB4D2D62354E926E9C7D3BE4181E81BA60F8285A896D17DA8C3242481B6C405769A39D547C74ED9FF95A70A796046B5EFF36682DC29DF040103DF0314C0D15F6CD957E491DB56DCDD1CA87A03EBE06B7B", 
            "A00000033304": "9F0605A0000003339F220104DF050420211231DF060101DF070101DF0281F8BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1DF040103DF0314F527081CF371DD7E1FD4FA414A665036E0F5E6E5", 
            "A00000033302": "9F0605A0000003339F220102DF050420211231DF060101DF070101DF028190A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57DF040103DF031403BB335A8549A03B87AB089D006F60852E4B8060", 
            "A00000000404": "9F0605A0000000049F220104DF050420171231DF060101DF070101DF028190A6DA428387A502D7DDFB7A74D3F412BE762627197B25435B7A81716A700157DDD06F7CC99D6CA28C2470527E2C03616B9C59217357C2674F583B3BA5C7DCF2838692D023E3562420B4615C439CA97C44DC9A249CFCE7B3BFB22F68228C3AF13329AA4A613CF8DD853502373D62E49AB256D2BC17120E54AEDCED6D96A4287ACC5C04677D4A5A320DB8BEE2F775E5FEC5DF040103DF0314381A035DA58B482EE2AF75F4C3F2CA469BA4AA6C", 
            "A00000033303": "9F0605A0000003339F220103DF050420211231DF060101DF070101DF0281B0B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33DDF040103DF031487F0CD7C0E86F38F89A66F8C47071A8B88586F26", 
            "A00000000406": "9F0605A0000000049F220106DF050420211231DF060101DF070101DF0281F8CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747FDF040103DF0314F910A1504D5FFB793D94F3B500765E1ABCAD72D9", 
            "A00000000405": "9F0605A0000000049F220105DF050420211231DF060101DF070101DF0281B0B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597DF040103DF0314EBFA0D5D06D8CE702DA3EAE890701D45E274C845", 
            "A00000006512": "9F0605A0000000659F220112DF050420221231DF060101DF070101DF0281B0ADF05CD4C5B490B087C3467B0F3043750438848461288BFEFD6198DD576DC3AD7A7CFA07DBA128C247A8EAB30DC3A30B02FCD7F1C8167965463626FEFF8AB1AA61A4B9AEF09EE12B009842A1ABA01ADB4A2B170668781EC92B60F605FD12B2B2A6F1FE734BE510F60DC5D189E401451B62B4E06851EC20EBFF4522AACC2E9CDC89BC5D8CDE5D633CFD77220FF6BBD4A9B441473CC3C6FEFC8D13E57C3DE97E1269FA19F655215B23563ED1D1860D8681DF040103DF0314874B379B7F607DC1CAF87A19E400B6A9E25163E8", 
            "A00000000307": "9F0605A0000000039F220107DF050420171231DF060101DF070101DF028190A89F25A56FA6DA258C8CA8B40427D927B4A1EB4D7EA326BBB12F97DED70AE5E4480FC9C5E8A972177110A1CC318D06D2F8F5C4844AC5FA79A4DC470BB11ED635699C17081B90F1B984F12E92C1C529276D8AF8EC7F28492097D8CD5BECEA16FE4088F6CFAB4A1B42328A1B996F9278B0B7E3311CA5EF856C2F888474B83612A82E4E00D0CD4069A6783140433D50725FDF040103DF0314B4BC56CC4E88324932CBC643D6898F6FE593B172", 
            "A00000006510": "9F0605A0000000659F220110DF050420221231DF060101DF070101DF02819099B63464EE0B4957E4FD23BF923D12B61469B8FFF8814346B2ED6A780F8988EA9CF0433BC1E655F05EFA66D0C98098F25B659D7A25B8478A36E489760D071F54CDF7416948ED733D816349DA2AADDA227EE45936203CBF628CD033AABA5E5A6E4AE37FBACB4611B4113ED427529C636F6C3304F8ABDD6D9AD660516AE87F7F2DDF1D2FA44C164727E56BBC9BA23C0285DF040103DF0314C75E5210CBE6E8F0594A0F1911B07418CADB5BAB", 
            "A00000000308": "9F0605A0000000039F220108DF050420231231DF060101DF070101DF0281B0D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0BDF040103DF031420D213126955DE205ADC2FD2822BD22DE21CF9A8", 
            "A00000000309": "9F0605A0000000039F220109DF050420231231DF060101DF070101DF0281F89D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41DF040103DF03141FF80A40173F52D7D27E0F26A146A1C8CCB29046"
        }, 
        "aids": {
            "A000000003101001": "9F0608A000000003101001DF0101009F0802008C9F0902008CDF1105FC70A82000DF1205DC40A8F800DF150400000010DF130500100000009F1B0400000500DF160190DF170110DF14039F3704DF180100", 
            "A000000003101002": "9F0608A000000003101002DF0101009F0802008C9F0902008CDF1105FC70A82000DF1205DC40A8F800DF150400000010DF130500100000009F1B0400000500DF160190DF170110DF14039F3704DF180100", 
            "A0000003330101": "9F0607A0000003330101DF0101009F080200209F09020020DF1105D8689CF800DF1205D8689CF800DF150400000010DF130500100000009F1B0400000500DF160190DF170110DF14039F3704DF180101", 
            "A0000000031020": "9F0607A0000000031020DF0101009F0802008C9F0902008CDF1105FC70A82000DF1205DC40A8F800DF150400000010DF130500100000009F1B0400000500DF160190DF170110DF14039F3704DF180100"
        }
    }
}
```

##### [返回目录↑](#content-title)

<a id="signIn"></a>
### 签到  /signIn
#### 1\. 签到
请求：  
```
GET /signIn HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",
"ksnNo": "600003387718",

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
    "respTime": "20160830112257", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功", 
    "traceNo": 20 ,
    "terminalWorkKeyInfo": {
        "keyType": "3DES", 
        "keyValue": "F5CE03DF66C3792D00B3F5B2577DE804", 
        "checkValue": "56DCB5B7", 
        "isBluetooth": true, 
        "macAddress": "8C:DE:52:3C:8F:C8", 
        "ksnNo": "600003387718"
    }, 
    "isUpdateICKey": false
}
```

##### [返回目录↑](#content-title)

<a id="updateTerminalICKeyStatus"></a>
### 更新终端IC公钥状态  /updateTerminalICKeyStatus
#### 1\. 更新终端IC公钥状态
请求：  
```
POST /updateTerminalICKeyStatus HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",
"ksnNo": "600003387718",

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
    "respTime": "20160830113902", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "成功"
}
```

##### [返回目录↑](#content-title)

<a id="activeAndBindEquip"></a>
### 绑定手刷设备  /activeAndBindEquip
#### 1\. 绑定手刷设备
请求：  
```
POST /activeAndBindEquip HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",
"ksnNo": "600003387718", //设备号
"model": "landim35", //终端型号
"macAddress:"8C:DE:52:3C:8F:C8", //设备MAC地址
"mId": 13 //增机主表Id

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
    "respTime": "20160830113902", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "激活绑定设备成功"
}
```

##### [返回目录↑](#content-title)

<a id="transMessage"></a>
### 发送交易小票  /transMessage
#### 1\. 发送交易小票
请求：  
```
POST /transMessage HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143",
"reqNo": "000064", //交易流水号
"amount": 10000, //交易金额(单位:分)
"terminalNo: "33365845", //终端号
"merchantNo" :"Z08224546146546",//商户号
"batchNo": "000001", //批次号
"mobile": "18611111117"//短信接收者的手机号

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
    "respTime": "20160830113902", 
    "isSuccess": true, 
    "respCode": "SUCCESS", 
    "respMsg": "交易小票短信发送成功,注意查收"
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
merhantNo:'Z08000000026875',
position: "117.194778,39.113809",
currency: "CNY",
amount: "300",//单位分
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
merchantNo: "111111111"
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
merchantNo:"111111111"
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


<a id="getForceUpdate"></a>
### 获取强制更新的参数  /getForceUpdate
#### 1\. 获取强制更新的参数
请求：  
```
GET /getForceUpdate HTTP/1.1
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
    "minVersion":'1.2.126',//IOS最低版本
    "respTime":"20151130125253",
    "isSuccess":true,
}
```
##### [返回目录↑](#content-title)


<a id="listReceipts"></a>
### 获取回单信息列表  /listReceipts
#### 1\. 获取回单信息列表
请求：  
```
GET /listReceipts HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"
"merchantNo":"Z08111111111111" //商户编号
"startTime": "2016-3-14",//起始时间
"endTime": "2016-3-16",//结束时间
"receiptStatus":0 //回单状态(0:全部, 2:待回单, 3:待审核, 4:已完成, 5:已关闭)
"lastId":"54"//最后一条回单的id(非必传项)
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
    "receiptCount": "3"// 列表条数
    "receiptList":[ //回单列表
        {
            "receiptId": 1112321, //回单Id
            "transAccountNo": "6217000010012052349", //交易卡号
	    "transAmount":100, //交易金额
	    "transTime":"2016-03-15 17:30:59", //交易时间
            "createDate"      : "2016-03-15 19:30:59", //创建时间
	    "requireReplyTime":"2016-03-16 17:30:59", //要求回复时间
	    "receiptType":1, //回单类型(1:差错交易查询,2:风险协查调单,3差错交易调单)
	    "receiptStatus":1, //回单状态(1:回单, 2:重新回单, 3:待审核, 4:完成回单, 5:关闭回单)
	    "rejectDesc":"性别不合适"//驳回原因项
        }, 
        {
            "receiptId": 1112321, 
            "transAccountNo": "6217000010012052349", 
	    "transAmount":100, 
	    "transTime":"2016-03-15 17:30:59",
	    "createDate"      : "2016-03-15 19:30:59", //创建时间
	    "requireReplyTime":"2016-03-16 17:30:59", 
	    "receiptType":1, 
	    "receiptStatus":1, 
	    "rejectDesc":"性别不合适"
        }, 
	{
            "receiptId": 1112321,
            "transAccountNo": "6217000010012052349",
	    "transAmount":100,
	    "transTime":"2016-03-15 17:30:59",
	    "createDate"      : "2016-03-15 19:30:59", //创建时间
	    "requireReplyTime":"2016-03-16 17:30:59",
	    "receiptType":1,
	    "receiptStatus":1,
	    "rejectDesc":"性别不合适"
        }
    ]
    "respMsg":"查询成功"
}
```
##### [返回目录↑](#content-title)


<a id="receipt"></a>
### 回单  /receipt
#### 1\. 回单
请求：  
```
POST /receipt HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"
"receiptId":1112321, //回单id
"receiptType": 1,//回单类型(1:差错交易查询,2:风险协查调单,3差错交易调单)
"transProvName": "北京市",//交易地址(省)
"transCityName": "北京市",//交易地址(市)
"transCountyName": "海淀区",//交易地址(区)
"areaDetailInfo":"马甸桥金澳国际写字楼",//详细街道信息
"merchantName":"小丸子",//商户信息
"mobileNo":"15801379665",//联系电话
"remark":"说点什么好",//备注
"salesSlip": 图片, //签购单图片(非必传)
"standBook": 图片, //台账图片(非必传)
"merchantNo":"Z08000000000001"//商户号
"receiptStatus":1, //回单状态(1:回单, 2:重新回单, 3:待审核, 4:完成回单, 5:关闭回单)
"replyCount":2//重复回单次数(当回单状态为重新回单时,必传)
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


<a id="receiptInfo"></a>
### 获取回单详情  /receiptInfo
#### 1\. 获取回单详情
请求：  
```
POST /receiptInfo HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"
"receiptId":1112321 //回单id
"receiptType": 1,//回单类型(1:差错交易查询,2:风险协查调单,3差错交易调单)
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
    "receiptInfo":{
    	"merchantNo":"Z08111111111111", //商户编号
	"merchantName":"小丸子", //商户名称
	"transAmount":100,//交易金额
	"receiptStatus":1, //回单状态(1:回单, 2:重新回单, 3:待审核, 4:完成回单, 5:关闭回单)
	"isOverdue":1,//是否逾期(1:是, 0:否)
	"dealTime":"2016-03-15 17:30:59",//处理日期
	"requireReplyTime":"2016-03-16 17:30:59", //要求回复时间
	"receiptType":1,//回单类型(1:差错交易查询,2:风险协查调单,3差错交易调单)
	"terminalNo": 22222,//终端号
	"transAccountNo": "6217000010012052349",//卡号
	"moneyStatus":"冻结",//资金状态
	"transNo":"ASD79233",//交易号
	"transTime":"2016-03-16 17:30:59", //交易时间
	"transType":"预授权",//交易类型
	"transProvName": "北京市",//交易地址(省)
        "transCityName": "北京市",//交易地址(市)
        "transCountyName": "海淀区",//交易地址(区)
        "areaDetailInfo":"马甸桥金澳国际写字楼",//详细街道信息
        "mobileNo":"15801376998",//联系电话
	"replyCount":2,//重复回单次数(当回单状态为重新回单时,必传)
	"salesSlipUUID": null,//签购单文件唯一标识
        "standBookUUID": null,//台账文件唯一标识
	"rejectDesc"      : "性别不合适"//驳回原因项(回单状态为重新回单时不为空)
    }
    "respMsg":"成功"
}
```
##### [返回目录↑](#content-title)


<a id="getMerchantInfo"></a>
### 获取商户详细信息  /getMerchantInfo
#### 1\. 获取商户详细信息
请求：  
```
POST /getMerchantInfo HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"
"merchantNo":XXXXXXXX, //商户NO
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

response: {
    "respTime": "20161220140037",
    "isSuccess": true,
    "respCode": "SUCCESS",
    "respMsg": "成功",
    "merchat":     {
    	"merchantId" : "XXXXXX",
    	"merchantNo" : "XXXXXX"
        "merchantName": "XXX",
        "merchantBankCard": "XXXXXXXXXXXXXXXXX",
        "merchantIdCard": "XXXXXXXXXXXXXXXXXX",
        "merchantMobile": "15201059026",// 如果authCode=3 表示商户四要素认证通过 会携带该字段，你可以直接那该信息到晋中银行               	         
 },
    "authCode": "1" // 0、表示没有经过四审 1、表示做过四审，送过来的商户信息身份信息匹配不成功 2、表示做过四审 身份匹配成功，账号匹配不成功 3、做过四审 身份和账户匹配成功
}

authCode =0 时 
  "merchant":     {
    	"merchantId" : "XXXXXX",
    	"merchantNo" : "XXXXXX"
        "merchantName": "XXX",
        "merchantBankCard": "XXXXXXXXXXXXXXXXX",
        "merchantIdCard": "XXXXXXXXXXXXXXXXXX",
 }

authCode =1 时 
 "merchant":     {
    	"merchantId" : "XXXXXX",
    	"merchantNo" : "XXXXXX"
        "merchantName": "XXX",
        "merchantBankCard": "XXXXXXXXXXXXXXXXX",
        "merchantIdCard": "XXXXXXXXXXXXXXXXXX",
 }


authCode =2 时
   "merchant":     {
    	"merchantId" : "XXXXXX",
    	"merchantNo" : "XXXXXX"
        "merchantName": "XXX",
        "merchantBankCard": "XXXXXXXXXXXXXXXXX",
        "merchantIdCard": "XXXXXXXXXXXXXXXXXX",
 }


authCode =3 时 

  "merchant":     {
        "merchantId" : "XXXXXX",
    	"merchantNo" : "XXXXXX"
        "merchantName": "XXX",
        "merchantBankCard": "XXXXXXXXXXXXXXXXX",
        "merchantIdCard": "XXXXXXXXXXXXXXXXXX",
        "merchantMobile":xxxxxxxx,
        "isNotice": "0" //0 没有电子银行 1 有电子银行
 }




```
##### [返回目录↑](#content-title)

<a id="authenticationMerchant"></a>
### 商户四要素认证和匹配  /authenticationMerchant
#### 1\. 商户四要素认证和匹配
请求：  
```
POST /authenticationMerchant HTTP/1.1
Host: mposp.21er.tk
Date: Thu, 03 Dec 2015 10:22:53
Content-Type: application/x-www-form-urlencoded; charset=utf-8
Content-Length: 30

"appVersion": "android.ZFT.1.2.143"
"merchantId":XXXXXXXX, //商户ID
"merchantNo":XXXXXXXX, //商户编号
"merchantName":XXXXXXXX, //商户名称
"merchantIdCard":XXXXXXXX, //商户身份证号
"merchantBankCard":, //商户结算卡号
"mobileNo":, //商户手机号
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
    "respTime": "20161216103350",
    "isSuccess": true,
    "respCode": "SUCCESS",
    "respMsg": "成功",
    "auth":true, //  true 表示验证成功 false 表示验证不成功
    "message" : xxxxxx
    "isNotice": "0" //0 没有电子银行 1 有电子银行
}
```
##### [返回目录↑](#content-title)
