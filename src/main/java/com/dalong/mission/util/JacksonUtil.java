package com.dalong.mission.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JacksonUtil {

    private static ObjectMapper objectMapper;

    static {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,false);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonUtil.class);

    public static <T> T readValue(String jsonStr, Class<T> valueType) {
        try {
            return objectMapper.readValue(jsonStr, valueType);
        } catch (Exception e) {
            LOGGER.error("字符串转换成对象失败", e);
            throw new RuntimeException("字符串转换成对象失败");
        }
    }

    public static <T> T readValue(String jsonStr, TypeReference<T> valueTypeRef) {
        try {
            return objectMapper.readValue(jsonStr, valueTypeRef);
        } catch (Exception e) {
            LOGGER.error("字符串转换成对象失败", e);
            throw new RuntimeException("字符串转换成对象失败");
        }
    }

    public static String toJSON(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            LOGGER.error("对象转换成字符串失败", e);
            throw new RuntimeException("对象转换成字符串失败");
        }
    }
}
