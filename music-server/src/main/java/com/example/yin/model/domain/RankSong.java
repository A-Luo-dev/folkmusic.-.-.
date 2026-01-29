package com.example.yin.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("rank_song")
public class RankSong {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;      // 榜单名，例如"飙升榜""热歌榜"
    private String coverImgUrl;  // 封面图片 URL
    private String type;      // 榜单类型，如 Top / Feature / Other
    
    @TableField("description")
    private String description;   // 榜单简介
    
    private String createTime;    // 创建时间（和表字段 create_time 对应）
}
