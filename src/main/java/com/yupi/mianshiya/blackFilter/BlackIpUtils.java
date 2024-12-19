/**
 * copyright (C), 2015-2024
 * fileName: BlackIpUtils
 *
 * @author: mlt
 * date:    2024/12/19 上午10:38
 * description:
 * history:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 * adpost    2024/12/19 上午10:38           V1.0
 */
package com.yupi.mianshiya.blackFilter;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.bloomfilter.BloomFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author mlt
 * @version 1.0.0
 * @date 2024/12/19
 */
@Slf4j
public class BlackIpUtils {
    private static BitMapBloomFilter bloomFilter;

    public static boolean isBlackIp(String ip){
        return bloomFilter.contains(ip);
    }

    public static void rebuildBloomFilter(String configInfo){
        if(StringUtils.isBlank(configInfo)){
            configInfo = "{}";
        }
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(configInfo, Map.class);
        List<String> blackIpList= (List<String>) map.get("blackIpList");
        // 重建bloomFilter
        synchronized (BlackIpUtils.class){
            if(blackIpList!=null && !blackIpList.isEmpty()){
                BitMapBloomFilter bitMapBloomFilter=new BitMapBloomFilter(10000);
                for(String ip:blackIpList){
                    bitMapBloomFilter.add(ip);
                }
                bloomFilter = bitMapBloomFilter;
            }
            else{
                bloomFilter = new BitMapBloomFilter(100);
            }
        }

    }


}
