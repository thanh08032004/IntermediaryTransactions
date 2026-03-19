package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.IntermediaryInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntermediaryInvoiceRepository extends JpaRepository<IntermediaryInvoice, String> {

    Page<IntermediaryInvoice> findBySellerIdOrBuyerId(Integer sellerId, Integer buyerId, Pageable pageable);
    
    Page<IntermediaryInvoice> findBySellerIdOrBuyerIdAndInvoiceCodeContainingIgnoreCaseOrSubjectContainingIgnoreCase(
            Integer sellerId, Integer buyerId, String codeKeyword, String subjectKeyword, Pageable pageable);
}