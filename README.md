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
| 注册| [/register](#register)                      | urlencoded           | POST   |  李飞     | 否   |
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

