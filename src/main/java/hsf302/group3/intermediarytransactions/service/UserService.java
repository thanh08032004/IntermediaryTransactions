package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Role;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.entity.UserProfile;
import hsf302.group3.intermediarytransactions.entity.Wallet;
import hsf302.group3.intermediarytransactions.repository.UserProfileRepository;
import hsf302.group3.intermediarytransactions.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Lấy danh sách user có phân trang, search theo tên hoặc username */
    public Page<User> getAllUsersPaged(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        if (keyword != null && !keyword.isEmpty()) {
            return userRepository.searchByName(keyword, pageable);
        }
        return userRepository.findAll(pageable);
    }

    /** Lấy tất cả user (không phân trang) */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Lấy user theo id */
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /** Lấy user theo username kèm profile, role.permissions, wallet */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /** Tạo mới user */
    @Transactional
    public void createNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);

        if (user.getProfile() != null) {
            user.getProfile().setUser(user);
        }

        userRepository.save(user);
    }

    /** Cập nhật user */
    @Transactional
    public void updateUser(User userForm) {
        User existingUser = getUserById(userForm.getId());

        // Kiểm tra trùng email/phone
        String newEmail = userForm.getProfile().getEmail();
        String newPhone = userForm.getProfile().getPhone();

        if (userProfileRepository.existsByEmailAndUserIdNot(newEmail, userForm.getId())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }
        if (userProfileRepository.existsByPhoneAndUserIdNot(newPhone, userForm.getId())) {
            throw new RuntimeException("Số điện thoại này đã được sử dụng!");
        }

        existingUser.setActive(userForm.getActive());
        existingUser.setRole(userForm.getRole());

        UserProfile profile = existingUser.getProfile();
        if (profile != null) {
            profile.setFullname(userForm.getProfile().getFullname());
            profile.setEmail(newEmail);
            profile.setPhone(newPhone);
        }

        userRepository.save(existingUser);
    }

    /** Bật/tắt trạng thái user */
    @Transactional
    public void toggleUserStatus(Integer id) {
        User user = getUserById(id);
        checkCurrentUser(user.getUsername(), "deactivate your own account");
        user.setActive(!user.getActive());
        userRepository.save(user);
    }

    /** Xóa user */
    @Transactional
    public void deleteUser(Integer id) {
        User user = getUserById(id);
        checkCurrentUser(user.getUsername(), "delete your own account");
        userRepository.deleteById(id);
    }

    /** Cập nhật profile của chính user */
    @Transactional
    public void updateMyProfile(String username, UserProfile updatedProfile) {
        User user = findByUsername(username);
        UserProfile profile = user.getProfile();

        if (profile == null) {
            updatedProfile.setUser(user);
            updatedProfile.setUserId(user.getId());
            user.setProfile(updatedProfile);
        } else {
            profile.setFullname(updatedProfile.getFullname());
            profile.setEmail(updatedProfile.getEmail());
            profile.setPhone(updatedProfile.getPhone());
            profile.setAvatar(updatedProfile.getAvatar());
            profile.setGender(updatedProfile.getGender());
            profile.setDateOfBirth(updatedProfile.getDateOfBirth());
            profile.setDescription(updatedProfile.getDescription());
        }

        userRepository.save(user);
    }

    /** Đăng ký user mới */
    @Transactional
    public void registerNewUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);

        // Role mặc định (user)
        Role userRole = new Role();
        userRole.setId(2);
        user.setRole(userRole);

        // Profile mặc định
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullname(user.getUsername());
        user.setProfile(profile);

        // Wallet mặc định
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        user.setWallet(wallet);

        userRepository.save(user);
    }

    /** Lấy username đang login */
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    /** Kiểm tra admin không thao tác trên chính mình */
    private void checkCurrentUser(String username, String action) {
        if (username.equals(getCurrentUsername())) {
            throw new RuntimeException("Bạn không thể " + action + "!");
        }
    }
}