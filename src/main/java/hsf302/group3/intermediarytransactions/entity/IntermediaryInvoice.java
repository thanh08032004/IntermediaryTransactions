package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    private String invoiceId;

    @Column(name = "invoice_code", length = 50, unique = true)
    private String invoiceCode;

    @Column(name = "seller_id", nullable = false)
    private Integer sellerId;

    @Column(name = "buyer_id")
    private Integer buyerId;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_payer")
    private FeePayer feePayer;

    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

    @Column(name = "buyer_total")
    private BigDecimal buyerTotal;

    // 🔥 TOKEN thay vì link
    @Column(name = "share_token", unique = true)
    private String shareToken;

    // ---------------- IMAGE ----------------
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntermediaryInvoiceImage> images = new ArrayList<>();

    // ---------------- INFO ----------------
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_method", length = 255)
    private String contactMethod;

    @Column(name = "hidden_info", columnDefinition = "TEXT")
    private String hiddenInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvoiceStatus status;

    // ---------------- TIME ----------------
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.invoiceId == null) {
            this.invoiceId = UUID.randomUUID().toString();
        }

        if (this.shareToken == null) {
            this.shareToken = UUID.randomUUID().toString();
        }

        if (this.status == null) {
            this.status = InvoiceStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================================================
    // 🔥 COMPUTED FIELD (KHÔNG LƯU DB)
    // ==================================================

    @Transient
    public String getShareLink() {
        return "/intermediary/join/" + shareToken;
    }

    @Transient
    public String getMainImageUrl() {
        if (!images.isEmpty()) {
            return images.stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                    .map(IntermediaryInvoiceImage::getImageUrl)
                    .findFirst()
                    .orElse(images.get(0).getImageUrl());
        }
        return "/images/default.jpg";
    }
}