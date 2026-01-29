package com.example.yin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yin.model.domain.UserSongClick;

import java.util.List;

/**
 * <p>
 * 用户点击歌曲日志表 服务类
 * </p>
 *
 * @author Byterain
 * @since 2025-09-03
 */
public interface UserSongClickService extends IService<UserSongClick> {

    /**
     * 添加点击记录
     */

    void addClick(Integer userId, Integer songId);

    /**
     * 查询某个用户最近点击记录
     */
    List<UserSongClick> findRecentByUser(Integer userId);
}
