package com.personalblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalblog.entity.SiteStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SiteStatMapper extends BaseMapper<SiteStat> {

    @Select("SELECT COALESCE(SUM(pv), 0) FROM site_stat")
    long sumPV();
}
