package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.IntermediaryFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface IntermediaryFeeRepository extends JpaRepository<IntermediaryFee, Integer> {

    @Query("""
        SELECT f FROM IntermediaryFee f
        WHERE :price >= f.minAmount
        AND (f.maxAmount IS NULL OR :price < f.maxAmount)
    """)
    IntermediaryFee findByPrice(@Param("price") BigDecimal price);
}