package com.example.outage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the Electricity Outage Reporting System.
 *
 * <p><strong>Development configuration only.</strong>
 * Users are defined in memory with BCrypt-hashed passwords.
 * In a real production deployment, replace this with a database-backed
 * UserDetailsService and load credentials from environment variables or a secrets manager.
 *
 * <p>Roles:
 * <ul>
 *   <li>USER  — can view the dashboard, create reports, and view/search reports.</li>
 *   <li>ADMIN — all USER permissions plus status updates, edits, and deletes.</li>
 * </ul>
 *
 * <p>Default development credentials (documented, not secret):
 * <pre>
 *   user  / user123
 *   admin / admin123
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * BCrypt password encoder — suitable for production use.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * In-memory user store for development and demonstration.
     * Replace with a JPA-backed UserDetailsService before production.
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        UserDetails regularUser = User.builder()
                .username("user")
                .password(encoder.encode("user123"))
                .roles("USER")
                .build();

        UserDetails adminUser = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN", "USER")
                .build();

        return new InMemoryUserDetailsManager(regularUser, adminUser);
    }

    /**
     * HTTP security filter chain.
     * Public paths: /login, /actuator/health, static assets.
     * All other paths require authentication.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Static assets and health endpoint are public
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/h2-console/**").hasRole("ADMIN")
                // All other requests require login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write(
                        "<!DOCTYPE html><html><head><title>403 Forbidden</title>" +
                        "<link rel='stylesheet' href='/css/main.css'/></head>" +
                        "<body style='background:#f8f9fa;min-height:100vh;display:flex;" +
                        "align-items:center;justify-content:center;font-family:Inter,sans-serif'>" +
                        "<div style='text-align:center;padding:40px'>" +
                        "<div style='font-size:5rem;font-weight:800;color:#e9ecef'>403</div>" +
                        "<h1 style='font-size:1.4rem;color:#343a40;margin:12px 0 8px'>Access Denied</h1>" +
                        "<p style='color:#adb5bd;margin-bottom:24px'>You do not have permission to perform this action.</p>" +
                        "<a href='/' style='padding:8px 18px;background:#1a2744;color:#fff;" +
                        "border-radius:6px;text-decoration:none;font-size:0.85rem'>Go to Dashboard</a>" +
                        "</div></body></html>"
                    );
                })
            )
            // Allow H2 console frames in dev (disable in production)
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            );

        return http.build();
    }
}
