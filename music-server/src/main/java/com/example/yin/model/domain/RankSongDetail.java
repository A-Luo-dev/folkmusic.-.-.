package com.example.yin.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("rank_song_detail")
public class RankSongDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long rankSongId; // 对应 RankSong 的 ID
    private Long songId;     // 对应 Song 表的 ID
    private Integer sequence; // 歌曲在榜单里的顺序
}
