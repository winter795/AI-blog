package com.personalblog.dto;

import lombok.Data;
import java.util.List;

/**
 * 文章保存/更新请求体 — tagIds 放在 body 中而非 query string。
 */
@Data
public class ArticleSaveRequest {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long categoryId;
    private Integer status;
    private List<Long> tagIds;
}
