package com.example.yin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yin.mapper.PostCommentsMapper;
import com.example.yin.model.domain.PostComments;
import com.example.yin.service.PostCommentsService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 * 帖子评论表 服务实现类
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
@Service
public class PostCommentsServiceImpl extends ServiceImpl<PostCommentsMapper, PostComments>
        implements PostCommentsService {

    @Override
    public List<PostComments> listByPostId(Long postId) {
        QueryWrapper<PostComments> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", postId);
        wrapper.orderByAsc("created_at");
        return list(wrapper);
    }

    @Override
    public boolean deleteComment(Long commentId, Integer userId) {
        // 仅允许本人删除，可扩展管理员逻辑
        PostComments comment = getById(commentId);
        if (comment == null) return false;
        if (!comment.getUserId().equals(userId)) return false;
        return removeById(commentId);
    }
}

