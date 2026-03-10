package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
