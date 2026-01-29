package com.example.yin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yin.mapper.PostsMapper;
import com.example.yin.model.domain.PostAttachments;
import com.example.yin.model.domain.PostComments;
import com.example.yin.model.domain.Posts;
import com.example.yin.model.request.PostCreateRequest;
import com.example.yin.model.vo.PostDetailVO;
import com.example.yin.model.vo.PostSummaryVO;
import com.example.yin.service.PostAttachmentsService;
import com.example.yin.service.PostCommentsService;
import com.example.yin.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import com.alibaba.fastjson.JSON;
import com.example.yin.service.ConsumerService;
import com.example.yin.model.domain.Consumer;

/**
 * <p>
 * 社区帖子表 服务实现类（已调整与实体字段一致）
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
@Service
@RequiredArgsConstructor
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts> implements PostsService {

    private final PostAttachmentsService postAttachmentsService;
    private final PostCommentsService postCommentsService;
    private final ConsumerService consumerService;

    @Override
    public PostDetailVO getDetail(Long postId) {
        Posts post = this.getById(postId);
        if (post == null) return null;

        // 浏览量 +1
        if (post.getViewCount() == null) post.setViewCount(1);
        else post.setViewCount(post.getViewCount() + 1);
        this.updateById(post);

        List<PostAttachments> attachments = postAttachmentsService.list(
                new LambdaQueryWrapper<PostAttachments>()
                        .eq(PostAttachments::getPostId, postId)
                        .orderByAsc(PostAttachments::getAttachmentId) // 用 attachmentId 排序
        );

        List<PostComments> comments = postCommentsService.list(
                new LambdaQueryWrapper<PostComments>()
                        .eq(PostComments::getPostId, postId)
                        .orderByAsc(PostComments::getCreatedAt)
        );

        PostDetailVO vo = new PostDetailVO();
        vo.setPost(post);
        vo.setAttachments(attachments);
        vo.setComments(comments);
        
        // 设置发帖人信息
        if (post.getUserId() != null) {
            try {
                Consumer user = consumerService.getById(post.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                    vo.setUserAvatar(user.getAvator());
                    System.out.println("设置详情用户名: " + user.getUsername() + " for post: " + post.getPostId());
                } else {
                    System.out.println("未找到用户: " + post.getUserId());
                    vo.setUsername("用户" + post.getUserId());
                }
            } catch (Exception e) {
                System.err.println("获取用户信息失败: " + e.getMessage());
                vo.setUsername("用户" + post.getUserId());
            }
        } else {
            vo.setUsername("匿名用户");
        }
        
        return vo;
    }


    @Override
    public List<PostSummaryVO> getAllPosts() {
        List<Posts> posts = this.list(
                new LambdaQueryWrapper<Posts>()
                        .orderByDesc(Posts::getCreatedAt) // 按创建时间倒序
        );

        return posts.stream().map(p -> {
            PostSummaryVO vo = new PostSummaryVO();
            vo.setPostId(p.getPostId());
            vo.setTitle(p.getTitle());
            // 截取前 50 字做摘要（可改）
            String content = p.getContent();
            if (content != null && content.length() > 50) {
                content = content.substring(0, 50) + "...";
            }
            vo.setContent(content);
            vo.setUserId(p.getUserId());
            vo.setLikeCount(p.getLikeCount());
            
            // 实时统计评论数
            long actualCommentCount = postCommentsService.count(
                new LambdaQueryWrapper<PostComments>()
                    .eq(PostComments::getPostId, p.getPostId())
            );
            vo.setCommentCount((int) actualCommentCount);
            System.out.println("帖子 " + p.getPostId() + " 评论数: " + actualCommentCount);
            
            // 实时统计附件数
            long actualAttachmentCount = postAttachmentsService.count(
                new LambdaQueryWrapper<PostAttachments>()
                    .eq(PostAttachments::getPostId, p.getPostId())
            );
            vo.setAttachmentCount((int) actualAttachmentCount);
            System.out.println("帖子 " + p.getPostId() + " 附件数: " + actualAttachmentCount);
            
            vo.setViewCount(p.getViewCount());
            vo.setCreatedAt(p.getCreatedAt());
            System.out.println("帖子 " + p.getPostId() + " 创建时间: " + p.getCreatedAt());
            
            // 设置标签
            if (p.getTags() != null && !p.getTags().trim().isEmpty()) {
                try {
                    List<String> tags = JSON.parseArray(p.getTags(), String.class);
                    vo.setTags(tags);
                    System.out.println("解析标签: " + p.getTags() + " -> " + tags);
                } catch (Exception e) {
                    System.err.println("标签解析失败: " + p.getTags() + ", 错误: " + e.getMessage());
                    vo.setTags(Collections.emptyList());
                }
            } else {
                System.out.println("帖子 " + p.getPostId() + " 没有标签");
                vo.setTags(Collections.emptyList());
            }
            
            // 设置发帖人信息
            if (p.getUserId() != null) {
                try {
                    Consumer user = consumerService.getById(p.getUserId());
                    if (user != null) {
                        vo.setUsername(user.getUsername());
                        vo.setUserAvatar(user.getAvator());
                        System.out.println("设置用户名: " + user.getUsername() + " for post: " + p.getPostId());
                    } else {
                        System.out.println("未找到用户: " + p.getUserId());
                        vo.setUsername("用户" + p.getUserId());
                    }
                } catch (Exception e) {
                    System.err.println("获取用户信息失败: " + e.getMessage());
                    vo.setUsername("用户" + p.getUserId());
                }
            } else {
                vo.setUsername("匿名用户");
            }
            
            return vo;
        }).collect(Collectors.toList());
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createPost(PostCreateRequest req, Long userId) {
        Posts post = new Posts();
        post.setUserId(userId.intValue());
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setIsOriginal(Boolean.TRUE.equals(req.getIsOriginal()));
        
        // 保存标签为JSON字符串
        if (req.getTags() != null && !req.getTags().isEmpty()) {
            String tagsJson = JSON.toJSONString(req.getTags());
            post.setTags(tagsJson);
            System.out.println("保存标签: " + tagsJson);
        } else {
            System.out.println("没有标签需要保存");
        }

        // 初始化计数字段
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setViewCount(0);

        // 时间字段
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        this.save(post);

        // 处理附件：更新已上传附件的postId
        if (req.getAttachments() != null && !req.getAttachments().isEmpty()) {
            System.out.println("开始处理附件，数量: " + req.getAttachments().size());
            for (PostCreateRequest.AttachmentDTO attachmentDTO : req.getAttachments()) {
                if (attachmentDTO.getAttachmentId() != null) {
                    // 更新已上传附件的postId
                    PostAttachments existingAttachment = postAttachmentsService.getById(attachmentDTO.getAttachmentId());
                    if (existingAttachment != null) {
                        existingAttachment.setPostId(post.getPostId());
                        postAttachmentsService.updateById(existingAttachment);
                        System.out.println("更新附件ID " + attachmentDTO.getAttachmentId() + " 的postId为 " + post.getPostId());
                    } else {
                        System.out.println("未找到附件ID: " + attachmentDTO.getAttachmentId());
                    }
                } else {
                    // 创建新的附件记录
                    PostAttachments pa = new PostAttachments();
                    pa.setPostId(post.getPostId());
                    pa.setType(attachmentDTO.getType());
                    pa.setUrl(attachmentDTO.getUrl());
                    pa.setCreatedAt(LocalDateTime.now());
                    postAttachmentsService.save(pa);
                    System.out.println("创建新附件记录");
                }
            }
            System.out.println("处理了 " + req.getAttachments().size() + " 个附件");
        } else {
            System.out.println("没有附件需要保存");
        }
        return post.getPostId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePost(Long postId, Long operatorUserId, boolean isAdmin) {
        Posts post = this.getById(postId);
        if (post == null) return;

        // 仅作者或管理员可删
        if (!isAdmin && (post.getUserId() == null || post.getUserId().longValue() != operatorUserId.longValue())) {
            throw new RuntimeException("无权限删除该帖子");
        }
        System.out.println("DB userId=" + post.getUserId() +
                " (" + post.getUserId().getClass().getName() + ")" +
                ", Header userId=" + operatorUserId +
                " (" + operatorUserId.getClass().getName() + ")");

        // 删除附件和评论
        postAttachmentsService.remove(new LambdaQueryWrapper<PostAttachments>()
                .eq(PostAttachments::getPostId, postId));
        postCommentsService.remove(new LambdaQueryWrapper<PostComments>()
                .eq(PostComments::getPostId, postId));
        this.removeById(postId);
    }

}
