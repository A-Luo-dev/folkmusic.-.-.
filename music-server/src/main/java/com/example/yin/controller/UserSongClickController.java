package com.example.yin.controller;

import com.example.yin.model.domain.UserSongClick;
import com.example.yin.service.UserSongClickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户点击歌曲日志表 前端控制器
 * </p>
 *
 * @author Byterain
 * @since 2025-09-03
 */
@RestController
@RequestMapping("/user_song_click")
public class UserSongClickController {

    @Autowired
    private UserSongClickService clickService;

    /**
     * 记录用户点击
     */
    @PostMapping("/add")
    public String addClick(@RequestBody UserSongClick click) {
        System.out.println("🔥 接收到的点击记录: userId= " + click.getUserId() + ", songId=" + click.getSongId());
        clickService.addClick(click.getUserId(), click.getSongId());
        return "点击记录成功";
    }


    /**
     * 查询用户最近点击记录
     */
    @GetMapping("/recent/{userId}")
    public List<UserSongClick> getRecent(@PathVariable Integer userId) {
        return clickService.findRecentByUser(userId);
    }
}
