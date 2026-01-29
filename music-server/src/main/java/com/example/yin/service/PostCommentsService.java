package com.example.yin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yin.model.domain.PostComments;
import java.util.List;
/**
 * <p>
 * 帖子评论表 服务类
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
public interface PostCommentsService extends IService<PostComments> {
    List<PostComments> listByPostId(Long postId);
    boolean deleteComment(Long commentId, Integer userId);
}
