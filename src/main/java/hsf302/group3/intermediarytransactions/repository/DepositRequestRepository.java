package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.DepositRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositRequestRepository extends JpaRepository<DepositRequest, Integer> {
}