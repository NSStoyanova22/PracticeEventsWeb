package com.example.hefest.config;

import com.example.hefest.model.SessionEndReason;
import com.example.hefest.model.User;
import com.example.hefest.repository.UserRepository;
import com.example.hefest.service.SessionTrackingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.Locale;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            SessionTrackingService sessionTrackingService) throws Exception {
        SimpleUrlAuthenticationSuccessHandler redirectToEvents = new SimpleUrlAuthenticationSuccessHandler("/events");
        redirectToEvents.setAlwaysUseDefaultTargetUrl(true);

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/login", "/register", "/403").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/chat/**").authenticated()
                        .requestMatchers("/notifications/**").authenticated()
                        .requestMatchers("/comments/**").authenticated()
                        .requestMatchers("/my-lists", "/favorites/**", "/collections/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/events/*/comments").authenticated()
                        .requestMatchers("/events/new").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers("/events/import/**").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/events/**").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/events").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/events/*").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers("/events/*/edit", "/events/*/delete", "/events/*/status", "/events/*/versions/*/restore", "/events/*/versions/compare").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/events/*/registrations/**").authenticated()
                        .requestMatchers("/events/**").permitAll()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .successHandler((request, response, authentication) -> {
                            if (authentication.getPrincipal() instanceof User user) {
                                sessionTrackingService.recordLogin(user, request);
                            }
                            redirectToEvents.onAuthenticationSuccess(request, response, authentication);
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            HttpSession session = request.getSession(false);
                            if (session != null) {
                                sessionTrackingService.endSession(session.getId(), SessionEndReason.LOGOUT);
                            }
                        })
                        .logoutSuccessUrl("/events")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions.accessDeniedPage("/403"));

        return http.build();
    }
}
