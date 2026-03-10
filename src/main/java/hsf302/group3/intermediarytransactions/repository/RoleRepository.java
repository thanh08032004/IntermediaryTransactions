package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
   Optional<Role> findByName(String name);
}