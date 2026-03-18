package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "intermediary_invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntermediaryInvoice {

    @Id
    @Column(name = "invoice_code")
    private String invoiceCode;

    @Column(name = "subject")
    private String subject;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "buyer_id")
    private Integer buyerId;

    @Column(name = "contact_method")
    private String contactMethod;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}