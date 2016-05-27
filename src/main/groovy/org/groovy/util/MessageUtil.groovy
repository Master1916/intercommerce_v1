package org.groovy.util

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.params.HttpClientParams
import org.apache.commons.httpclient.params.HttpMethodParams
import org.apache.commons.lang.math.NumberUtils
import org.groovy.common.Constants
import org.jdom.Document
import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jdom.output.Format
import org.jdom.output.XMLOutputter
import org.jpos.iso.ISOUtil
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.xml.sax.InputSource

import java.security.MessageDigest

/**
 *  发送短信工具类
 */
class MessageUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, MessageUtil.getSimpleName())

    /**
     * 创建请求报文
     *
     *
     * @param phone
     * @param contents
     * @param sign
     * @return
     */
    def createRequestXml(def phone, def contents, def sign){
        Element root = new Element("message");
        //用户名密码
        root = addElement("account",root,null);
        root = addElement("password",root,null);
        root = addElement("msgid",root,Constants.MSG_MESSAGEID);
        root = addElement("phones",root,phone);
        root = addElement("content",root,contents);
        root = addElement("sign",root,sign);
        root = addElement("subcode",root,null);
        //空为立即发送
        root = addElement("sendtime",root,null);
        Document document = new Document();
        document.setRootElement(root);
        XMLOutputter out = new XMLOutputter(format());
        return out.outputString(document);
    }

    /**
     * 格式化生成的xml文件，如果不进行格式化的话，生成的xml文件将会是很长的一行...
     * @return
     */
    public Format format(){
        Format format = Format.getCompactFormat();
        format.setEncoding(Constants.CHARSET_UTF_8);
        format.setIndent(" ");
        return format;
    }

    /**
     * 发送短信消息
     *
     *
     * @param phone 手机号
     * @param content 发送内容
     * @return
     */
    public String sendMessage(String phone,String content){
        HttpClient client = createHttpClient();
        PostMethod post = createPostMethod(Constants.MSG_URL);
        try {
            def body = createRequestXml(phone,content,"【中汇掌富通】");
            post.addParameter("message",body)
            int status = client.executeMethod(post);
            switch (status){
                case HttpStatus.SC_OK :
                    def response = post.getResponseBodyAsString();
                    SAXBuilder builder = new SAXBuilder();
                    try {
                        StringReader sr = new StringReader(response);
                        InputSource is = new InputSource(sr);
                        Document document = builder.build(is);
                        Element root = document.getRootElement();
                        Element result = root.getChild("result");
                        if(result == null){
                            return  null;
                        }
                        return result.getText();
                    } catch (Exception e){
                        e.printStackTrace();
                        return null;
                    }
                    break;
                default:
                    def response = post.getResponseBodyAsString();
                    log.info("response:" + response);
                    return null;
            }
        } catch (Exception e){
            post.abort();
            e.printStackTrace();
            return null;
        } finally {
            try {
                if(post != null){
                    post.releaseConnection();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private HttpClient createHttpClient(){
        def client = NameRegistrar.getIfExists(Constants.MESSAGE_AUTH_RECEIVER_URL);
        if(!client) {
            HttpClientParams clientParams = new HttpClientParams();
            clientParams.makeLenient();
            clientParams.setAuthenticationPreemptive(false);
            clientParams.setParameter(HttpMethodParams.SO_TIMEOUT, NumberUtils.toInt(Constants.httpcofig.SO_TIMEOUT,30000));
            clientParams.setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, NumberUtils.toInt(Constants.httpcofig.HEAD_BODY_CHECK_TIMEOUT,30000));
            clientParams.setParameter(HttpMethodParams.REJECT_HEAD_BODY, Boolean.TRUE);
            clientParams.setParameter(HttpMethodParams.STATUS_LINE_GARBAGE_LIMIT, NumberUtils.toInt(Constants.httpcofig.STATUS_LINE_GARBAGE_LIMIT,0));
            clientParams.setParameter(HttpClientParams.MAX_REDIRECTS, NumberUtils.toInt(Constants.httpcofig.MAX_REDIRECTS,5));
            clientParams.setConnectionManagerTimeout(NumberUtils.toInt(Constants.httpcofig.CONN_MANAGER_TIMEOUT,30000));
            client = new HttpClient(clientParams, new MultiThreadedHttpConnectionManager());
            NameRegistrar.register(Constants.MESSAGE_AUTH_RECEIVER_URL,client);
        }
        return client;
    }

    private PostMethod createPostMethod(String url){
        PostMethod postMethod = new PostMethod(url);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,Constants.CHARSET_UTF_8);
        return postMethod;
    }

    private String encode(String sourceString) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return ISOUtil.hexString(md.digest(new String(sourceString).getBytes())).toLowerCase();
        } catch (Exception ex) {
            return null;
        }
    }

    private def addElement(String elementName, def root, def content){
        Element element = new Element(elementName);
        if(null != content){
            element.setText(content as String);
        }else{
            element.setText(Constants."MSG_${elementName.toUpperCase()}" as String);
            if("password".equals(elementName)){
                element.setText(encode(Constants."MSG_${elementName.toUpperCase()}" as String));
            }
        }
        root.addContent(element);
        root;
    }
}
