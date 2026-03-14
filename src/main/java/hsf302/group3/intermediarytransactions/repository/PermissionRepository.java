package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
   Optional<Permission> findByName(String name);
}