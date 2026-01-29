package com.example.yin.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 帖子评论表
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("post_comments")
public class PostComments implements Serializable {
    @TableId(value = "comment_id")  // 这里告诉MP主键是 post_id

    private static final long serialVersionUID = 1L;

    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    @TableField("post_id")
    private Long postId;

    @TableField("user_id")
    private Integer userId;

    /**
     * 0表示顶级评论
     */
    @TableField("parent_id")
    private Long parentId;

    @TableField("content")
    private String content;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("created_at")
    private LocalDateTime createdAt;


}
