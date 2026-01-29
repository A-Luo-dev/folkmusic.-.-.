package com.example.yin.mapper;

import com.example.yin.model.domain.Posts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 社区帖子表 Mapper 接口
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */
@Mapper
public interface PostsMapper extends BaseMapper<Posts> {

}
