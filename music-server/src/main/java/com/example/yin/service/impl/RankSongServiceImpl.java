package com.example.yin.service.impl;

import com.example.yin.mapper.RankSongDetailMapper;
import com.example.yin.mapper.RankSongMapper;
import com.example.yin.mapper.SongMapper;
import com.example.yin.model.domain.RankSong;
import com.example.yin.model.domain.RankSongDetail;
import com.example.yin.model.domain.Song;
import com.example.yin.model.vo.PlaylistVo;
import com.example.yin.model.vo.SongVo;
import com.example.yin.service.RankSongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankSongServiceImpl implements RankSongService {

    @Autowired
    private RankSongMapper rankSongMapper;

    @Autowired
    private RankSongDetailMapper rankSongDetailMapper;

    @Autowired
    private SongMapper songMapper;

    @Override
    public List<RankSong> getAllRankSong() {
        return rankSongMapper.selectList(null);
    }

    @Override
    public PlaylistVo getPlaylistByRankId(Long rankSongId) {
        RankSong rankSong = rankSongMapper.selectById(rankSongId);
        if (rankSong == null) return null;

        List<RankSongDetail> details = rankSongDetailMapper.getSongsByRankId(rankSongId);

        List<SongVo> songs = details.stream().map(detail -> {
            Song song = songMapper.selectById(detail.getSongId());
            if (song == null) return null;

            SongVo vo = new SongVo();
            vo.setId(song.getId());
            vo.setName(song.getName());
            vo.setUrl(song.getUrl());
            vo.setPic(song.getPic());
            vo.setSequence(detail.getSequence()); // ✅ 直接赋值 Integer
            return vo;
        }).filter(vo -> vo != null).collect(Collectors.toList());




        PlaylistVo playlist = new PlaylistVo();
        playlist.setId(rankSong.getId());
        playlist.setName(rankSong.getName());
        playlist.setCoverImgUrl(rankSong.getCoverImgUrl());
        playlist.setDescription(rankSong.getDescription());  // 设置榜单简介
        playlist.setSongs(songs);

        return playlist;
    }

}
