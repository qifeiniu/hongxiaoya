package com.hongxiaoya.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongxiaoya.api.entity.Post;
import com.hongxiaoya.api.mapper.PostMapper;
import com.hongxiaoya.api.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    @Override
    public List<Post> getPostList() {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        return postMapper.selectList(queryWrapper);
    }

    @Override
    public void createPost(Long userId, Post post) {
        // 1. 字数限制验证
        if (post.getContent() != null && post.getContent().length() > 500) {
            throw new RuntimeException("动态内容不能超过500字");
        }

        // 2. 模拟百度内容审核过滤 (敏感词过滤)
        checkContentCompliance(post.getContent());

        post.setUserId(userId);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreatedAt(new Date());
        postMapper.insert(post);
    }

    private void checkContentCompliance(String content) {
        if (content == null) return;
        // 模拟敏感词库
        String[] sensitiveWords = {"代开发票", "涉黄", "赌博", "毒品"};
        for (String word : sensitiveWords) {
            if (content.contains(word)) {
                log.warn("发现敏感词: {}", word);
                throw new RuntimeException("内容包含敏感词汇，发布失败");
            }
        }
    }

    @Override
    public void likePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
        }
    }
}
