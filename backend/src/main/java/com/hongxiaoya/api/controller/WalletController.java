package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.config.UserContext;
import com.hongxiaoya.api.entity.Wallet;
import com.hongxiaoya.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/info")
    public Result<Wallet> getInfo() {
        Long userId = UserContext.getUserId();
        return Result.success(walletService.getWallet(userId));
    }

    @PostMapping("/sign-in")
    public Result<String> signIn() {
        Long userId = UserContext.getUserId();
        try {
            walletService.signIn(userId);
            return Result.success("签到成功");
        } catch (Exception e) {
            return Result.error(3001, e.getMessage());
        }
    }

    @PostMapping("/recharge")
    public Result<String> recharge(@RequestParam Integer amount) {
        Long userId = UserContext.getUserId();
        // 此处应对接微信/支付宝支付，支付成功后回调加余额
        // 这里模拟充值成功直接加鸭蛋
        walletService.addBalance(userId, amount, "recharge", "sim_" + System.currentTimeMillis(), null);
        return Result.success("充值成功");
    }

    @GetMapping("/ledger")
    public Result<java.util.List<com.hongxiaoya.api.entity.EggLedger>> getLedgerRecords(@RequestParam(required = false) String scene) {
        Long userId = UserContext.getUserId();
        return Result.success(walletService.getLedgerRecords(userId, scene));
    }
}
