package com.example.yin.service.impl;

import com.example.yin.service.SearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 搜索记录服务实现类
 */
@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {
    
    private static final String SEARCH_HISTORY_KEY_PREFIX = "search_history:user:";
    private static final int MAX_HISTORY_SIZE = 10; // 最多保存10条搜索记录
    private static final int EXPIRE_DAYS = 30; // 搜索记录过期时间30天
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void addSearchHistory(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        
        String key = SEARCH_HISTORY_KEY_PREFIX + userId;
        String trimmedKeyword = keyword.trim();
        
        try {
            // 使用有序集合存储，分数为当前时间戳，这样可以按时间排序
            long timestamp = System.currentTimeMillis();
            
            // 先移除已存在的相同关键词
            redisTemplate.opsForZSet().remove(key, trimmedKeyword);
            
            // 添加新的搜索记录
            redisTemplate.opsForZSet().add(key, trimmedKeyword, timestamp);
            
            // 保持最多10条记录
            redisTemplate.opsForZSet().removeRange(key, 0, -MAX_HISTORY_SIZE - 1);
            
            // 设置过期时间
            redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS);
            
            System.out.println("添加搜索记录成功: userId=" + userId + ", keyword=" + trimmedKeyword);
            
        } catch (Exception e) {
            System.err.println("添加搜索记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public List<String> getSearchHistory(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        String key = SEARCH_HISTORY_KEY_PREFIX + userId;
        List<String> history = new ArrayList<>();
        
        try {
            // 获取最近的搜索记录，按时间倒序
            Set<Object> keywords = redisTemplate.opsForZSet().reverseRange(key, 0, MAX_HISTORY_SIZE - 1);
            
            if (keywords != null) {
                for (Object keyword : keywords) {
                    if (keyword != null) {
                        history.add(keyword.toString());
                    }
                }
            }
            
            System.out.println("获取搜索记录成功: userId=" + userId + ", size=" + history.size());
            
        } catch (Exception e) {
            System.err.println("获取搜索记录失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history;
    }
    
    @Override
    public void clearSearchHistory(Long userId) {
        if (userId == null) {
            return;
        }
        
        String key = SEARCH_HISTORY_KEY_PREFIX + userId;
        
        try {
            redisTemplate.delete(key);
            System.out.println("清空搜索记录成功: userId=" + userId);
            
        } catch (Exception e) {
            System.err.println("清空搜索记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void removeSearchHistory(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        
        String key = SEARCH_HISTORY_KEY_PREFIX + userId;
        String trimmedKeyword = keyword.trim();
        
        try {
            redisTemplate.opsForZSet().remove(key, trimmedKeyword);
            System.out.println("删除搜索记录成功: userId=" + userId + ", keyword=" + trimmedKeyword);
            
        } catch (Exception e) {
            System.err.println("删除搜索记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
