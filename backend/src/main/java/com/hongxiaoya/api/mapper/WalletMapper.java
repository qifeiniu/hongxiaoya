package com.hongxiaoya.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongxiaoya.api.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {
    @Update("UPDATE wallets SET balance = balance + #{amount}, temp_balance = temp_balance + #{amount} WHERE user_id = #{userId}")
    int addTempBalance(@Param("userId") Long userId, @Param("amount") Integer amount);
    
    @Update("UPDATE wallets SET balance = balance - #{amount}, temp_balance = temp_balance - #{amount} WHERE user_id = #{userId} AND temp_balance >= #{amount}")
    int deductTempBalance(@Param("userId") Long userId, @Param("amount") Integer amount);
    
    @Update("UPDATE wallets SET balance = balance - #{amount}, permanent_balance = permanent_balance - #{amount} WHERE user_id = #{userId} AND permanent_balance >= #{amount}")
    int deductPermanentBalance(@Param("userId") Long userId, @Param("amount") Integer amount);

    @Update("UPDATE wallets SET balance = balance + #{amount}, permanent_balance = permanent_balance + #{amount} WHERE user_id = #{userId}")
    int addPermanentBalance(@Param("userId") Long userId, @Param("amount") Integer amount);
}
