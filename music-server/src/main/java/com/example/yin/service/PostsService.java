package com.example.yin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yin.model.domain.Posts;
import com.example.yin.model.request.PostCreateRequest;
import com.example.yin.model.vo.PostDetailVO;
import com.example.yin.model.vo.PostSummaryVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 社区帖子表 服务类
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
public interface PostsService extends IService<Posts> {

    PostDetailVO getDetail(Long postId);

    Long createPost(PostCreateRequest req, Long userId);

    void deletePost(Long postId, Long operatorUserId, boolean isAdmin);

    Map<String, Object> getAllPosts(String keyword, Integer pagenum, Integer pagesize);  // 新增的方法
}