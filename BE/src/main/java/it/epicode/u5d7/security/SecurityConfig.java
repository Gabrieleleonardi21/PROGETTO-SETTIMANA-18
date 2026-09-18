package it.epicode.u5d7.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Sicurezza stateless basata su JWT: niente sessione, niente CSRF, niente form
 * di login di Spring. Pubbliche solo registrazione/login e l'handshake WebSocket;
 * tutto il resto richiede un Bearer token valido.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// Piu' origini separate da virgola: localhost per il PC e l'IP del Mac per il telefono in rete locale
	private final List<String> allowedOrigins;
	// Instrada anche gli errori di Spring Security verso ExceptionsHandler, cosi' la risposta e' sempre un ErrorPayload JSON
	private final HandlerExceptionResolver exceptionResolver;

	public SecurityConfig(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
		this.allowedOrigins = allowedOrigins;
		this.exceptionResolver = exceptionResolver;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// 401 senza token e 403 senza permessi passano dall'ExceptionsHandler
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((req, res, e) -> exceptionResolver.resolveException(req, res, null, e))
						.accessDeniedHandler((req, res, e) -> exceptionResolver.resolveException(req, res, null, e)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
						// L'handshake WebSocket e' una GET del browser senza header Authorization:
						// l'identita' viene data dopo, sul frame CONNECT (StompAuthInterceptor)
						.requestMatchers("/ws/**").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// Vale solo per /api/**: l'origine dell'handshake /ws e' controllata in WebSocketConfig
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}
}
