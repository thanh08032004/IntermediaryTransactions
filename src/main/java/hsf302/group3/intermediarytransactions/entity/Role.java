package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"permissions"}) // Không cho phép toString quét qua permissions
@EqualsAndHashCode(exclude = {"permissions"}) // Quan trọng nhất để tránh lỗi Concurrent khi Hibernate quản lý Set
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    private Boolean active;

    @ManyToMany(fetch = FetchType.EAGER) // Đã để EAGER là tốt, nhưng vẫn cần Exclude ở trên
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}