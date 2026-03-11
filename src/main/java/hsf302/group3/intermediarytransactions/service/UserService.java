package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Role;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.entity.UserProfile;
import hsf302.group3.intermediarytransactions.entity.Wallet;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
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
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Transactional
    public void createNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);

        if (user.getProfile() != null) {
            user.getProfile().setUser(user);
        }

        userRepository.save(user);
    }

    @Transactional
    public void updateUser(User updatedUser) {
        User existingUser = getUserById(updatedUser.getId());
        String currentUsername = getCurrentUsername();

        //chi active/inactive neu khong phai minh
        if (!existingUser.getUsername().equals(currentUsername)) {
            existingUser.setActive(updatedUser.getActive());
            existingUser.setRole(updatedUser.getRole());
        }


        if (updatedUser.getProfile() != null) {
            if (existingUser.getProfile() == null) {
                // chua profile
                UserProfile newProfile = updatedUser.getProfile();
                newProfile.setUser(existingUser);
                newProfile.setUserId(existingUser.getId());
                existingUser.setProfile(newProfile);
            } else {
                // co profile
                existingUser.getProfile().setFullname(updatedUser.getProfile().getFullname());
                existingUser.getProfile().setEmail(updatedUser.getProfile().getEmail());
                existingUser.getProfile().setPhone(updatedUser.getProfile().getPhone());
                // cac truong khac them sau
            }
        }

        userRepository.save(existingUser);
    }

    @Transactional
    public void toggleUserStatus(Integer id) {
        User userToToggle = getUserById(id);
        String currentUsername = getCurrentUsername();

        // ngan admin tu xoa
        if (userToToggle.getUsername().equals(currentUsername)) {
            throw new RuntimeException("You cannot deactivate your own account!");
        }

        userToToggle.setActive(!userToToggle.getActive());
        userRepository.save(userToToggle);
    }

    @Transactional
    public void deleteUser(Integer id) {
        User userToDelete = getUserById(id);
        String currentUsername = getCurrentUsername();

        if (userToDelete.getUsername().equals(currentUsername)) {
            throw new RuntimeException("You cannot delete your own account!");
        }

        userRepository.deleteById(id);
    }

    // ham ho tro lay username Security Context
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    @Transactional
    public void updateMyProfile(String username, UserProfile updatedProfileData) {
        User user = findByUsername(username);
        UserProfile existingProfile = user.getProfile();

        if (existingProfile == null) {
            updatedProfileData.setUser(user);
            updatedProfileData.setUserId(user.getId());
            user.setProfile(updatedProfileData);
        } else {
            existingProfile.setFullname(updatedProfileData.getFullname());
            existingProfile.setEmail(updatedProfileData.getEmail());
            existingProfile.setPhone(updatedProfileData.getPhone());
            existingProfile.setAvatar(updatedProfileData.getAvatar());
            existingProfile.setGender(updatedProfileData.getGender());
            existingProfile.setDateOfBirth(updatedProfileData.getDateOfBirth());
            existingProfile.setDescription(updatedProfileData.getDescription());
        }
        userRepository.save(user);
    }

    @Transactional
    public void registerNewUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);

        Role userRole = new Role();
        userRole.setId(2);
        user.setRole(userRole);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullname(user.getUsername());
        user.setProfile(profile);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(java.math.BigDecimal.ZERO);
        user.setWallet(wallet);
        userRepository.save(user);
    }
}