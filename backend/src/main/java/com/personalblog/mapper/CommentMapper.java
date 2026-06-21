package com.personalblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalblog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
