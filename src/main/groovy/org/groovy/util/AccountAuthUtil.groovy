package org.groovy.util

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.commons.httpclient.params.HttpClientParams
import org.apache.commons.httpclient.params.HttpMethodParams
import org.apache.commons.lang.StringUtils
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
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.util
 * Author : Wukunmeng
 * User : wkm
 * Date : 15-12-10
 * Time : 下午12:01
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
class AccountAuthUtil {

    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, AccountAuthUtil.getSimpleName())

    private String id;

    private String accountNo;

    private String accountName;

    private String mobileNo;

    private String idenNo;

    private String bankName;

    private String bankCode;

    /**
     * 创建请求报文
     *
     *
     * @param phone
     * @param contents
     * @param sign
     * @return
     */
    def createRequestXml() {
        Element root = new Element("CNEPAYDSF");
        //增加头信息
        root.addContent(createHead());
        root.addContent(createBody());

        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");// 设置xml文件的字符为UTF-8，解决中文问题
        XMLOutputter xmlout = new XMLOutputter(format);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        xmlout.output(root, bo);
        log.info 'req: ' + bo.toString();

        Document document = new Document();
        document.setRootElement(root);
        XMLOutputter out = new XMLOutputter(formater());
        return out.outputString(document);
    }

    /**
     *
     * @param element
     */
    def createHead() {
        Element head = new Element("HEAD")
        initHeadData(head);
        return head;
    }

    def initHeadData(Element head) {
        head = addElement("TRX_CODE", head, "100005");
        head = addElement("REQ_SN", head, id);
        head = addElement("SIGNED_MSG", head, "");
        head = addElement("RET_CODE", head, "");
        head = addElement("NOTIFY_URL", head, Constants.MESSAGE_AUTH_RECEIVER_URL);
        return addElement("RET_MSG", head, "");
    }

    def createBody() {
        Element body = new Element("BODY")
        initBodyData(body);
        return body;
    }

    def initBodyData(Element body) {
        body = addElement("MERCHANT_ID", body, Constants.MESSAGE_AUTH_MERCHANT_ID);
        body = addElement("TOTAL_ITEM", body, "1");
        body = addElement("TOTAL_SUM", body, "0");
        body = addElement("SN", body, "1");
        body = addElement("BANK_NAME", body, bankName);
        body = addElement("BANK_CODE", body, bankCode);
        body = addElement("ACCOUNT_NAME", body, accountName);
        body = addElement("ACCOUNT_NO", body, accountNo);
        body = addElement("MOBILE_NO", body, mobileNo);
        body = addElement("ACCOUNT_PROP", body, "0");
        body = addElement("AMOUNT", body, "0");
        body = addElement("CURRENCY", body, "");
        body = addElement("PROTOCOL", body, "");
        body = addElement("PROTOCOL_USER_ID", body, "");
        body = addElement("RET_CODE", body, "");
        body = addElement("RET_MSG", body, "");
        body = addElement("ID_TYPE", body, "0");
        body = addElement("ID", body, idenNo);
        return addElement("EXTENDS", body, "");
    }

    /**
     * 格式化生成的xml文件，如果不进行格式化的话，生成的xml文件将会是很长的一行...
     * @return
     */
    public Format formater() {
        Format format = Format.getCompactFormat();
        format.setEncoding("GBK");
        format.setIndent(" ");
        return format;
    }

    /**
     * 发送短信消息
     * @param id 请求唯一标识
     * @param accountNo 账户号
     * @param accountName 账户名
     * @param mobileNo 手机号
     * @param idenNo 身份信息
     * @param bankName 银行名称
     * @param bankCode 银行编码
     * @return
     */
    public def sendMessage(String id, String accountNo, String accountName,
                           String mobileNo, String idenNo, String bankName, String bankCode) {
        log.info("银行卡4要素审核开关开启");
        if (!Constants.IS_CARD_VALIDATE) {
            log.info("银行卡4要素审核开关开启");
            def res = [code: "M0000", message: "验证成功"]
            return res
        }
        this.id = id;
        this.accountNo = accountNo;
        this.accountName = accountName;
        this.mobileNo = mobileNo;
        this.idenNo = idenNo;
        this.bankName = bankName;
        this.bankCode = bankCode;
        HttpClient client = createHttpClient();
        PostMethod post = createPostMethod(Constants.MESSAGE_AUTH_URL);
        try {
            def body = createRequestXml();
            post.addParameter("message", body);
            StringRequestEntity sre = new StringRequestEntity(body, "text/xml", "GBK");
            post.setRequestEntity(sre);
            int status = client.executeMethod(post);
            switch (status) {
                case HttpStatus.SC_OK:
                    def response = post.getResponseBodyAsString();
                    if (post) {
                        response = URLDecoder.decode(response, "GBK")
                    } else {
                        return null
                    }
                    log.info("response:" + URLDecoder.decode(response, "GBK"));
                    SAXBuilder builder = new SAXBuilder();
                    StringReader sr
                    try {
                        sr = new StringReader(response);
                        InputSource is = new InputSource(sr);
                        Document document = builder.build(is);
                        Element root = document.getRootElement();
                        Element head = root.getChild("HEAD");
                        def res = [code: head.getChild("RET_CODE")?.getText(), message: head.getChild("RET_MSG")?.getText()]
                        return res
                    } catch (Exception e) {
                        log.error("exception:" + e.getMessage(), e);
                        return null;
                    } finally {
                        sr?.close()
                    }
                    break;
                default:
                    def response = post.getResponseBodyAsString();
                    log.info("response:" + response);
                    return null;
            }
        } catch (Exception e) {
            post.abort();
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (post != null) {
                    post.releaseConnection();
                }
            } catch (Exception e) {
                log.error("exception:" + e.getMessage(), e);
            }
        }
    }

    private HttpClient createHttpClient() {
        def client = NameRegistrar.getIfExists(Constants.MESSAGE_AUTH_RECEIVER_URL);
        if (!client) {
            HttpClientParams clientParams = new HttpClientParams();
            clientParams.makeLenient();
            clientParams.setAuthenticationPreemptive(false);
            clientParams.setParameter(HttpMethodParams.SO_TIMEOUT, NumberUtils.toInt(Constants.httpcofig.SO_TIMEOUT, 30000));
            clientParams.setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, NumberUtils.toInt(Constants.httpcofig.HEAD_BODY_CHECK_TIMEOUT, 30000));
            clientParams.setParameter(HttpMethodParams.REJECT_HEAD_BODY, Boolean.TRUE);
            clientParams.setParameter(HttpMethodParams.STATUS_LINE_GARBAGE_LIMIT, NumberUtils.toInt(Constants.httpcofig.STATUS_LINE_GARBAGE_LIMIT, 0));
            clientParams.setParameter(HttpClientParams.MAX_REDIRECTS, NumberUtils.toInt(Constants.httpcofig.MAX_REDIRECTS, 5));
            clientParams.setConnectionManagerTimeout(NumberUtils.toInt(Constants.httpcofig.CONN_MANAGER_TIMEOUT, 30000));
            client = new HttpClient(clientParams, new MultiThreadedHttpConnectionManager());
            NameRegistrar.register(Constants.MESSAGE_AUTH_RECEIVER_URL, client);
        }
        return client;
    }

    private PostMethod createPostMethod(String url) {
        PostMethod postMethod = new PostMethod(url);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "GBK");
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

    private def addElement(String elementName, def parent, def content) {
        Element element = new Element(elementName);
        if (!StringUtils.isBlank(content as String)) {
            element.setText(content as String);
        }
        parent.addContent(element);
        parent;
    }
}
