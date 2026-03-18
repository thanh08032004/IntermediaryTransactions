package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Cart;
import hsf302.group3.intermediarytransactions.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUser(User user);
}
