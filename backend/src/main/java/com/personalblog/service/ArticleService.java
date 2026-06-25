package com.personalblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.entity.Article;
import com.personalblog.entity.ArticleTag;
import com.personalblog.mapper.ArticleMapper;
import com.personalblog.mapper.ArticleTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final AiSearchService searchService;

    public ArticleService(ArticleMapper articleMapper, ArticleTagMapper articleTagMapper,
                          AiSearchService searchService) {
        this.articleMapper = articleMapper;
        this.articleTagMapper = articleTagMapper;
        this.searchService = searchService;
    }

    public Page<Article> page(int pageNum, int pageSize, String keyword, Integer status, Long categoryId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                          .or().like(Article::getContent, keyword)
                          .or().like(Article::getSummary, keyword));
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Article::getCreatedAt);
        return articleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Transactional
    public void save(Article article, List<Long> tagIds) {
        articleMapper.insert(article);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
        searchService.refreshIndex();
    }

    @Transactional
    public void update(Article article, List<Long> tagIds) {
        articleMapper.updateById(article);
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, article.getId());
        articleTagMapper.delete(wrapper);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
        searchService.refreshIndex();
    }

    @Transactional
    public void delete(Long id) {
        articleMapper.deleteById(id);
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(wrapper);
        searchService.refreshIndex();
    }

    public void toggleStatus(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            article.setStatus(article.getStatus() == 1 ? 0 : 1);
            articleMapper.updateById(article);
            searchService.refreshIndex();
        }
    }

    public Article getById(Long id) {
        return articleMapper.selectById(id);
    }

    public List<Long> getTagIds(Long articleId) {
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        return articleTagMapper.selectList(wrapper).stream()
                .map(ArticleTag::getTagId).toList();
    }
}
