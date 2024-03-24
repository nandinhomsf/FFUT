package br.com.eterniaserver.bddia.configuration.security;

import br.com.eterniaserver.bddia.domain.user.enums.BaseRoles;
import br.com.eterniaserver.bddia.domain.user.services.JWTService;
import br.com.eterniaserver.bddia.domain.user.services.UserAccountService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilter {

    private final UserAccountService userAccountService;
    private final JWTService jwtService;

    @Bean
    public OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NonNull HttpServletRequest request,
                                            @NonNull HttpServletResponse response,
                                            @NonNull FilterChain filterChain) {
                Optional<String> authorizationOptional = Optional.ofNullable(request.getHeader("Authorization"));

                authorizationOptional.ifPresent(authorization -> {
                    if (authorization.contains("Bearer ")) {
                        String authorizationToken = authorization.split(" ")[1];

                        if (jwtService.isValidToken(authorizationToken)) {
                            String userLogin = jwtService.getUserLogin(authorizationToken);

                            UserDetails userDetails = userAccountService.login(userLogin);

                            UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );

                            user.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(user);
                        }
                    }
                });

                try {
                    filterChain.doFilter(request, response);
                } catch (ServletException | IOException exception) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    logger.info(exception.getMessage());
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> {
                    request.requestMatchers(HttpMethod.POST, "/user/").hasRole(BaseRoles.ADMIN.name());
                    request.requestMatchers(HttpMethod.GET, "/user/**").hasRole(BaseRoles.USER.name());
                    request.requestMatchers(HttpMethod.PUT, "/user/").hasRole(BaseRoles.USER.name());
                    request.requestMatchers(HttpMethod.DELETE, "/user/**").hasRole(BaseRoles.ADMIN.name());
                    request.requestMatchers("/login/**").permitAll();
                    request.requestMatchers("/swagger-ui/**").permitAll();
                    request.requestMatchers("/api-docs/**").permitAll();
                    request.requestMatchers("/error/**").permitAll();
                    request.anyRequest().denyAll();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}