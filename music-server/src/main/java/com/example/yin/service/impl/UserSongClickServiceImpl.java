package com.example.yin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yin.mapper.UserSongClickMapper;
import com.example.yin.model.domain.UserSongClick;
import com.example.yin.service.UserSongClickService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户点击歌曲日志表 服务实现类
 * </p>
 *
 * @author Byterain
 * @since 2025-09-03
 */
@Service
@Transactional
public class UserSongClickServiceImpl extends ServiceImpl<UserSongClickMapper, UserSongClick>
        implements UserSongClickService {

    @Override
    public void addClick(Integer userId, Integer songId) {
        UserSongClick click = new UserSongClick();
        click.setUserId(userId);
        click.setSongId(songId);
        // 将 Date 转换为 LocalDateTime
        Date date = new Date();
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        click.setClickTime(localDateTime);
        this.save(click);
    }

    @Override
    public List<UserSongClick> findRecentByUser(Integer userId) {
        return this.list(new LambdaQueryWrapper<UserSongClick>()
                .eq(UserSongClick::getUserId, userId)
                .orderByDesc(UserSongClick::getClickTime)
                .last("LIMIT 20")); // 默认取最近20条
    }
}
