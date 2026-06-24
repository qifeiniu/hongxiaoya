package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.config.UserContext;
import com.hongxiaoya.api.entity.Post;
import com.hongxiaoya.api.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    public Result<List<Post>> getList() {
        return Result.success(postService.getPostList());
    }

    @PostMapping("/create")
    public Result<String> create(@RequestBody Post post) {
        Long userId = UserContext.getUserId();
        postService.createPost(userId, post);
        return Result.success("发布成功");
    }

    @PostMapping("/like")
    public Result<String> like(@RequestParam Long postId) {
        Long userId = UserContext.getUserId();
        postService.likePost(userId, postId);
        return Result.success("点赞成功");
    }
}
