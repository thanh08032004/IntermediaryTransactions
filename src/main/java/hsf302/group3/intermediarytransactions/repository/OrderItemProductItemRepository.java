package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.OrderItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemProductItemRepository extends JpaRepository<OrderItemDetail, Integer> {
}