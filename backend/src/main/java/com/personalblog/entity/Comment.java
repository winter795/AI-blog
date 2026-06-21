package com.personalblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String author;
    private String email;
    private String content;
    private Integer status;  // 0=待审核 1=通过 2=拒绝
    private Long parentId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
