package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Username cannot be blank.")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Transient
    @NotBlank(message = "Password cannot be blank", groups = OnCreate.class)
    @Size(min = 6, message = "Password must be at least 6 characters long.", groups = OnCreate.class)
    private String rawPassword;

    // Validation group
    public interface OnCreate {}

    @Column(nullable = true, length = 255)
    private String resetToken;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UserProfile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Wallet wallet;



    public User(Integer id) {
        this.id = id;
    }

}