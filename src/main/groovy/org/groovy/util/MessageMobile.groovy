package org.groovy.util

import net.sf.json.JSONObject
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.params.HttpMethodParams
import org.groovy.common.Constants
import org.jdom.Element
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

import java.security.MessageDigest

/**
 *  发送短信工具类
 */
class MessageMobile {

    private Log log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, MessageMobile.getSimpleName())

    /**
     * 创建请求报文
     *
     *
     * @param phone
     * @param contents
     * @param sign
     * @return
     */
    NameValuePair[] createMessageListRequest(String mobile, String osType, boolean detail) {
        String methodType = "7";
//        String dateTime = new Timestamp().timestamp;
//        String c = String.valueOf(System.currentTimeMillis());
//        String random = c.substring(c.length() - 5);
//        String md5 = encode(dateTime + random + methodType);
        NameValuePair[] nameValuePairs = new NameRegistrar[4];
        nameValuePairs[0] = new NameValuePair("methodType", methodType);
        nameValuePairs[1] = new NameValuePair("osType", osType);
        nameValuePairs[2] = new NameValuePair("phoneNum", mobile);
        nameValuePairs[3] = new NameValuePair("detail", String.valueOf(detail));
        log.info(nameValuePairs + "----")
        return nameValuePairs
    }

    /**
     *
     * @param mobile
     * @param messageId
     * @return
     */
    NameValuePair[] createModifyMessageStatusRequest(String mobile, String messageId, String osType) {
//        String dateTime = new Date().format(Constants.DATE_FORMAT);
//        String c = String.valueOf(System.currentTimeMillis());
//        String random = c.substring(c.length() - 5);
        String methodType = "8";
//        String md5 = encode(dateTime + random + methodType);
        NameValuePair[] nameValuePairs = new NameRegistrar[4];
        nameValuePairs[0] = new NameValuePair("methodType", methodType);
        nameValuePairs[1] = new NameValuePair("osType", osType);
        nameValuePairs[2] = new NameValuePair("phoneNum", mobile);
        nameValuePairs[3] = new NameValuePair("newsId", messageId);

        log.info(nameValuePairs + "----")
        return nameValuePairs

    }

    /**
     * 发送短信消息
     *
     *
     * @param phone 手机号
     * @param content 发送内容
     * @return
     */
    public def listMessage(String mobile, String os, boolean detail) {
        String osType = os.equalsIgnoreCase("ios") ? "0" : "1";
        return post(createMessageListRequest(mobile, osType, detail));
    }

    /**
     * 发送短信消息
     *
     *
     * @param phone 手机号
     * @param content 发送内容
     * @return
     */
    public def modifyMessage(String mobile, String messageId, String os) {
        String osType = os.equalsIgnoreCase("ios") ? "0" : "1";
        return post(createModifyMessageStatusRequest(mobile, messageId, osType));
    }

    public def post(NameValuePair[] nameRegistrars) {
        HttpClient client = createHttpClient();
        String path = Constants.MESSAGE_CENTER;
        log.info "MESSAGE PATH :" + path
        PostMethod post = createPostMethod(Constants.MESSAGE_CENTER);
        try {
            post.addParameters(nameRegistrars);
            int status = client.executeMethod(post);
            def response = post.getResponseBodyAsString();
            switch (status) {
                case HttpStatus.SC_OK:
                    try {

                        log.info("response :" + response);

                        JSONObject jsonObject = JSONObject.fromObject(response);
                        log.info("to json:" + jsonObject.toString(4));
                        return jsonObject
                    } catch (Exception e) {
                        post.abort();
                        log.error("Exception:" + e.getMessage(), e);
                        return null;
                    }
                    break;
                default:
                    log.info("response:" + response);
                    post.abort();
                    return null;
            }
        } catch (Exception e) {
            post.abort();
            log.error("Exception:" + e.getMessage(), e);
            return null;
        } finally {
            try {
                if (post != null) {
                    post.releaseConnection();
                }
            } catch (Exception e) {
                log.error("Exception:" + e.getMessage(), e);
            }
        }
    }

    private HttpClient createHttpClient() {
        HttpClient client = new HttpClient();
        return client;
    }

    private PostMethod createPostMethod(String url) {
        PostMethod postMethod = new PostMethod(url);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Constants.CHARSET_UTF_8);
        return postMethod;
    }

    private String encode(String sourceString) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceString.getBytes(Constants.CHARSET_UTF_8));
            byte[] temp = md.digest(Constants.MESSAGE_CENTER_KEY.getBytes(Constants.CHARSET_UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < temp.length; i++) {
                stringBuilder.append(Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6));
            }
            return stringBuilder.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private def addElement(String elementName, def root, def content) {
        Element element = new Element(elementName);
        if (null != content) {
            element.setText(content as String);
        } else {
            element.setText(Constants."MSG_${elementName.toUpperCase()}" as String);
            if ("password".equals(elementName)) {
                element.setText(encode(Constants."MSG_${elementName.toUpperCase()}" as String));
            }
        }
        root.addContent(element);
        root;
    }
}
