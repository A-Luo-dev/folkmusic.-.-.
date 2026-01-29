package com.example.yin.service;

import java.util.List;

/**
 * 搜索记录服务接口
 */
public interface SearchHistoryService {
    
    /**
     * 添加搜索记录
     * @param userId 用户ID
     * @param keyword 搜索关键词
     */
    void addSearchHistory(Long userId, String keyword);
    
    /**
     * 获取用户搜索记录
     * @param userId 用户ID
     * @return 搜索记录列表（最多10条）
     */
    List<String> getSearchHistory(Long userId);
    
    /**
     * 清空用户搜索记录
     * @param userId 用户ID
     */
    void clearSearchHistory(Long userId);
    
    /**
     * 删除单条搜索记录
     * @param userId 用户ID
     * @param keyword 要删除的关键词
     */
    void removeSearchHistory(Long userId, String keyword);
}
