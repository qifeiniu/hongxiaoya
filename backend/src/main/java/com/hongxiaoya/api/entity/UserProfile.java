package com.hongxiaoya.api.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_profiles")
public class UserProfile {
    @TableId
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Date birthday;
    private Integer height;
    private Integer weight;
    private String cityCode;
    private String hometownCode;
    private String householdCode;
    private Integer education;
    private String school;
    private Integer incomeLevel;
    private String profession;
    private Integer maritalStatus;
    private String constellation;
    private String bio;
    private String familyBackground;
    private String hobbies;
    private String idealPartner;
    private String expectedLife;
    private String photos;
    private String tags;
    private Integer isRealAuth;
    private Integer isEduAuth;
    private Integer isAvatarAuth;
    private String wechatId;
}
