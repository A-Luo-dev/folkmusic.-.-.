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
 * 帖子附件表
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("post_attachments")
public class PostAttachments implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "attachment_id", type = IdType.AUTO)
    private Long attachmentId;

    @TableField("post_id")
    private Long postId;

    /**
     * 附件类型：image/video/audio
     */
    @TableField("type")
    private String type;

    /**
     * 附件路径或链接
     */
    @TableField("url")
    private String url;

    @TableField("created_at")
    private LocalDateTime createdAt;


}
