package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.dto.UserProfileDto;
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



    @Transactional
    public void createNewUser(User user) {
        //trung username
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("The username already exists!");
        }
        // trung email
        if (user.getProfile() != null && user.getProfile().getEmail() != null) {
            if (userProfileRepository.existsByEmailAndUserIdNot(
                    user.getProfile().getEmail(), 0)) {
                throw new RuntimeException("The email address has already been used!");
            }
        }

//        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);

        if (user.getProfile() != null) {
            user.getProfile().setUser(user);
        }

        userRepository.save(user);
    }

    @Transactional
    public void updateUser(User userForm) {
        User existingUser = getUserById(userForm.getId());

        String newEmail = userForm.getProfile().getEmail();
        String newPhone = userForm.getProfile().getPhone();

        if (userProfileRepository.existsByEmailAndUserIdNot(newEmail, userForm.getId())) {
            throw new RuntimeException("This email address is already in use!");
        }
        if (userProfileRepository.existsByPhoneAndUserIdNot(newPhone, userForm.getId())) {
            throw new RuntimeException("This phone number is already in use!");
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

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
    public UserProfileDto getProfileDto(String username) {
        User user = findByUsername(username);
        return new UserProfileDto(user);
    }

    @Transactional
    public void updateMyProfile(String username, UserProfileDto dto) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            profile.setUserId(user.getId());
            user.setProfile(profile);
        }

        profile.setFullname(dto.getFullname());
        profile.setEmail(dto.getEmail());
        profile.setPhone(dto.getPhone());
        profile.setAvatar(dto.getAvatar());
        profile.setGender(dto.getGender());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setDescription(dto.getDescription());

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