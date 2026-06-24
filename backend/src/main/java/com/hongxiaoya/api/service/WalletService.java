package com.hongxiaoya.api.service;

import com.hongxiaoya.api.entity.Wallet;

import java.util.List;

public interface WalletService {
    Wallet getWallet(Long userId);
    void signIn(Long userId);
    boolean deductBalance(Long userId, Integer amount, String scene, String relatedId, Long targetUserId);
    void addBalance(Long userId, Integer amount, String scene, String relatedId, Long sourceUserId);
    List<com.hongxiaoya.api.entity.EggLedger> getLedgerRecords(Long userId, String scene);
}

