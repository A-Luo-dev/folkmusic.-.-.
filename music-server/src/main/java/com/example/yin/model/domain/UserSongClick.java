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
 * 用户点击歌曲日志表
 * </p>
 *
 * @author Byterain
 * @since 2025-09-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_song_click")
public class UserSongClick implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Integer userId;

    @TableField("song_id")
    private Integer songId;

    @TableField("click_time")
    private LocalDateTime clickTime;


}
