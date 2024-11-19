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

        // Configuración global del caché sin TTL (TTL infinito)
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ZERO) // TTL infinito (sin expiración)
                .disableCachingNullValues() // No almacenar valores nulos en caché
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        // Configuración específica para el caché de canales SIN TTL (TTL infinito)
        RedisCacheConfiguration channelCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ZERO) // TTL infinito (sin expiración)
                .disableCachingNullValues() // No almacenar valores nulos
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .prefixCacheNameWith("channels::"); // Prefijo de la clave para diferenciación

        // Configuración específica para el caché de playlists SIN TTL (TTL infinito)
        RedisCacheConfiguration playlistCacheConfig = defaultCacheConfig
                .entryTtl(Duration.ZERO) // TTL infinito (sin expiración)
                .prefixCacheNameWith("playlists::"); // Prefijo de la clave para diferenciación

        // Configuración específica para el caché de videos de playlists SIN TTL (TTL infinito)
        RedisCacheConfiguration playlistVideoCacheConfig = defaultCacheConfig
                .entryTtl(Duration.ZERO) // TTL infinito (sin expiración)
                .prefixCacheNameWith("playlistVideos::"); // Prefijo de la clave para diferenciación

        // Mapa para definir las configuraciones de cada caché
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("mytube_channels", channelCacheConfig); // Sin TTL
        cacheConfigurations.put("mytube_playlists", playlistCacheConfig); // Sin TTL
        cacheConfigurations.put("mytube_playlist_videos", playlistVideoCacheConfig); // Sin TTL

        // Configurar el administrador de caché
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig) // Configuración global
                .withInitialCacheConfigurations(cacheConfigurations) // Configuraciones específicas
                .transactionAware() // Asegura que el caché se gestione en el contexto de las transacciones
                .build();
    }
}
