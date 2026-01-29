package com.example.yin.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;




/**
 * <p>
 * 社区帖子表
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("posts")
public class Posts implements Serializable {

    @TableId(value = "post_id")  // 这里告诉MP主键是 post_id

    private static final long serialVersionUID = 1L;

    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;

    @TableField("user_id")
    private Integer userId;

    @TableField("tags")
    @Column(columnDefinition = "TEXT")
    private String tags; // 存储 ["原创","吉他弹唱"]


    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    /**
     * 0=非原创,1=原创
     */
    @TableField("is_original")
    private Boolean isOriginal;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;


}
