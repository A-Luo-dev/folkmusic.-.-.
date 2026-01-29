package com.example.yin.controller;

import com.example.yin.model.request.PostCreateRequest;
import com.example.yin.model.vo.PostDetailVO;
import com.example.yin.model.vo.PostSummaryVO;
import com.example.yin.service.ConsumerService;
import com.example.yin.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * <p>
 * 社区帖子表 前端控制器
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostsController {

    @Autowired
    private PostsService postsService;

    @Autowired
    private ConsumerService consumerService; // ✅ 你的用户服务

    /**
     * 获取所有帖子列表
     * GET /posts
     */
    @GetMapping
    public List<PostSummaryVO> getAll() {
        System.out.println("获取所有帖子列表");
        List<PostSummaryVO> posts = postsService.getAllPosts();
        System.out.println("返回帖子数量: " + posts.size());
        return posts;
    }


    /**
     * 获取帖子详情（含附件与评论）
     * GET /posts/{id}
     */
    @GetMapping("/{id}")
    public PostDetailVO getDetail(@PathVariable Long id) {
        System.out.println("获取帖子详情，ID: " + id);
        PostDetailVO vo = postsService.getDetail(id);
        if (vo == null) {
            throw new RuntimeException("帖子不存在");
        }
        System.out.println("返回帖子详情: " + vo);
        return vo;
    }

    @PostMapping
    public Map<String, Object> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                      @Validated @RequestBody PostCreateRequest req) {

        System.out.println("收到发帖请求，userId: " + userId);
        System.out.println("请求数据: " + req);

        // 校验登录
        if (userId == null || consumerService.getById(userId) == null) {
            System.out.println("用户未登录或不存在: " + userId);
            throw new RuntimeException("请先登录！");
        }

        if (!StringUtils.hasText(req.getContent())) {
            throw new RuntimeException("帖子内容不能为空");
        }

        Long id = postsService.createPost(req, userId);
        System.out.println("帖子创建成功，ID: " + id);

        Map<String, Object> resp = new HashMap<>();
        resp.put("postId", id);
        resp.put("success", true);
        return resp;
    }



    /**
     * 删除帖子（作者或管理员）
     * DELETE /posts/{id}
     * Header: X-User-Id: <用户ID> (可选)
     * Header: X-Admin: true (可选)
     * 或通过Session验证管理员身份
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id,
                                      @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                      @RequestHeader(value = "X-Admin", required = false) String adminHeader,
                                      javax.servlet.http.HttpSession session) {
        
        System.out.println("删除帖子请求: postId=" + id + ", userId=" + userId + ", adminHeader=" + adminHeader);
        
        // 检查是否是管理员（通过Session或Header）
        boolean isAdmin = false;
        Long operatorUserId = userId;
        
        // 方式1: 检查Session中是否有管理员登录信息
        String adminName = (String) session.getAttribute("name");
        if (adminName != null) {
            System.out.println("管理员通过Session验证: " + adminName);
            isAdmin = true;
            // 管理员删除时，如果没有提供userId，使用管理员ID（这里简化处理，使用-1表示管理员操作）
            if (operatorUserId == null) {
                operatorUserId = -1L;
            }
        }
        
        // 方式2: 检查Header中的X-Admin标记
        if ("true".equalsIgnoreCase(adminHeader)) {
            System.out.println("管理员通过Header验证");
            isAdmin = true;
        }
        
        if (!isAdmin && operatorUserId == null) {
            throw new RuntimeException("请先登录");
        }
        
        postsService.deletePost(id, operatorUserId, isAdmin);
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "删除成功");
        return resp;
    }
}
