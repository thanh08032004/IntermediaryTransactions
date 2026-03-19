package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    boolean existsByEmailAndUserIdNot(String email, Integer userId);

    boolean existsByPhoneAndUserIdNot(String phone, Integer userId);
}
