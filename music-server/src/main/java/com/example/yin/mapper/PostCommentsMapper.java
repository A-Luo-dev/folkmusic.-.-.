package com.example.yin.mapper;

import com.example.yin.model.domain.PostComments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 帖子评论表 Mapper 接口
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
@Mapper
public interface PostCommentsMapper extends BaseMapper<PostComments> {

}
