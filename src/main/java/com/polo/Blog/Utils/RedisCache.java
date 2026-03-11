package com.polo.Blog.Utils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polo.Blog.Domain.Entity.MessageSlip;
import com.polo.Blog.Service.MessageSlipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisCache {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final Logger log = LoggerFactory.getLogger(RedisCache.class);
    @Autowired
    private ObjectMapper objectMapper;
    //========空值防止缓存穿透===================//
    /// 空值
    private static final String NULL_VALUE = "null";
    /// 空值缓存时间
    private static final long NULL_TTL = 5;
    /**
     * 读取缓存
     * @param key 键
     * @param typeReference 读取数据类型
     * @return 返回值
     * @param <T> 泛型 复杂类型
     */
    public <T> T get(String key, TypeReference<T> typeReference){
        String json = stringRedisTemplate.opsForValue().get(key);
        /// 如果查询结果为空，返回null
        if(NULL_VALUE.equals(json) || json == null){
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        }catch (Exception e){
            return null;
        }
    }

    /// 单类型
    public <T> T get(String key, Class<T> clazz){
        String json = stringRedisTemplate.opsForValue().get(key);
        /// 如果查询结果为空，返回null
        if(NULL_VALUE.equals(json) || json == null){
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        }catch (Exception e){
            log.error(String.valueOf(e));
            return null;
        }
    }
    /**
     * 写入缓存
     * @param key 键
     * @param value 值
     * @param timeout 超时限定
     * @param timeUnit 超时单位
     * @param <T> 泛型
     */
    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit){
        try{
            /// 数据库查不到，Redis中缓存空值
            if(value == null){
                stringRedisTemplate.opsForValue().set(key, NULL_VALUE, NULL_TTL, TimeUnit.MINUTES);
                return;
            }
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, timeout, timeUnit);

        }catch (Exception e){
          log.error(String.valueOf(e));
        }
    }

    /**
     * 删除缓存
     * @param key 键
     */
    public void deleteCache(String key){
        try {
            stringRedisTemplate.delete(key);
        }catch (Exception ignored){

        }
    }


    /**
     * 随机获取 N 条数据
     *
     * @param num           随机取数量
     * @param key           键
     * @param typeReference 读取数据类型
     * @param <T>           泛型 复杂类型
     * @return 返回值
     */
    public <T> List<MessageSlip> getMessageToShowCache(String key, TypeReference<T> typeReference, Integer num, MessageSlipService messageSlipService){
        List<String> jsons = stringRedisTemplate.opsForSet().randomMembers(key, num);
        /// 如果查询结果为空，返回null
        if(jsons == null){
            return new ArrayList<MessageSlip>();
        }
        try {
            List<MessageSlip> messageSlipCache =  jsons.stream().map(json -> JSON.parseObject(json, MessageSlip.class)).collect(Collectors.toList());
            if(messageSlipCache != null) return messageSlipCache;
        }catch (Exception e){
            return refreshAndReturn(key, num, messageSlipService);
        }
        return null;
    }
    private List<MessageSlip> refreshAndReturn(String key, Integer num, MessageSlipService service) {
        stringRedisTemplate.delete(key);

        LambdaQueryWrapper<MessageSlip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageSlip::getIsDeleted, 0).last("ORDER BY RAND() LIMIT 100");
        List<MessageSlip> list = service.list(wrapper);

        // 存入 Set
        if (!list.isEmpty()) {
            String[] members = list.stream()
                    .map(JSON::toJSONString)
                    .toArray(String[]::new);
            stringRedisTemplate.opsForSet().add(key, members);
            stringRedisTemplate.expire(key, 5, TimeUnit.MINUTES);
        }

        // 返回请求的数量（或全部）
        return list.stream().limit(num).collect(Collectors.toList());
    }
    public void setMessageSet(String key, List<MessageSlip> list, long timeout, TimeUnit unit) {
        stringRedisTemplate.delete(key);
        if (list != null && !list.isEmpty()) {
            String[] members = list.stream()
                    .map(JSON::toJSONString)
                    .toArray(String[]::new);
            stringRedisTemplate.opsForSet().add(key, members);
            stringRedisTemplate.expire(key, timeout, unit);
        }
    }
}
