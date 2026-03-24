package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.IntermediaryInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IntermediaryInvoiceRepository extends JpaRepository<IntermediaryInvoice, String> {

    Page<IntermediaryInvoice> findBySellerIdOrBuyerIdOrderByCreatedAtDesc(Integer sellerId, Integer buyerId, Pageable pageable);

    Page<IntermediaryInvoice> findBySellerIdOrBuyerIdAndInvoiceCodeContainingIgnoreCaseOrSubjectContainingIgnoreCase(
            Integer sellerId, Integer buyerId, String invoiceCodeKeyword, String subjectKeyword, Pageable pageable
    );

}