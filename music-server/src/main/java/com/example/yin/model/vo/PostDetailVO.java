package com.example.yin.model.vo;

import com.example.yin.model.domain.PostAttachments;
import com.example.yin.model.domain.PostComments;
import com.example.yin.model.domain.Posts;
import lombok.Data;

import java.util.List;

@Data
public class PostDetailVO {
    private Posts post;
    private List<PostAttachments> attachments;
    private List<PostComments> comments;
    
    // 新增字段
    private String username;          // 发帖人用户名
    private String userAvatar;        // 发帖人头像
}
