package com.hongxiaoya.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongxiaoya.api.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
