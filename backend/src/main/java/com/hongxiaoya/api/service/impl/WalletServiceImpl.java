package com.hongxiaoya.api.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongxiaoya.api.entity.EggLedger;
import com.hongxiaoya.api.entity.VipMembership;
import com.hongxiaoya.api.entity.Wallet;
import com.hongxiaoya.api.mapper.EggLedgerMapper;
import com.hongxiaoya.api.mapper.WalletMapper;
import com.hongxiaoya.api.service.VipService;
import com.hongxiaoya.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final EggLedgerMapper eggLedgerMapper;
    private final VipService vipService;

    @Override
    public Wallet getWallet(Long userId) {
        return walletMapper.selectById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signIn(Long userId) {
        String idempotentKey = "sign_" + userId + "_" + DateUtil.format(new Date(), "yyyyMMdd");
        
        Long count = eggLedgerMapper.selectCount(new LambdaQueryWrapper<EggLedger>()
                .eq(EggLedger::getIdempotentKey, idempotentKey));
        if (count > 0) {
            throw new RuntimeException("今日已签到");
        }

        VipMembership vipInfo = vipService.getVipInfo(userId);
        boolean isVip = (vipInfo != null && vipInfo.getExpireAt().after(new Date()));
        int rewardAmount = isVip ? 20 : 10;
        
        walletMapper.addTempBalance(userId, rewardAmount);
        
        Wallet wallet = walletMapper.selectById(userId);

        EggLedger ledger = new EggLedger();
        ledger.setUserId(userId);
        ledger.setAmount(rewardAmount);
        ledger.setBalanceAfter(wallet.getBalance());
        ledger.setScene("sign_in");
        ledger.setIdempotentKey(idempotentKey);
        ledger.setExpireAt(DateUtil.offsetMonth(new Date(), 1));
        ledger.setCreatedAt(new Date());
        eggLedgerMapper.insert(ledger);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductBalance(Long userId, Integer amount, String scene, String relatedId, Long targetUserId) {
        Wallet wallet = walletMapper.selectById(userId);
        if (wallet == null || wallet.getBalance() < amount) {
            return false;
        }

        int tempDeduct = 0;
        int permDeduct = 0;
        
        if (wallet.getTempBalance() >= amount) {
            tempDeduct = amount;
            int affected = walletMapper.deductTempBalance(userId, amount);
            if (affected == 0) {
                throw new RuntimeException("并发扣费失败，请重试");
            }
        } else {
            tempDeduct = wallet.getTempBalance();
            permDeduct = amount - tempDeduct;
            if (tempDeduct > 0) {
                int affected = walletMapper.deductTempBalance(userId, tempDeduct);
                if (affected == 0) throw new RuntimeException("并发扣费失败，请重试");
            }
            if (permDeduct > 0) {
                int affected = walletMapper.deductPermanentBalance(userId, permDeduct);
                if (affected == 0) throw new RuntimeException("并发扣费失败，请重试");
            }
        }
        
        Wallet newWallet = walletMapper.selectById(userId);

        EggLedger ledger = new EggLedger();
        ledger.setUserId(userId);
        ledger.setAmount(-amount);
        ledger.setBalanceAfter(newWallet.getBalance());
        ledger.setScene(scene);
        ledger.setRelatedId(relatedId);
        ledger.setTargetUserId(targetUserId);
        ledger.setIdempotentKey(scene + "_" + relatedId + "_" + System.currentTimeMillis());
        ledger.setCreatedAt(new Date());
        eggLedgerMapper.insert(ledger);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBalance(Long userId, Integer amount, String scene, String relatedId, Long sourceUserId) {
        walletMapper.addPermanentBalance(userId, amount);

        Wallet newWallet = walletMapper.selectById(userId);

        EggLedger ledger = new EggLedger();
        ledger.setUserId(userId);
        ledger.setAmount(amount);
        ledger.setBalanceAfter(newWallet.getBalance());
        ledger.setScene(scene);
        ledger.setRelatedId(relatedId);
        ledger.setTargetUserId(sourceUserId);
        ledger.setIdempotentKey(scene + "_" + relatedId + "_" + System.currentTimeMillis());
        ledger.setCreatedAt(new Date());
        eggLedgerMapper.insert(ledger);
    }

    @Override
    public java.util.List<EggLedger> getLedgerRecords(Long userId, String scene) {
        LambdaQueryWrapper<EggLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EggLedger::getUserId, userId);
        if (scene != null && !scene.isEmpty()) {
            wrapper.eq(EggLedger::getScene, scene);
        }
        wrapper.orderByDesc(EggLedger::getCreatedAt);
        return eggLedgerMapper.selectList(wrapper);
    }
}
