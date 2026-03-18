package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    // =========================
    // 💰 NẠP TIỀN
    // =========================
    @Transactional
    public void deposit(Integer userId, BigDecimal amount, String description) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseGet(() -> Wallet.builder()
                        .user(user) // ✅ đúng
                        .balance(BigDecimal.ZERO)
                        .build());

        // ✅ cộng tiền đúng chuẩn
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // 🔥 lưu transaction
        Transaction tx = Transaction.builder()
                .transactionCode("TXN_" + System.currentTimeMillis())
                .userId(userId)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description(description)
                .build();

        transactionRepository.save(tx);
    }

    // =========================
    // 💸 THANH TOÁN
    // =========================
    @Transactional
    public void pay(Integer userId, BigDecimal amount, Integer orderId) {

        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        // ✅ check tiền
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Không đủ tiền");
        }

        // ✅ trừ tiền
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        // 🔥 lưu transaction
        Transaction tx = Transaction.builder()
                .transactionCode("TXN_" + System.currentTimeMillis())
                .userId(userId)
                .amount(amount)
                .type(TransactionType.PAYMENT)
                .status(TransactionStatus.SUCCESS)
                .description("Thanh toán đơn " + orderId)
                .build();

        transactionRepository.save(tx);
    }
}