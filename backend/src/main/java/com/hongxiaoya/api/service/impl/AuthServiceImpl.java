package com.hongxiaoya.api.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.hutool.core.util.RandomUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teaopenapi.models.Config;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongxiaoya.api.common.JwtUtils;
import com.hongxiaoya.api.config.properties.AliyunProperties;
import com.hongxiaoya.api.dto.LoginReq;
import com.hongxiaoya.api.dto.WechatLoginReq;
import com.hongxiaoya.api.entity.EggLedger;
import com.hongxiaoya.api.entity.User;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.entity.Wallet;
import com.hongxiaoya.api.mapper.EggLedgerMapper;
import com.hongxiaoya.api.mapper.UserMapper;
import com.hongxiaoya.api.mapper.UserProfileMapper;
import com.hongxiaoya.api.mapper.WalletMapper;
import com.hongxiaoya.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final WalletMapper walletMapper;
    private final EggLedgerMapper eggLedgerMapper;
    private final UserProfileMapper userProfileMapper;
    private final StringRedisTemplate redisTemplate;
    private final AliyunProperties aliyunProperties;
    private final WxMaService wxMaService;

    private static final String SMS_CODE_PREFIX = "sms:code:";

    @Override
    public void sendSms(String phone) {
        // 限制发送频率 (60s)
        if (Boolean.TRUE.equals(redisTemplate.hasKey(SMS_CODE_PREFIX + phone))) {
            throw new RuntimeException("请勿频繁发送验证码");
        }

        String code = RandomUtil.randomNumbers(6);
        
        try {
            Config config = new Config()
                    .setAccessKeyId(aliyunProperties.getAccessKeyId())
                    .setAccessKeySecret(aliyunProperties.getAccessKeySecret());
            config.endpoint = "dysmsapi.aliyuncs.com";
            Client client = new Client(config);
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setSignName(aliyunProperties.getSms().getSignName())
                    .setTemplateCode(aliyunProperties.getSms().getTemplateCode())
                    .setPhoneNumbers(phone)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");
            
            // 真实环境下应开启发送
            // client.sendSms(sendSmsRequest);
            
            // 测试环境打印到控制台，不实际发送
            log.info("【模拟发送短信】手机号: {}, 验证码: {}", phone, code);
            
            // 存入 Redis，有效期 5 分钟
            redisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, code, 5, TimeUnit.MINUTES);
            
        } catch (Exception e) {
            log.error("发送短信失败", e);
            throw new RuntimeException("发送短信失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> loginByPhone(LoginReq req) {
        String phone = req.getPhone();
        String inputCode = req.getCode();

        // 万能验证码 123456 用于测试
        if (!"123456".equals(inputCode)) {
            String redisCode = redisTemplate.opsForValue().get(SMS_CODE_PREFIX + phone);
            if (redisCode == null || !redisCode.equals(inputCode)) {
                throw new RuntimeException("验证码错误或已过期");
            }
        }
        
        // 删除验证码
        redisTemplate.delete(SMS_CODE_PREFIX + phone);

        // 2. 查询用户是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
        
        boolean isNewUser = false;
        // 3. 不存在则自动注册
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            user.setRole(1);
            userMapper.insert(user);
            isNewUser = true;

            initNewUser(user);
        }

        return generateLoginResult(user, isNewUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> loginByWechat(WechatLoginReq req) {
        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(req.getCode());
            String openId = session.getOpenid();
            String unionId = session.getUnionid();

            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getWxOpenid, openId));

            boolean isNewUser = false;
            if (user == null) {
                user = new User();
                user.setWxOpenid(openId);
                user.setWxUnionid(unionId);
                user.setStatus(1);
                user.setRole(1);
                userMapper.insert(user);
                isNewUser = true;

                initNewUser(user);
            }

            return generateLoginResult(user, isNewUser);
        } catch (Exception e) {
            log.error("微信登录失败", e);
            throw new RuntimeException("微信登录失败", e);
        }
    }

    private void initNewUser(User user) {
        // 初始化 Profile
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setNickname("小鸭_" + user.getId());
        // 使用统一默认头像
        profile.setAvatar("/static/avatars/default-avatar.png"); // 默认头像
        userProfileMapper.insert(profile);

        // 初始化钱包与注册赠送 100 鸭蛋
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(100);
        wallet.setPermanentBalance(0);
        wallet.setTempBalance(100);
        walletMapper.insert(wallet);

        // 写入流水
        EggLedger ledger = new EggLedger();
        ledger.setUserId(user.getId());
        ledger.setAmount(100);
        ledger.setBalanceAfter(100);
        ledger.setScene("register");
        ledger.setIdempotentKey("reg_" + user.getId());
        eggLedgerMapper.insert(ledger);
    }

    private Map<String, Object> generateLoginResult(User user, boolean isNewUser) {
        // 生成 JWT Token
        String token = JwtUtils.generateToken(user.getId());

        // 检查资料是否完善
        UserProfile profile = userProfileMapper.selectById(user.getId());
        boolean isProfileComplete = false;
        if (profile != null) {
            // 基础资料完善标准：昵称、头像、性别、生日均已填写 (性别必须是1或2)
            isProfileComplete = profile.getNickname() != null && !profile.getNickname().isEmpty() 
                && profile.getAvatar() != null && !profile.getAvatar().isEmpty()
                && profile.getGender() != null && (profile.getGender() == 1 || profile.getGender() == 2)
                && profile.getBirthday() != null;
        }

        // 封装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("isNewUser", isNewUser);
        result.put("isProfileComplete", isProfileComplete);

        return result;
    }
}
