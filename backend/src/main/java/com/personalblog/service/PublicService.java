package com.personalblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.entity.*;
import com.personalblog.mapper.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final FriendLinkMapper friendLinkMapper;

    public PublicService(ArticleMapper articleMapper, ArticleTagMapper articleTagMapper,
                         CategoryMapper categoryMapper, TagMapper tagMapper,
                         FriendLinkMapper friendLinkMapper) {
        this.articleMapper = articleMapper;
        this.articleTagMapper = articleTagMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.friendLinkMapper = friendLinkMapper;
    }

    public Page<Article> articlePage(int pageNum, int pageSize, Long categoryId, Long tagId, String keyword) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);
        if (categoryId != null) wrapper.eq(Article::getCategoryId, categoryId);
        if (keyword != null && !keyword.isBlank())
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword).or().like(Article::getContent, keyword));
        wrapper.orderByDesc(Article::getIsTop).orderByDesc(Article::getCreatedAt);

        if (tagId != null) {
            List<Long> articleIds = articleTagMapper.selectList(
                    new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tagId))
                    .stream().map(ArticleTag::getArticleId).toList();
            if (articleIds.isEmpty()) return new Page<>(pageNum, pageSize, 0);
            wrapper.in(Article::getId, articleIds);
        }
        return articleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public Article getArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null && article.getStatus() == 1) {
            article.setViewCount(article.getViewCount() + 1);
            articleMapper.updateById(article);
        }
        return article;
    }

    public List<Article> topArticles() {
        return articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1).eq(Article::getIsTop, 1)
                .orderByDesc(Article::getCreatedAt));
    }

    public List<Article> latestArticles(int n) {
        return articleMapper.selectPage(
                new Page<>(1, n),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getCreatedAt)
        ).getRecords();
    }

    public List<Article> hotArticles(int n) {
        return articleMapper.selectPage(
                new Page<>(1, n),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getViewCount)
        ).getRecords();
    }

    public Article getPrevArticle(Long id) {
        if (articleMapper.selectById(id) == null) return null;
        var page = articleMapper.selectPage(
                new Page<>(1, 1),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1).lt(Article::getId, id)
                        .orderByDesc(Article::getId));
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    public Article getNextArticle(Long id) {
        if (articleMapper.selectById(id) == null) return null;
        var page = articleMapper.selectPage(
                new Page<>(1, 1),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1).gt(Article::getId, id)
                        .orderByAsc(Article::getId));
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    public List<Tag> getArticleTags(Long articleId) {
        List<Long> tagIds = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId))
                .stream().map(ArticleTag::getTagId).toList();
        if (tagIds.isEmpty()) return List.of();
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().in(Tag::getId, tagIds));
    }

    public List<Category> allCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
    }

    public List<Tag> allTags() {
        return tagMapper.selectList(new LambdaQueryWrapper<>());
    }

    public List<FriendLink> friendLinks() {
        return friendLinkMapper.selectList(
                new LambdaQueryWrapper<FriendLink>().orderByAsc(FriendLink::getSortOrder));
    }
}
