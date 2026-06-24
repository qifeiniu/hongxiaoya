package com.hongxiaoya.api.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("egg_ledgers")
public class EggLedger {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer amount;
    private Integer balanceAfter;
    private String scene;
    private String relatedId;
    private Long targetUserId;
    private Date expireAt;
    private String idempotentKey;
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
}
