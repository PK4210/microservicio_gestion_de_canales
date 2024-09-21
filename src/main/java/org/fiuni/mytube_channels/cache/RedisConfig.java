package org.fiuni.mytube_channels.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Configuración de ObjectMapper personalizado
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);

        // Serializador JSON con ObjectMapper personalizado
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Configuración global del caché con TTL de 10 minutos
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL global de 10 minutos
                .disableCachingNullValues() // No almacenar valores nulos en caché
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        // Configuración específica para el caché de canales SIN TTL
        RedisCacheConfiguration channelCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues() // No almacenar valores nulos
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .prefixCacheNameWith("channels::"); // Prefijo de la clave para diferenciación

        // Configuración específica para el caché de playlists con TTL de 20 minutos
        RedisCacheConfiguration playlistCacheConfig = defaultCacheConfig
                .entryTtl(Duration.ofMinutes(20)) // TTL de 20 minutos para playlists
                .prefixCacheNameWith("playlists::"); // Prefijo de la clave para diferenciación

        // Configuración específica para el caché de videos de playlists con TTL de 30 minutos
        RedisCacheConfiguration playlistVideoCacheConfig = defaultCacheConfig
                .entryTtl(Duration.ofMinutes(30)) // TTL de 30 minutos para videos de playlists
                .prefixCacheNameWith("playlistVideos::"); // Prefijo de la clave para diferenciación

        // Mapa para definir las configuraciones de cada caché
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("mytube_channels", channelCacheConfig); // Sin TTL
        cacheConfigurations.put("mytube_playlists", playlistCacheConfig); // Con TTL
        cacheConfigurations.put("mytube_playlist_videos", playlistVideoCacheConfig); // Con TTL

        // Configurar el administrador de caché
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig) // Configuración global
                .withInitialCacheConfigurations(cacheConfigurations) // Configuraciones específicas
                .transactionAware() // Asegura que el caché se gestione en el contexto de las transacciones
                .build();
    }
}
