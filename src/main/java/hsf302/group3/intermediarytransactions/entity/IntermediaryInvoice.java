package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
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
    @Column(name = "invoice_id", length = 50)
    private String invoiceId; // trùng với DB: PRIMARY KEY

    @Column(name = "invoice_code", length = 50, unique = true)
    private String invoiceCode;

    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "buyer_id")
    private Integer buyerId;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "fee_payer")
    private String feePayer; // BUYER / SELLER

    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

    @Column(name = "buyer_total")
    private BigDecimal buyerTotal;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_method", length = 255)
    private String contactMethod;

    @Column(name = "hidden_info", columnDefinition = "TEXT")
    private String hiddenInfo; // chỉ hiển thị khi SUCCESS

    @Column(name = "status", length = 20)
    private String status; // PENDING, PAID, CHECKING, COMPLAINT, SUCCESS, CANCELLED

    @Column(name = "share_link", columnDefinition = "TEXT")
    private String shareLink;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}