package org.fiuni.mytube_channels.service.playlistvideo;

import com.fiuni.mytube.domain.playlist.PlaylistDomain;
import com.fiuni.mytube.domain.playlistvideo.PlaylistVideoDomain;
import com.fiuni.mytube.domain.video.VideoDomain;
import com.fiuni.mytube.dto.channel.ChannelDTO;
import com.fiuni.mytube.dto.playlistvideo.PlaylistVideoDTO;
import com.fiuni.mytube.dto.playlistvideo.PlaylistVideoResult;
import org.fiuni.mytube_channels.converter.PlaylistVideoConverter;
import org.fiuni.mytube_channels.dao.IPlaylistDAO;
import org.fiuni.mytube_channels.dao.IPlaylistVideoDAO;
import org.fiuni.mytube_channels.dao.IVideoDAO;
import org.fiuni.mytube_channels.exception.DatabaseOperationException;
import org.fiuni.mytube_channels.exception.InvalidInputException;
import org.fiuni.mytube_channels.exception.ResourceNotFoundException;
import org.fiuni.mytube_channels.service.base.BaseServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaylistVideoServiceImpl extends BaseServiceImpl<PlaylistVideoDTO, PlaylistVideoDomain, PlaylistVideoResult> implements IPlaylistVideoService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistVideoServiceImpl.class);

    @Autowired
    private IPlaylistVideoDAO playlistVideoDao;

    @Autowired
    private PlaylistVideoConverter converter;

    @Autowired
    private IVideoDAO videoDAO;

    @Autowired
    private IPlaylistDAO playlistDAO;

    @Autowired
    private CacheManager cacheManager;

    @Override
    protected PlaylistVideoDTO convertDomainToDto(PlaylistVideoDomain domain) {
        return converter.domainToDto(domain); // Usa el convertidor para hacer la conversión
    }

    @Override
    protected PlaylistVideoDomain convertDtoToDomain(PlaylistVideoDTO dto) {
        return converter.dtoToDomain(dto); // Usa el convertidor para hacer la conversión
    }

    @Override
    public List<ChannelDTO> findAllOrderBySubscribersCountDesc() {
        return List.of();
    }

    @Override
    public PlaylistVideoResult getAll(){
        return null;
    }

    @Override
    public PlaylistVideoResult getAll(Pageable pageable) {
        logger.info("Buscando todos los PlaylistVideoDomains con paginación");

        // Obtener la página de dominios
        Page<PlaylistVideoDomain> page = playlistVideoDao.findAll(pageable);  // Cambiar esto si tienes un filtro similar a `findAllByDeletedFalse`
        List<PlaylistVideoDTO> playlistVideoList = page.getContent().stream()
                .map(this::convertDomainToDto)
                .toList(); // Convertir a DTOs

        // Guardar cada DTO individualmente en el caché
        playlistVideoList.forEach(playlistVideo -> {
            cacheManager.getCache("mytube_playlist_videos").put("playlist_video_" + playlistVideo.get_id(), playlistVideo);
            logger.info("PlaylistVideo guardado en caché con clave: playlist_video_{}", playlistVideo.get_id());
        });

        // Crear y devolver el resultado
        PlaylistVideoResult result = new PlaylistVideoResult();
        result.setPlaylistVideos(playlistVideoList);

        logger.info("Todos los PlaylistVideoDomains obtenidos exitosamente con paginación");

        return result;
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mytube_playlists_videos", key = "'playlist_video_' + #id")
    public PlaylistVideoDTO getById(Integer id) {
        logger.info("Buscando PlaylistVideoDomain con ID: {}", id);
        PlaylistVideoDomain domain = playlistVideoDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel con id " + id + " no encontrado"));
        return convertDomainToDto(domain);
    }

    @Override
    @Transactional
    @CachePut(value = "mytube_playlist_videos", key = "'playlist_video_' + #result._id")
    public PlaylistVideoDTO save(PlaylistVideoDTO dto) {
        logger.info("Guardando nuevo PlaylistVideoDomain desde PlaylistVideoDTO: {}", dto);

        // Validación de entrada
        if (dto.getPlaylistId() == null || dto.getVideoId() == null) {
            throw new InvalidInputException("El ID de la playlist y el video no pueden estar vacíos");
        }

        try {
            // Validar y obtener las entidades relacionadas
            PlaylistDomain playlist = playlistDAO.findById(dto.getPlaylistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Playlist con id " + dto.getPlaylistId() + " no encontrada"));
            VideoDomain video = videoDAO.findById(dto.getVideoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Video con id " + dto.getVideoId() + " no encontrado"));

            // Convertir DTO a dominio
            PlaylistVideoDomain domain = convertDtoToDomain(dto);
            domain.setPlaylist(playlist);
            domain.setVideo(video);

            // Guardar el dominio en la base de datos
            PlaylistVideoDomain savedDomain = playlistVideoDao.save(domain);
            logger.info("PlaylistVideoDomain guardado exitosamente: {}", savedDomain);

            // Convertir el dominio guardado a DTO
            PlaylistVideoDTO savedDto = convertDomainToDto(savedDomain);
            logger.info("PlaylistVideoDTO guardado con ID: {}", savedDto.get_id());

            return savedDto; // Este es el 'result' utilizado por @CachePut
        } catch (Exception e) {
            logger.error("Error al guardar el PlaylistVideo en la base de datos", e);
            throw new DatabaseOperationException("Error al guardar el PlaylistVideo en la base de datos");
        }
    }


    @Override
    @Transactional
    @CachePut(value = "mytube_playlist_videos", key = "'playlist_video_' + #result._id")
    public PlaylistVideoDTO update(Integer id, PlaylistVideoDTO dto) {
        logger.info("Actualizando PlaylistVideoDomain con ID: {}", id);

        return playlistVideoDao.findById(id)
                .map(domain -> {
                    logger.info("PlaylistVideoDomain encontrado para actualización: {}", domain);

                    // Validar y obtener entidades relacionadas
                    PlaylistDomain playlist = playlistDAO.findById(dto.getPlaylistId()).orElse(null);
                    VideoDomain video = videoDAO.findById(dto.getVideoId()).orElse(null);

                    if (playlist != null && video != null) {
                        // Actualizar los campos del dominio
                        domain.setPlaylist(playlist);
                        domain.setVideo(video);
                        PlaylistVideoDomain updatedDomain = playlistVideoDao.save(domain);
                        logger.info("PlaylistVideoDomain actualizado exitosamente: {}", updatedDomain);
                        return convertDomainToDto(updatedDomain);
                    } else {
                        if (playlist == null) logger.warn("PlaylistDomain no encontrado para ID: {}", dto.getPlaylistId());
                        if (video == null) logger.warn("VideoDomain no encontrado para ID: {}", dto.getVideoId());
                        return null;
                    }
                })
                .orElseGet(() -> {
                    logger.warn("Error al actualizar PlaylistVideoDomain, no encontrado para ID: {}", id);
                    return null;
                });
    }


    @Override
    @Transactional
    @CacheEvict(value = "mytube_playlist_videos", key = "'playlist_video_' + #id")
    public void delete(Integer id) {
        logger.info("Eliminando PlaylistVideoDomain con ID: {}", id);

        // Buscar el dominio antes de intentar eliminarlo
        PlaylistVideoDomain domain = playlistVideoDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlaylistVideo con id " + id + " no encontrado"));

        try {
            // Eliminar el dominio de la base de datos
            playlistVideoDao.delete(domain);
            logger.info("PlaylistVideoDomain eliminado exitosamente con ID: {}", id);
        } catch (Exception e) {
            logger.error("Error al eliminar el PlaylistVideo con ID: {}", id, e);
            throw new DatabaseOperationException("Error al eliminar el PlaylistVideo de la base de datos");
        }
    }

}
