package org.groovy.service.common

import org.groovy.common.Commons
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar
import org.mobile.mpos.service.common.BannerService

import javax.servlet.http.HttpServletRequest

/**
 * Create with IntelliJ IDEA
 * Project name : mposp
 * Package name : org.groovy.service.common
 * Author : Wukunmeng
 * User : wkm
 * Date : 16-1-14
 * Time : 下午5:20
 * 版权所有,侵权必究！
 * To change this template use File | Settings | File and Code Templates.
 */
public class BannerServiceImpl implements BannerService{

    private Log log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, BannerServiceImpl.getSimpleName());

    /**
     * 广告列表接口
     * 广告的报文结构如下
     * {
     *      "head": {
     *          "total" : 1
     *      }
     *      "body": [
     *          {
     *              "title":"广告标题",
     *              "message":"广告信息",  //可有可无
     *              "imageUrl":"http://image.21er.tk/1.jpg",
     *              "targetUrl":"http://image.21er.tk/11.jpg"
     *          }
     *      ]
     * }
     * @param request
     * @return
     */
    @Override
    public Object listBanners(HttpServletRequest request) {

        //TODO 前期提供静态的接口，后续通过MessageUtil调用广告服务
        //new MessageUtil().listBanner()
        def head = ["total":"2"];
        def b1 = ["title":"广告位1"];
        b1 << ["message":"广告信息1"]
//        b1 << ["imageUrl":"https://boss3.cnepay.net/images/adpic/main_home_ad_1.png"]
        b1 << ["imageUrl":"https://mposp.vcpos.cn/downloadBanner.action?fileName=main_home_ad_1.png&appVersion=ios.ZFT.1.1.813&type=banner"] //生产地址
//        b1 << ["imageUrl":"http://mposp.21er.tk/downloadBanner.action?fileName=main_home_ad_1.png&appVersion=ios.ZFT.1.1.813&type=banner"] //测试地址
        b1 << ["targetUrl":""]
        def b2 = ["title":"广告位2"];
        b2 << ["message":"广告信息2"]
//        b2 << ["imageUrl":"https://boss3.cnepay.net/images/adpic/main_home_ad_2.png"]
        b2 << ["imageUrl":"https://mposp.vcpos.cn/downloadBanner.action?fileName=main_home_ad_2.png&appVersion=ios.ZFT.1.1.813&type=banner"] //生产地址
//        b2 << ["imageUrl":"http://mposp.21er.tk/downloadBanner.action?fileName=main_home_ad_2.png&appVersion=ios.ZFT.1.1.813&type=banner"] //测试地址
        b2 << ["targetUrl":""]
        def b3 = ["title":"广告位3"];
        b3 << ["message":"广告信息3"]
        b3 << ["imageUrl":"https://mposp.vcpos.cn/downloadBanner.action?fileName=main_home_ad_3.png&appVersion=ios.ZFT.1.1.813&type=banner"] //生产地址
//        b3 << ["imageUrl":"http://mposp.21er.tk/downloadBanner.action?fileName=main_home_ad_3.png&appVersion=ios.ZFT.1.1.813&type=banner"] //测试地址
        b3 << ["targetUrl":""]
//        def b4 = ["title":"广告位4"];
//        b4 << ["message":"广告信息4"]
//        b4 << ["imageUrl":"http://image.21er.tk/4.jpg"]
//        b4 << ["targetUrl":"http://image.21er.tk/41.jpg"]
        def body = [];
        body << b1;
        body << b2;
        body << b3;
//        body << b4;
        def banner = ["head":head];
        banner << ["body":body];
        return Commons.success(banner, "成功");
    }

}
