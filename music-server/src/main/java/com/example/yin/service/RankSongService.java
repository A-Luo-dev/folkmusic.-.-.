package com.example.yin.service;

import com.example.yin.model.domain.RankSong;
import com.example.yin.model.vo.PlaylistVo;
import java.util.List;

public interface RankSongService {

    // 获取所有排行榜
    List<RankSong> getAllRankSong();

    // 根据排行榜ID获取歌曲详情（返回前端可用 VO）
    PlaylistVo getPlaylistByRankId(Long rankSongId);
}
