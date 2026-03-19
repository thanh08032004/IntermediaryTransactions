package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByResetToken(String resetToken);
    @Query("SELECT u FROM User u LEFT JOIN u.profile p WHERE u.username = :input OR p.email = :input")
    Optional<User> findByUsernameOrEmail(@Param("input") String input);
    Page<User> findAll(Pageable pageable);
    @Query("SELECT u FROM User u JOIN u.profile p WHERE p.fullname LIKE %:name% OR u.username LIKE %:name%")
    Page<User> searchByName(@Param("name") String name, Pageable pageable);
}
