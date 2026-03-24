package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.IntermediaryInvoice;
import hsf302.group3.intermediarytransactions.repository.IntermediaryInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IntermediaryInvoiceService {

    private final IntermediaryInvoiceRepository repository;

    public Page<IntermediaryInvoice> getUserInvoices(Integer userId, int page, int size, String keyword) {
        PageRequest pageable = PageRequest.of(page, size);

        if (keyword == null || keyword.isEmpty()) {
            return repository.findBySellerIdOrBuyerIdOrderByCreatedAtDesc(userId, userId, pageable);
        }

        return repository.searchInvoices(userId, keyword, pageable);
    }

    public Optional<IntermediaryInvoice> getById(String id) {
        return repository.findById(id);
    }

    public IntermediaryInvoice save(IntermediaryInvoice invoice) {
        return repository.save(invoice);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

}