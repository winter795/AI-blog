package com.personalblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalblog.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
