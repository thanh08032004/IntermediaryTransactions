package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Permission;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
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
    @Transactional(readOnly = true) //giu cho session mo
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // chuyen Set sang List ngay lap tuc de tranh tranh chap Concurrent
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

        // duyet qua permissions va gan vao authorities
        if (user.getRole().getPermissions() != null) {
            //  Hibernate nap du lieu bang cap truy cap truc tiep
            for (Permission p : user.getRole().getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(p.getName()));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getActive(),
                true, true, true,
                authorities
        );
    }
}