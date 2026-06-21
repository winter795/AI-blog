package com.personalblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("site_config")
public class SiteConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private String configValue;
}
