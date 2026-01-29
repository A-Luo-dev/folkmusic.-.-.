package com.example.yin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子摘要信息（用于帖子列表）
 */
@Data
public class PostSummaryVO {
    private Long postId;
    private String title;
    private String content;   // 这里可以考虑只截取前 N 个字
    private Integer userId;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private Integer attachmentCount;  // 附件数量
    private LocalDateTime createdAt;
    
    // 新增字段
    private List<String> tags;        // 标签列表
    private String username;          // 发帖人用户名
    private String userAvatar;        // 发帖人头像
}
