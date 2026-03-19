package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Permission;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
import hsf302.group3.intermediarytransactions.security.CustomUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // ROLE
        authorities.add(new SimpleGrantedAuthority("ROLE_" + u.getRole().getName()));

        // PERMISSIONS
        if (u.getRole().getPermissions() != null) {
            for (Permission p : u.getRole().getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(p.getName()));
            }
        }

        return new CustomUserDetails(
                u.getId(),
                u.getUsername(),
                u.getPassword(),
                authorities
        );
    }
}