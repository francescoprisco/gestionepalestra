package it.francescoprisco.gestionepalestra.config;

import it.francescoprisco.gestionepalestra.security.jwt.JwtAuthEntryPoint;
import it.francescoprisco.gestionepalestra.security.jwt.JwtAuthTokenFilter;
import it.francescoprisco.gestionepalestra.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * Classe principale per la configurazione di Spring Security.
 * Definisce le regole di accesso agli endpoint, il gestore di autenticazione e la codifica delle password.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthEntryPoint unauthorizedHandler;
    /**
     * Crea un bean per il nostro filtro di autenticazione JWT.
     * @return Il filtro JWT.
     */
    @Bean
    public JwtAuthTokenFilter authenticationJwtTokenFilter() {
        return new JwtAuthTokenFilter();
    }
    /**
     * Configura il provider di autenticazione che usa il nostro UserDetailsService
     * per caricare i dati dell'utente e il PasswordEncoder per verificare le password.
     * @return Il provider di autenticazione configurato.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    /**
     * Espone l'AuthenticationManager come bean per poterlo usare nel nostro AuthController.
     * @param authConfig La configurazione di autenticazione di Spring.
     * @return L'AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    /**
     * Definisce l'algoritmo di codifica delle password (BCrypt).
     * @return L'istanza del PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    /**
     * Definisce la catena di filtri di sicurezza e le regole di autorizzazione HTTP.
     * @param http L'oggetto HttpSecurity da configurare.
     * @return La SecurityFilterChain costruita.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> 
                  auth
                    .requestMatchers("/", "/index.html", "/*.js", "/*.css", "/*.ico").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/nfc/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/bookings/availability/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("RECEPTIONIST")
                    .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}