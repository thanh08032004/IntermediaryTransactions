package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @NotBlank(message = "The full name must not be left blank.")
    @Column(nullable = false, length = 100)
    private String fullname;


    @Pattern(regexp = "^(0|\\+84)(\\d{9})?$", message = "Invalid phone number")
    @Column(name = "phone", length = 20)
    private String phone;

    @Email(message = "Invalid email")
    @Column(unique = true, length = 100)
    private String email;

    private String avatar;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}