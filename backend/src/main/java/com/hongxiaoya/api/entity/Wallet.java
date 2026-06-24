package com.hongxiaoya.api.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wallets")
public class Wallet {
    @TableId
    private Long userId;
    private Integer balance;
    private Integer permanentBalance;
    private Integer tempBalance;
}
