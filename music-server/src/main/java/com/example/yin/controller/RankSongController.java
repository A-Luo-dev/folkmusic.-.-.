package com.example.yin.controller;

import com.example.yin.common.R;
import com.example.yin.model.domain.RankSong;
import com.example.yin.service.RankSongService;
import com.example.yin.model.vo.PlaylistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rankSong")
public class RankSongController {

    @Autowired
    private RankSongService rankSongService;

    // 获取全部排行榜
    @GetMapping("/list")
    public R getAllRankSong() {
        List<RankSong> list = rankSongService.getAllRankSong();
        return R.success("排行榜列表获取成功", list);
    }
    //获取排行榜下歌曲内容
    @GetMapping("/detail")
    public R getRankSongDetail(@RequestParam Long rankSongId) {
        PlaylistVo playlist = rankSongService.getPlaylistByRankId(rankSongId);
        if (playlist == null) {
            return R.error("榜单不存在");
        }
        return R.success("榜单歌曲获取成功", playlist);
    }
}
