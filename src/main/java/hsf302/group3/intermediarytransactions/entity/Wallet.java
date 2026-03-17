package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", unique = true)
    private Integer userId;

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance;
}