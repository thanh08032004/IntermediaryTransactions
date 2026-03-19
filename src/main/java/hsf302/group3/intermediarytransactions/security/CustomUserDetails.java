package hsf302.group3.intermediarytransactions.security;

import hsf302.group3.intermediarytransactions.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final User user; // entity User
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // nếu bạn có trường kiểm tra hết hạn thì đổi ở đây
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // nếu bạn có trường khóa tài khoản thì đổi ở đây
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // nếu bạn có quản lý thời hạn mật khẩu thì đổi ở đây
    }

    @Override
    public boolean isEnabled() {
        // chuyển Boolean sang primitive boolean an toàn
        return user.getActive() != null && user.getActive();
    }
}