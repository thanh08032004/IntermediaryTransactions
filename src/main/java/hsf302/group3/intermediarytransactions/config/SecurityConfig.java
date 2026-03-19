package hsf302.group3.intermediarytransactions.config;

import hsf302.group3.intermediarytransactions.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/forgot-password", "/reset-password/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/sepay-webhook").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
//                .rememberMe(remember -> remember
//                        .key("uniqueAndSecretKey")
//                        .tokenValiditySeconds(86400 * 7) // ghi nho trong 7 ngay (86400s = 1 ngay)
//                        .userDetailsService(customUserDetailsService)
//                        .rememberMeParameter("remember-me")
//                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }

//    public static void main(String[] args) {
//        BCryptPasswordEncoder b = new BCryptPasswordEncoder();
//        System.out.println(b.encode("123456"));
//    }
}