package com.hongxiaoya.api.service;

import com.hongxiaoya.api.entity.Post;

import java.util.List;

public interface PostService {
    List<Post> getPostList();
    void createPost(Long userId, Post post);
    void likePost(Long userId, Long postId);
}
