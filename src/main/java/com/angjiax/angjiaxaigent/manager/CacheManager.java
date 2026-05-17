package com.angjiax.angjiaxaigent.manager;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class CacheManager {
    private static CacheManager instance;
    private final Map<String, CacheEntry> cache;
    private final long defaultTTL; // 默认过期时间（毫秒）

    // 缓存条目（值 + 过期时间）
    private static class CacheEntry {
        Object value;
        long expireTime;

        CacheEntry(Object value, long ttl) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + ttl;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    // 私有构造方法
    private CacheManager() {
        this.cache = new ConcurrentHashMap<>();
        this.defaultTTL = TimeUnit.MINUTES.toMillis(30); // 默认30分钟过期
    }

    // 获取单例（双重校验锁）
    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }

    // 存入缓存
    public void put(String key, Object value, long ttl) {
        cache.put(key, new CacheEntry(value, ttl));
    }

    public void put(String key, Object value) {
        put(key, value, defaultTTL);
    }

    // 获取缓存（自动清理过期条目）
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        return entry.value;
    }

    // 删除缓存
    public void remove(String key) {
        cache.remove(key);
    }

    // 清空缓存
    public void clear() {
        cache.clear();
    }
}