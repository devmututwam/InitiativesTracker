package zm.co.zanaco.tracker.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import zm.co.zanaco.tracker.security.JwtAuthenticationFilter;
import zm.co.zanaco.tracker.security.JwtTokenProvider;

/**
 * Security configuration.
 *
 * <h2>In-memory dev users</h2>
 * <table>
 *   <tr><th>Username</th><th>Password</th><th>Role</th></tr>
 *   <tr><td>admin</td><td>admin123</td><td>ADMIN</td></tr>
 *   <tr><td>manager</td><td>manager123</td><td>MANAGER</td></tr>
 *   <tr><td>dev</td><td>dev123</td><td>DEVELOPER</td></tr>
 *   <tr><td>viewer</td><td>viewer123</td><td>VIEWER</td></tr>
 * </table>
 *
 * <h2>Role matrix</h2>
 * <ul>
 *   <li>VIEWER   → GET /api/**</li>
 *   <li>DEVELOPER → VIEWER + POST status / costs / calculate-saving</li>
 *   <li>MANAGER  → DEVELOPER + POST/PATCH/DELETE initiative, budget, cost</li>
 *   <li>ADMIN    → same as MANAGER (extend here for admin-only ops)</li>
 * </ul>
 *
 * <p>{@link JwtAuthenticationFilter} is instantiated inline (not a Spring bean)
 * to avoid a circular dependency between the filter chain and {@link UserDetailsService}.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // -------------------------------------------------------------------------
    // Shared beans
    // -------------------------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin123")).roles("ADMIN").build();
        UserDetails manager = User.withUsername("manager")
                .password(encoder.encode("manager123")).roles("MANAGER").build();
        UserDetails dev = User.withUsername("dev")
                .password(encoder.encode("dev123")).roles("DEVELOPER").build();
        UserDetails viewer = User.withUsername("viewer")
                .password(encoder.encode("viewer123")).roles("VIEWER").build();
        return new InMemoryUserDetailsManager(admin, manager, dev, viewer);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // -------------------------------------------------------------------------
    // Filter chain
    // -------------------------------------------------------------------------

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtTokenProvider jwtTokenProvider,
                                           UserDetailsService userDetailsService) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")))
                .authorizeHttpRequests(auth -> auth

                        // --- Public ---
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/api-docs", "/api-docs/**").permitAll()

                        // --- VIEWER+ : all GET requests ---
                        .requestMatchers(HttpMethod.GET, "/api/**")
                                .hasAnyRole("VIEWER", "DEVELOPER", "MANAGER", "ADMIN")

                        // --- DEVELOPER+ : status changes, cost entries, savings ---
                        .requestMatchers(HttpMethod.POST, "/api/initiatives/*/status")
                                .hasAnyRole("DEVELOPER", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/initiatives/*/costs")
                                .hasAnyRole("DEVELOPER", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/initiatives/*/calculate-saving")
                                .hasAnyRole("DEVELOPER", "MANAGER", "ADMIN")

                        // --- MANAGER+ : create and update ---
                        .requestMatchers(HttpMethod.POST, "/api/initiatives")
                                .hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/initiatives/*")
                                .hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/initiatives/*/budgets")
                                .hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/budgets/*")
                                .hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/budgets/*")
                                .hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/costs/*")
                                .hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/costs/*")
                                .hasAnyRole("MANAGER", "ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
