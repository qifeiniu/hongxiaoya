package com.hongxiaoya.api.service.impl;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeIdcardRequest;
import com.aliyun.ocr_api20210707.models.RecognizeIdcardResponse;
import com.aliyun.teaopenapi.models.Config;
import com.hongxiaoya.api.config.properties.AliyunProperties;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongxiaoya.api.config.properties.AliyunProperties;
import com.hongxiaoya.api.dto.UpdateProfileReq;
import com.hongxiaoya.api.entity.User;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.mapper.UserMapper;
import com.hongxiaoya.api.mapper.UserProfileMapper;
import com.hongxiaoya.api.service.ProfileService;
import com.hongxiaoya.api.service.RelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserProfileMapper userProfileMapper;
    private final UserMapper userMapper;
    private final AliyunProperties aliyunProperties;
    @Lazy
    private final RelationService relationService;

    @Override
    public UserProfile getProfile(Long userId) {
        return userProfileMapper.selectById(userId);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileReq req) {
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            BeanUtils.copyProperties(req, profile);
            userProfileMapper.insert(profile);
        } else {
            BeanUtils.copyProperties(req, profile);
            userProfileMapper.updateById(profile);
        }
    }

    @Override
    public void realNameAuth(Long userId, String frontImageUrl, String backImageUrl, String faceImageUrl) {
        try {
            // 模拟阿里云 OCR 调用
            Config config = new Config()
                    .setAccessKeyId(aliyunProperties.getAccessKeyId())
                    .setAccessKeySecret(aliyunProperties.getAccessKeySecret());
            config.endpoint = aliyunProperties.getOcr().getEndpoint();
            Client client = new Client(config);
            
            RecognizeIdcardRequest request = new RecognizeIdcardRequest()
                    .setUrl(frontImageUrl);

            // 真实环境下应开启发送
            // RecognizeIdcardResponse response = client.recognizeIdcard(request);
            // System.out.println(response.getBody().getData());
            
            log.info("【模拟OCR及活体检测】用户ID: {}, 身份证正面: {}", userId, frontImageUrl);
            
            // 更新用户认证状态
            UserProfile profile = userProfileMapper.selectById(userId);
            if (profile != null) {
                profile.setIsRealAuth(1);
                // 保存身份证号码等信息(此处省略具体字段)
                userProfileMapper.updateById(profile);
            }
        } catch (Exception e) {
            log.error("实名认证失败", e);
            throw new RuntimeException("实名认证失败: " + e.getMessage(), e);
        }
    }

    @Override
    public UserProfile getUserDetail(Long userId, Long targetUserId) {
        relationService.recordVisitor(targetUserId, userId);
        return userProfileMapper.selectById(targetUserId);
    }

    @Override
    @Transactional
    public void applyMatchmaker(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        
        if (user.getRole() != null && user.getRole() == 2) {
            throw new RuntimeException("您已经是红娘了");
        }
        
        // 检查申请条件
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null || profile.getIsRealAuth() == null || profile.getIsRealAuth() == 0) {
            throw new RuntimeException("请先完成实名认证");
        }
        
        // 检查邀请人数
        Long referralCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getReferrerId, userId));
        if (referralCount < 10) {
            throw new RuntimeException("需要成功邀请至少10名好友注册（当前: " + referralCount + "）");
        }
        
        // 模拟审核通过，直接设为红娘
        user.setRole(2);
        userMapper.updateById(user);
    }
}
