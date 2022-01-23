package com.liu.nkcommunity.util;

import ch.qos.logback.core.pattern.ConverterUtil;
import com.alibaba.fastjson.JSONObject;
import com.sun.corba.se.spi.ior.ObjectKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    public static String md5(String key){
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes(StandardCharsets.UTF_8));
    }

    // 转为json的数据传递
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        if (map != null){
            for (String key: map.keySet()){
                jsonObject.put(key, map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    public static String getJSONString(int code, String msg){
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code){
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name","zhangsan");
        map.put("age", 20);
        String s = getJSONString(0, "200", map);
        System.out.println("s = " + s);
    }

}




















