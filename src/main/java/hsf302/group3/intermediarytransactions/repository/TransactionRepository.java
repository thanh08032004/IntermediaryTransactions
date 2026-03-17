package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    // 🔍 lấy lịch sử theo user
    List<Transaction> findByUserId(Integer userId);

    // 🔍 tìm theo mã giao dịch
    Transaction findByTransactionCode(String transactionCode);
}