package org.groovy.util

import org.groovy.dao.dictionary.DictionaryDao
import org.groovy.dao.merchant.MerchantDao
import org.jpos.util.Log
import org.jpos.util.Logger
import org.jpos.util.NameRegistrar

/**
 * Created with IntelliJ IDEA.
 *
 * 地图工具
 *
 */
class LocationUtil {
    //添加日志记录对象
    static log = new Log(NameRegistrar.getIfExists('logger.Q2') as Logger, LocationUtil.getSimpleName())
    //高德定位接口路径
    private static final String GAODE_INTERFACE_REGEO = 'http://restapi.amap.com/v3/geocode/regeo';
    //高德接口key
    private static final String GAODE_INTERFACE_KEY = '2ad3fef8524a0a373150f14b3304114f';

    /**
     * Created with IntelliJ IDEA.
     *
     *  用经度和纬度定位：返回当前经纬度所在的地址的省份(province)-城市(city)-地区(district)
     *  params:
     *      longitude:经度（小数点后最多6位数）
     *      latitude :纬度（小数点后最多6位数）,
     *
     */
    static Map locationByLongitudeAndLatitude(def longitude, def latitude) {
        //设定返回值
        def result = [:]
        try {
            //经纬度为空则返回空
            if (!(longitude != null && latitude != null)) {
                //返回响应
                return result
            }
            //设置访问高德接口路径
            def url = GAODE_INTERFACE_REGEO + '?output=json&location=' + longitude.trim() + ',' + latitude.trim() + '&key=' + GAODE_INTERFACE_KEY;
            //访问接口，接受返回的json串
            def reqResult = HttpClientUtil.doGet(url)
            //将json转换成map对象
            def resultMap = new groovy.json.JsonSlurper().parseText(reqResult)
            log.info 'reqResult=' + reqResult
            //判断接口调用是否成功
            if ('1' == resultMap.status) {
                //添加返回的省份
                result << ['province': resultMap.regeocode.addressComponent.province]
                //添加返回的市
                result << ['city': resultMap.regeocode.addressComponent.city]
                //添加返回的地区
                result << ['district': resultMap.regeocode.addressComponent.district]
            }
            //返回结果
            return result
        } catch (Exception e) {
            //如果有异常则返回空，并且记录异常信息
            log.error("获取高德定位失败：" + e.getMessage());
            //返回响应
            return result
        }

    }

    /**
     * Created with IntelliJ IDEA.
     *
     *  更新/存储商户经纬度所在的地址的省份(province)-城市(city)-地区(district)
     *  params:
     *      merchID:商户ID
     *      code   :省/市代码
     *
     */
    static boolean saveAreaCode(def merchantT1, def merchantD0, def secondMerchantD0, def position) {
        try {
            //经度值
            def longitude
            //纬度值
            def latitude
            //获取经纬度
            def positions = position?.split(",")
            //如果position格式不正确，则经纬度值为空
            if (positions?.size() == 2) {
                //经度值初始化
                longitude = positions[0]
                //纬度值初始化
                latitude = positions[1]
            }
            //获取数据库操作对象
            DictionaryDao dictionaryDao = new DictionaryDao()
            MerchantDao merchantDao = new MerchantDao()
            //访问高德经纬度定位接口
            def result = locationByLongitudeAndLatitude(longitude, latitude)
            //设定高德获取的地域名
            def areaName;
            //如果地区为直辖市，则使用省名查询
            if (result?.city) {
                //如果是直辖市，则使用市名
                areaName = result.city;
            } else if (result?.province) {
                //如果是非直辖市，则使用省份
                areaName = result?.province;
            }
            //如果高德返回的地址不是空，则查询area表获取区域ID
            if (areaName) {
                //查询area表获取区域ID
                def areaInfo = dictionaryDao.findAreaInfoByAreaName(areaName);
                //区域ID不为空则添加到商户信息中
                if (areaInfo) {
                    //更新商户区域字段
                    if(merchantT1){
                        merchantT1.area_id = areaInfo.id;
                        merchantDao.updateMerchantAreaIDByMerchID(merchantT1)
                        log.info '已经更新了T1商户的区域ID'
                    }
                    if(merchantD0) {
                        merchantD0.area_id = areaInfo.id;
                        merchantDao.updateMerchantAreaIDByMerchID(merchantD0)
                        log.info '已经更新了D0商户的区域ID'
                    }
                    if(secondMerchantD0) {
                        secondMerchantD0.area_id = areaInfo.id;
                        merchantDao.updateMerchantAreaIDByMerchID(secondMerchantD0)
                        log.info '已经更新了D0秒到商户的区域ID'
                    }


                }
            } else {
                log.info "没有找到定位信息"
            }
            //如果T1商户的区域ID还是空，则给默认值北京
            if (merchantT1 && merchantT1.area_id == null) {
                merchantT1.area_id = 1
                log.info '已经更新了T1商户的区域ID 默认北京'
//                merchantDao.updateMerchantAreaIDByMerchID(merchantT1)
                merchantDao.update(merchantT1);
            }
            //如果D0商户的区域ID还是空，则给默认值北京
            if (merchantD0 && merchantD0.area_id == null) {
                merchantD0.area_id = 1
                log.info '已经更新了D0商户的区域ID 默认北京'
//                merchantDao.updateMerchantAreaIDByMerchID()
                merchantDao.update(merchantD0);
            }
            //如果D0商户的区域ID还是空，则给默认值北京
            if (secondMerchantD0 && secondMerchantD0.area_id == null) {
                secondMerchantD0.area_id = 1
                log.info '已经更新了D0商户的区域ID 默认北京'
//                merchantDao.updateMerchantAreaIDByMerchID()
                merchantDao.update(secondMerchantD0);
            }
            //返回成功响应
            return true
        } catch (Exception e) {
            //记录错误信息
            log.error("更新/添加商户当前地区ID失败：" + e.getMessage())
            //返回失败响应
            return false
        }
    }
}
