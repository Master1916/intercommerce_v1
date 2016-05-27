package org.groovy.util

import net.sf.json.JSONObject
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.params.HttpMethodParams
import org.groovy.common.Constants
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar


/**
 *  发送短信工具类
 */
class BannerUtil {

    private Log log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, BannerUtil.getSimpleName())

    /**
     * 创建请求报文
     *
     *
     * @param phone
     * @param contents
     * @param sign
     * @return
     */
    NameValuePair[] createBannerRequest() {
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


    public def listBanner(){
        return post(createBannerRequest());
    }

    public def post(NameValuePair[] nameRegistrars) {
        HttpClient client = createHttpClient();
        //TODO 需要改成广告服务地址
        String path = "http://banner.service.org/xxxxx";
        log.info "Banner path :" + path
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
}
