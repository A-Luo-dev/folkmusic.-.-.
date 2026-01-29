package com.example.yin.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class PlaylistVo {
    private Long id;
    private String name;
    private String coverImgUrl;
    private String description;  // 榜单简介
    private List<SongVo> songs;
}
