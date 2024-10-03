package org.fiuni.mytube_channels.service.playlist;

import com.fiuni.mytube.domain.playlist.PlaylistVisibility;
import com.fiuni.mytube.dto.channel.ChannelDTO;
import org.fiuni.mytube_channels.converter.PlaylistConverter;
import org.fiuni.mytube_channels.dao.IPlaylistDAO;
import com.fiuni.mytube.domain.playlist.PlaylistDomain;
import com.fiuni.mytube.dto.playlist.PlaylistDTO;
import com.fiuni.mytube.dto.playlist.PlaylistResult;
import org.fiuni.mytube_channels.exception.DatabaseOperationException;
import org.fiuni.mytube_channels.exception.InvalidInputException;
import org.fiuni.mytube_channels.exception.ResourceNotFoundException;
import org.fiuni.mytube_channels.service.base.BaseServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaylistServiceImpl extends BaseServiceImpl<PlaylistDTO, PlaylistDomain, PlaylistResult> implements IPlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistServiceImpl.class);

    @Autowired
    private IPlaylistDAO playlistDao;

    @Autowired
    private PlaylistConverter converter;

    @Autowired
    private CacheManager cacheManager;

    @Override
    protected PlaylistDTO convertDomainToDto(PlaylistDomain domain) {
        logger.info("Convirtiendo PlaylistDomain a PlaylistDTO: {}", domain);
        PlaylistDTO dto = converter.domainToDto(domain);
        logger.info("PlaylistDomain convertido a PlaylistDTO: {}", dto);
        return dto;
    }

    @Override
    protected PlaylistDomain convertDtoToDomain(PlaylistDTO dto) {
        logger.info("Convirtiendo PlaylistDTO a PlaylistDomain: {}", dto);
        PlaylistDomain domain = converter.dtoToDomain(dto);
        logger.info("PlaylistDTO convertido a PlaylistDomain: {}", domain);
        return domain;
    }

    @Override
    public List<ChannelDTO> findAllOrderBySubscribersCountDesc() {
        return List.of();
    }

    @Override
    public PlaylistResult getAll() {
        return null;
    }

    // Implementación del método para obtener todos los canales con paginación
    @Override
    @Transactional (readOnly = true)
    public PlaylistResult getAll(Pageable pageable) {
        logger.info("Buscando todas las PlaylistDomains con paginación");

        // Obtener la página de dominios
        Page<PlaylistDomain> page = playlistDao.findAllByDeletedFalse(pageable);
        List<PlaylistDTO> playlistList = page.getContent().stream()
                .map(this::convertDomainToDto)
                .toList();

        // Guardar cada DTO individualmente en el caché
        playlistList.forEach(playlist -> {
            cacheManager.getCache("mytube_playlists").put("playlist_" + playlist.get_id(), playlist);
            logger.info("Playlist guardada en caché con clave: playlist_{}", playlist.get_id());
        });

        // Crear y devolver el resultado
        PlaylistResult result = new PlaylistResult();
        result.setPlaylists(playlistList);

        logger.info("Todas las PlaylistDomains obtenidas exitosamente con paginación");

        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public PlaylistDTO getById(Integer id) {
        logger.info("Buscando PlaylistDomain por ID: {}", id);

        // Buscar en el caché primero
        Cache cache = cacheManager.getCache("mytube_playlists");
        if (cache != null) {
            PlaylistDTO cachedPlaylist = cache.get("playlist_" + id, PlaylistDTO.class);
            if (cachedPlaylist != null) {
                logger.info("PlaylistDTO encontrado en caché con ID: {}", id);
                return cachedPlaylist;
            }
        }

        // Si no está en caché, buscar en la base de datos
        PlaylistDomain domain = playlistDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist con id " + id + " no encontrada"));

        // Convertir el dominio a DTO
        PlaylistDTO dto = converter.domainToDto(domain);

        // Guardar el resultado en el caché
        if (cache != null) {
            cache.put("playlist_" + id, dto);
            logger.info("PlaylistDTO guardado en caché con ID: {}", id);
        }

        return dto;
    }


    @Override
    @Transactional(rollbackFor = {InvalidInputException.class, DatabaseOperationException.class, Exception.class})
    public PlaylistDTO save(PlaylistDTO dto) {
        logger.info("Guardando nueva PlaylistDomain desde PlaylistDTO: {}", dto);

        // Validación de entrada
        if (dto.getPlaylistName() == null || dto.getPlaylistName().isEmpty()) {
            logger.warn("El nombre de la playlist está vacío, lanzando InvalidInputException");
            throw new InvalidInputException("El nombre de la playlist no puede estar vacío");
        }

        try {
            // Convertir DTO a Domain
            PlaylistDomain domain = convertDtoToDomain(dto);

            // Guardar en la base de datos y obtener el dominio guardado
            PlaylistDomain savedDomain = playlistDao.save(domain);
            logger.info("PlaylistDomain guardado exitosamente: {}", savedDomain);

            // Convertir el dominio guardado a DTO
            PlaylistDTO savedDto = convertDomainToDto(savedDomain);
            logger.info("PlaylistDTO guardado con ID: {}", savedDto.get_id());

            // Almacenar en el caché
            Cache cache = cacheManager.getCache("mytube_playlists");
            if (cache != null) {
                cache.put("playlist_" + savedDto.get_id(), savedDto);
                logger.info("PlaylistDTO guardado en caché con clave: playlist_{}", savedDto.get_id());
            }

            return savedDto;
        } catch (Exception e) {
            logger.error("Error al guardar la playlist en la base de datos", e);
            throw new DatabaseOperationException("Error al guardar la playlist en la base de datos");
        }
    }

    @Override
    @Transactional(rollbackFor = {InvalidInputException.class, DatabaseOperationException.class, ResourceNotFoundException.class, Exception.class})
    public PlaylistDTO update(Integer id, PlaylistDTO dto) {
        logger.info("Actualizando PlaylistDomain con ID: {}", id);

        // Buscar la playlist en la base de datos
        PlaylistDomain domain = playlistDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist con id " + id + " no encontrada"));

        // Validación del nombre de la playlist
        if (dto.getPlaylistName() == null || dto.getPlaylistName().isEmpty()) {
            logger.warn("El nombre de la playlist está vacío, lanzando InvalidInputException");
            throw new InvalidInputException("El nombre de la playlist no puede estar vacío");
        }

        try {
            // Actualizar el dominio con los valores del DTO
            domain.setPlaylistName(dto.getPlaylistName());
            domain.setCreationDate(dto.getCreationDate());
            domain.setVisibility(PlaylistVisibility.valueOf(dto.getVisibility()));

            // Guardar el dominio actualizado en la base de datos
            PlaylistDomain updatedDomain = playlistDao.save(domain);
            logger.info("PlaylistDomain actualizado exitosamente: {}", updatedDomain);

            // Convertir el dominio actualizado a DTO
            PlaylistDTO updatedDto = convertDomainToDto(updatedDomain);
            logger.info("PlaylistDTO actualizado: {}", updatedDto);

            // Asignar el _id del dominio actualizado al DTO
            updatedDto.set_id(updatedDomain.getId());

            // Almacenar en el caché
            Cache cache = cacheManager.getCache("mytube_playlists");
            if (cache != null) {
                cache.put("playlist_" + updatedDto.get_id(), updatedDto);
                logger.info("PlaylistDTO actualizado y guardado en caché con clave: playlist_{}", updatedDto.get_id());
            }

            return updatedDto;
        } catch (Exception e) {
            logger.error("Error al actualizar la playlist en la base de datos", e);
            throw new DatabaseOperationException("Error al actualizar la playlist en la base de datos");
        }
    }

    @Override
    @Transactional(rollbackFor = {ResourceNotFoundException.class, DatabaseOperationException.class, Exception.class})
    public void softDelete(Integer id) {
        logger.info("Eliminando PlaylistDomain con ID: {}", id);

        // Buscar el dominio en la base de datos
        PlaylistDomain domain = playlistDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist con id " + id + " no encontrada"));

        try {
            // Marcar el dominio como eliminado (eliminación lógica)
            domain.setDeleted(Boolean.TRUE);
            playlistDao.save(domain);
            logger.info("PlaylistDomain marcado como eliminado con ID: {}", id);

            // Eliminar el elemento del caché
            Cache cache = cacheManager.getCache("mytube_playlists");
            if (cache != null) {
                cache.evict("playlist_" + id);
                logger.info("PlaylistDTO eliminado del caché con clave: playlist_{}", id);
            }
        } catch (Exception e) {
            logger.error("Error al eliminar lógicamente la playlist con ID: {}", id, e);
            throw new DatabaseOperationException("Error al eliminar lógicamente la playlist con ID: " + id);
        }
    }

}
