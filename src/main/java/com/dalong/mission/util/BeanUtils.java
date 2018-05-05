package com.dalong.mission.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * bean工具类
 *
 * @author dalong
 * @summary 工具类
 * @since 2018-05-05 下午12:08
 **/
public class BeanUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);

    private static ObjectMapper objectMapper = new ObjectMapper();


    static {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static <T, R> R convertType(T source, Class<R> targetClass) {
        if (source == null) {
            return null;
        }
        final R result = org.springframework.beans.BeanUtils.instantiate(targetClass);
        org.springframework.beans.BeanUtils.copyProperties(source, result);
        return result;
    }

    public static <T, R> R jsonConvertType(T source, Class<R> targetClass) {
        if (source == null) {
            return null;
        }
        R result = null;
        try {
            String value = objectMapper.writeValueAsString(source);
            result = objectMapper.readValue(value, objectMapper.getTypeFactory().constructType(targetClass));
        } catch (JsonProcessingException e) {
            LOGGER.info(e.getClass().getName(), e);
        } catch (IOException e) {
            LOGGER.info(e.getClass().getName(), e);
        }
        return result;
    }

    public static <T, R> List<R> convertType(List<T> source, Class<R> targetClass) {
        if (source == null) {
            return null;
        }
        final Function<Class<R>, Function<T, R>> convertBeanFunFactory = t -> s -> BeanUtils.convertType(s, t);
        final Function<T, R> convertBeanFun = convertBeanFunFactory.apply(targetClass);

        return source.stream().map(convertBeanFun).collect(Collectors.toList());
    }

    public static <T, R> List<R> jsonConvertType(List<T> source, Class<R> targetClass) {
        if (source == null) {
            return null;
        }
        List<R> result = null;
        try {
            String value = objectMapper.writeValueAsString(source);
            result = objectMapper.readValue(value, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, targetClass));
        } catch (IOException e) {
            LOGGER.info(e.getClass().getName(), e);
        }
        return result;
    }


    public static Set<String> fieldsNonNull(Object bean) {
        final PropertyDescriptor[] dps = org.springframework.beans.BeanUtils.getPropertyDescriptors(bean.getClass());
        return Arrays.stream(dps)
                .filter(pd -> pd.getReadMethod() != null)
                .filter(pd -> {
                    Method m = pd.getReadMethod();
                    try {
                        return m.invoke(bean) != null;
                    } catch (IllegalAccessException | InvocationTargetException ignore) {
                        return false;
                    }
                })
                .map(PropertyDescriptor::getName)
                .collect(Collectors.toSet());
    }

    // 排序
//    public static <T, R> Pagination<R> convert(Pagination<T> pagination, Function<T, R> mapper, ExecutorService executorService) {
//        return Mono.just(new Pagination<R>(pagination.getPageNo(), pagination.getPageSize()))
//                .map(page -> {
//                    page.setTotalCount(pagination.getTotalCount());
//                    if (pagination.getPageList() == null || pagination.getPageList().size() == 0) {
//                        return page;
//                    }
//                    page.setPageList(Flux.fromIterable(pagination.getPageList())
//                            .zipWith(Flux.range(1, Integer.MAX_VALUE),
//                                    (id, sortNo) -> new SortView<T, R>(sortNo, id))
//                            .flatMap(sortView -> Mono.fromCallable(() -> sortView.setRet(
//                                    mapper.apply(sortView.getTarget()))).subscribeOn(
//                                    Schedulers.fromExecutor(executorService)),
//                                    pagination.getPageSize())
//                            .sort()
//                            .map(sv -> sv.getRet())
//                            .collect(Collectors.toList())
//                            .block());
//                    return page;
//                })
//                .block();
//    }
//
//    public static class SortView<T, R> implements Comparable<SortView> {
//        private int sortNo;
//        private T target;
//        private R ret;
//
//        public SortView() {
//        }
//
//        public SortView(int sortNo, T target) {
//            this.sortNo = sortNo;
//            this.target = target;
//        }
//
//        @Override
//        public int compareTo(SortView o) {
//            return this.sortNo - o.getSortNo();
//        }
//
//        public int getSortNo() {
//            return this.sortNo;
//        }
//
//        public SortView<T, R> setSortNo(int sortNo) {
//            this.sortNo = sortNo;
//            return this;
//        }
//
//        public T getTarget() {
//            return this.target;
//        }
//
//        public SortView<T, R> setTarget(T target) {
//            this.target = target;
//            return this;
//        }
//
//        public R getRet() {
//            return this.ret;
//        }
//
//        public SortView<T, R> setRet(R ret) {
//            this.ret = ret;
//            return this;
//        }
//    }
}
