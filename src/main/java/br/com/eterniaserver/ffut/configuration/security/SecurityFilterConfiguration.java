package br.com.eterniaserver.ffut.configuration.security;

import br.com.eterniaserver.ffut.domain.user.enums.BaseRoles;
import br.com.eterniaserver.ffut.domain.user.services.JWTService;
import br.com.eterniaserver.ffut.domain.user.services.UserAccountService;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilterConfiguration {

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
                .cors(cors -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of("*"));
                    configuration.setAllowedMethods(List.of("*"));
                    configuration.setAllowedHeaders(List.of("*"));

                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", configuration);

                    cors.configurationSource(source);
                })
                .authorizeHttpRequests(request -> {
                    request.requestMatchers(HttpMethod.OPTIONS).permitAll();
                    request.requestMatchers("/login/**").permitAll();
                    request.requestMatchers("/swagger-ui/**").permitAll();
                    request.requestMatchers("/api-docs/**").permitAll();
                    request.requestMatchers("/error/**").permitAll();
                    request.requestMatchers(HttpMethod.POST, "/user/").permitAll();
                    request.requestMatchers(HttpMethod.GET, "/user/").hasRole(BaseRoles.USER.name());
                    request.requestMatchers(HttpMethod.GET, "/user/rank/list/").hasRole(BaseRoles.USER.name());
                    request.requestMatchers(HttpMethod.GET, "/user/**").hasRole(BaseRoles.ADMIN.name());
                    request.requestMatchers(HttpMethod.PUT, "/user/").hasRole(BaseRoles.USER.name());
                    request.requestMatchers(HttpMethod.DELETE, "/user/**").hasRole(BaseRoles.ADMIN.name());
                    request.requestMatchers("/answer/**").hasRole(BaseRoles.USER.name());
                    request.requestMatchers(HttpMethod.GET, "/challenge/list/").hasRole(BaseRoles.USER.name());
                    request.requestMatchers(HttpMethod.GET, "/challenge/**").hasRole(BaseRoles.USER.name());
                    request.requestMatchers(HttpMethod.POST, "/challenge/").hasRole(BaseRoles.ADMIN.name());
                    request.requestMatchers(HttpMethod.DELETE, "/challenge/**").hasRole(BaseRoles.ADMIN.name());
                    request.requestMatchers(HttpMethod.PUT, "/challenge/**").hasRole(BaseRoles.ADMIN.name());
                    request.anyRequest().denyAll();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
