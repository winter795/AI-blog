package com.personalblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalblog.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
