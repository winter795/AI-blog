package com.personalblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("site_stat")
public class SiteStat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Long pv;
    private Long uv;
}
