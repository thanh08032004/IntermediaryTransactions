package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Cart;
import hsf302.group3.intermediarytransactions.entity.CartItem;
import hsf302.group3.intermediarytransactions.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
