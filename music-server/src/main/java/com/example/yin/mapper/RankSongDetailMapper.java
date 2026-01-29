package com.example.yin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yin.model.domain.RankSongDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankSongDetailMapper extends BaseMapper<RankSongDetail> {

    // 根据榜单ID获取歌曲列表（按顺序）
    List<RankSongDetail> getSongsByRankId(@Param("rankSongId") Long rankSongId);
}
