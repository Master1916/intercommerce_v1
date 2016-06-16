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
    "idCard":"341225199005063894", //身份证号(当为POS商户时,必返)
    "realName":"张树彬",//真实姓名(当为POS商户时,必返)
    "posStatus": 0 //POS认证状态 (0未绑定 ,1待刷卡，2待认证,3实名认证通过)
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
    "merchantList": [    
      {
        "merchantid": 676453,--商户ID
        "merchantNo": "500000000621891"--商户编码
      },
    ...
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
"start": "2016-06-06", --起始时间 yyyy-MM-dd格式
"end": "2016-06-06", --结束时间 yyyy-MM-dd格式 
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
	"transTime": "20160606125959",//交易时间
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
    "isLoaded": false, //是否加载完数据
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
            "settleDate": "2016-03-15 16:31:25", //结算日期
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
            "settleDate": "2016-03-15 16:25:26", 
            "uniqueRecord": "10793-6228480010970642611-895590-d0"
        }
    ], 
    "settleCount": 7, //总条数
    "settleAmount": 6413673//结算总额(单位:分)
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
    "dateCreated": "2016-03-15 16:30:00", //数据创建时间 
    "status": 2, //结算状态(1:成功, 2:失败)
    "transAmount": 29853, //交易金额(单位:分)
    "settleMoney": 29853, //结算金额(单位:分)
    "accountNum": "6228480010970642611", //结算账户 
    "merchantName": "旧数据企业", //商户名称
    "merchantNo": "500000000876552", //商户号
    "settleDate": "2016-03-15 16:31:25", //结算日期
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
            "imageUrl": "http://localhost:29008/downloadBanner.action?fileName=main_home_ad_2.png&appVersion=ios.ZFT.1.2.143&type=banner&date=20160612&aid=2", //图片路径
            "targetUrl": "www.baidu.com"//跳转地址
        }
    ], 
    "head": {
        "total": 1//广告总条数
    }
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
type	  : "advertisement"//获取类型(advertisement:"广告", campaign:"活动")
date      : "20160616"//日期
aid       : 2//(广告、活动)id
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
    图片文件
}
```

