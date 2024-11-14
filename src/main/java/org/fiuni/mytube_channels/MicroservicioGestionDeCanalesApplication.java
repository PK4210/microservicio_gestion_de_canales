package org.fiuni.mytube_channels;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "org.fiuni.mytube_channels.dao")
@EntityScan(basePackages = "com.fiuni.mytube.domain")
@EnableCaching
public class MicroservicioGestionDeCanalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroservicioGestionDeCanalesApplication.class, args);
    }
}
