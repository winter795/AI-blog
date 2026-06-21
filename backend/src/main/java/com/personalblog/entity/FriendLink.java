package com.personalblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend_link")
public class FriendLink {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String url;
    private String description;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
