package com.example.yin.controller;

import com.example.yin.service.SearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索记录控制器
 */
@RestController
@RequestMapping("/search-history")
public class SearchHistoryController {
    
    @Autowired
    private SearchHistoryService searchHistoryService;
    
    /**
     * 添加搜索记录
     * POST /search-history/add
     * Header: X-User-Id: <用户ID>
     * Body: { "keyword": "搜索关键词" }
     */
    @PostMapping("/add")
    public Map<String, Object> addSearchHistory(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userId == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return response;
            }
            
            String keyword = request.get("keyword");
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "搜索关键词不能为空");
                return response;
            }
            
            searchHistoryService.addSearchHistory(userId, keyword);
            
            response.put("success", true);
            response.put("message", "搜索记录添加成功");
            
        } catch (Exception e) {
            System.err.println("添加搜索记录异常: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "添加搜索记录失败");
        }
        
        return response;
    }
    
    /**
     * 获取用户搜索记录
     * GET /search-history/list
     * Header: X-User-Id: <用户ID>
     */
    @GetMapping("/list")
    public Map<String, Object> getSearchHistory(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userId == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                response.put("data", new ArrayList<>());
                return response;
            }
            
            List<String> history = searchHistoryService.getSearchHistory(userId);
            
            response.put("success", true);
            response.put("message", "获取搜索记录成功");
            response.put("data", history);
            
        } catch (Exception e) {
            System.err.println("获取搜索记录异常: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取搜索记录失败");
            response.put("data", new ArrayList<>());
        }
        
        return response;
    }
    
    /**
     * 清空用户搜索记录
     * DELETE /search-history/clear
     * Header: X-User-Id: <用户ID>
     */
    @DeleteMapping("/clear")
    public Map<String, Object> clearSearchHistory(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userId == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return response;
            }
            
            searchHistoryService.clearSearchHistory(userId);
            
            response.put("success", true);
            response.put("message", "搜索记录清空成功");
            
        } catch (Exception e) {
            System.err.println("清空搜索记录异常: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "清空搜索记录失败");
        }
        
        return response;
    }
    
    /**
     * 删除单条搜索记录
     * DELETE /search-history/remove
     * Header: X-User-Id: <用户ID>
     * Body: { "keyword": "要删除的关键词" }
     */
    @DeleteMapping("/remove")
    public Map<String, Object> removeSearchHistory(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userId == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return response;
            }
            
            String keyword = request.get("keyword");
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "搜索关键词不能为空");
                return response;
            }
            
            searchHistoryService.removeSearchHistory(userId, keyword);
            
            response.put("success", true);
            response.put("message", "搜索记录删除成功");
            
        } catch (Exception e) {
            System.err.println("删除搜索记录异常: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "删除搜索记录失败");
        }
        
        return response;
    }
}
