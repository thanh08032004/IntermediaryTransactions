package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.IntermediaryInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IntermediaryInvoiceRepository extends JpaRepository<IntermediaryInvoice, String> {

    Page<IntermediaryInvoice> findBySellerIdOrBuyerIdOrderByCreatedAtDesc(Integer sellerId, Integer buyerId, Pageable pageable);
    Optional<IntermediaryInvoice> findByShareToken(String shareToken);
    Page<IntermediaryInvoice> findBySellerIdOrBuyerIdAndInvoiceCodeContainingIgnoreCaseOrSubjectContainingIgnoreCase(
            Integer sellerId, Integer buyerId, String invoiceCodeKeyword, String subjectKeyword, Pageable pageable
    );
    @Query("""
    SELECT i FROM IntermediaryInvoice i
    WHERE (i.sellerId = :userId OR i.buyerId = :userId)
      AND (
            LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(i.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
    ORDER BY i.createdAt DESC
""")
    Page<IntermediaryInvoice> searchInvoices(
            @Param("userId") Integer userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    // Lấy tất cả hóa đơn mà user là seller, theo ngày tạo giảm dần
    Page<IntermediaryInvoice> findBySellerIdOrderByCreatedAtDesc(Integer sellerId, Pageable pageable);

    // Tìm hóa đơn theo seller + keyword (invoiceCode hoặc subject)
    @Query("""
    SELECT i FROM IntermediaryInvoice i
    WHERE i.sellerId = :sellerId
      AND (LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(i.subject) LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY i.createdAt DESC
""")
    Page<IntermediaryInvoice> findBySellerIdAndKeyword(
            @Param("sellerId") Integer sellerId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    Page<IntermediaryInvoice> findByBuyerIdOrderByCreatedAtDesc(Integer buyerId, Pageable pageable);
}