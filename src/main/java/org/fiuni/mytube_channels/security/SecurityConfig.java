package org.fiuni.mytube_channels.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configurando la cadena de filtros de seguridad");

        http
                .authorizeHttpRequests((auth) -> {
                    logger.info("Permitiendo todas las solicitudes");
                    auth.anyRequest().permitAll();  // Permitir todas las solicitudes
                })
                .csrf(csrf -> {
                    logger.info("Deshabilitando protección CSRF para todos los endpoints");
                    csrf.ignoringRequestMatchers("/**");  // Ignorar CSRF en todos los endpoints
                });

        logger.info("Cadena de filtros de seguridad configurada exitosamente");
        return http.build();
    }
}
