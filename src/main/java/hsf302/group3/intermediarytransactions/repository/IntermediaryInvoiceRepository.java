package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.IntermediaryInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntermediaryInvoiceRepository extends JpaRepository<IntermediaryInvoice, String> {
}