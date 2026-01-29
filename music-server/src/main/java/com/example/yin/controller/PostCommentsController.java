package com.example.yin.controller;

import com.example.yin.model.domain.PostComments;
import com.example.yin.service.ConsumerService;
import com.example.yin.service.PostCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>
 * 帖子评论表 前端控制器
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
@RestController
@RequestMapping("/comments")
public class PostCommentsController {

    @Autowired
    private PostCommentsService commentsService;

    @Autowired
    private ConsumerService consumerService;

    /**
     * 获取帖子评论列表
     * GET /comments?postId=1
     */
    @GetMapping
    public List<PostComments> getComments(@RequestParam("postId") Long postId) {
        return commentsService.listByPostId(postId);
    }

    /**
     * 新增评论
     * POST /comments
     * Header: X-User-Id: <用户ID>
     * Body: { "postId":1, "parentId":0, "content":"好棒的帖子" }
     */
    @PostMapping
    public Map<String, Object> addComment(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                          @RequestBody Map<String, Object> req){

        // 校验登录
        if (userId == null || consumerService.getById(userId) == null) {
            throw new RuntimeException("请先登录！");
        }

        Object postIdObj = req.get("postId");
        if (postIdObj == null) {
            throw new RuntimeException("postId 不能为空！");
        }
        Long postId = Long.valueOf(postIdObj.toString());

        Object parentIdObj = req.get("parentId");
        Long parentId = (parentIdObj == null) ? 0L : Long.valueOf(parentIdObj.toString());

        String content = (String) req.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空！");
        }

        PostComments comment = new PostComments();
        comment.setPostId(postId);
        comment.setParentId(parentId);
        comment.setUserId((int) userId.longValue());
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());

        commentsService.save(comment);

        Map<String, Object> resp = new HashMap<>();
        resp.put("commentId", comment.getCommentId());
        resp.put("success", true);
        return resp;
    }


    /**
     * 删除评论
     * DELETE /comments/{commentId}
     * Header: X-User-Id: <用户ID>
     */
    @DeleteMapping("/{commentId}")
    public Map<String, Object> deleteComment(
            @RequestHeader("X-User-Id") Integer userId,
            @PathVariable Long commentId) {

        if (userId == null) {
            throw new RuntimeException("请先登录！");
        }

        boolean success = commentsService.deleteComment(commentId, userId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", success);
        return resp;
    }
}
