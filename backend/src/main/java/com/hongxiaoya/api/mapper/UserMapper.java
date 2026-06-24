package com.hongxiaoya.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongxiaoya.api.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
