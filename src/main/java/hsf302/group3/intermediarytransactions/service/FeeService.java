package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.FeePayer;
import hsf302.group3.intermediarytransactions.entity.IntermediaryFee;
import hsf302.group3.intermediarytransactions.repository.IntermediaryFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final IntermediaryFeeRepository feeRepository;

    public BigDecimal calculateFee(BigDecimal price) {
        IntermediaryFee rule = feeRepository.findByPrice(price);

        if (rule == null) {
            throw new RuntimeException("No fee rule found");
        }

        return rule.getFeeFixed();
    }

    public BigDecimal calculateBuyerTotal(BigDecimal price, BigDecimal fee, FeePayer payer) {
        if (payer == FeePayer.BUYER) {
            return price.add(fee);
        } else {
            return price;
        }
    }
}