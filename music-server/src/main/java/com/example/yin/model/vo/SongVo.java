package com.example.yin.model.vo;

import lombok.Data;

@Data
public class SongVo {
    private Long id;      // 必须是 Long 或者 Integer，和 song.getId() 类型一致
    private String name;
    private String url;
    private String pic;
    private Integer sequence; // 或者 Long，根据你后端 RankSongDetail.sequence 类型决定
}
