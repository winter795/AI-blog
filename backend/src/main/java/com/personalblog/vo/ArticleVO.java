package com.personalblog.vo;

import com.personalblog.entity.Article;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章前端展示对象 — 隐藏 deleted、embedding 等内部字段。
 */
@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long categoryId;
    private Integer status;
    private Integer isTop;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ArticleVO from(Article a) {
        if (a == null) return null;
        ArticleVO vo = new ArticleVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setSummary(a.getSummary());
        vo.setContent(a.getContent());
        vo.setCategoryId(a.getCategoryId());
        vo.setStatus(a.getStatus());
        vo.setIsTop(a.getIsTop());
        vo.setViewCount(a.getViewCount());
        vo.setLikeCount(a.getLikeCount());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        return vo;
    }
}
