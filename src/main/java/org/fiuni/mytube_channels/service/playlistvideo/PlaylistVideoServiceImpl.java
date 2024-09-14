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

        Page<PlaylistVideoDomain> page = playlistVideoDao.findAll(pageable);  // Paginación
        List<PlaylistVideoDTO> playlistVideoList = page.getContent().stream()
                .map(this::convertDomainToDto)
                .toList();// Convertir a DTOs

        PlaylistVideoResult result = new PlaylistVideoResult();
        result.setPlaylistVideos(playlistVideoList);

        logger.info("Todos los PlaylistVideoDomain obtenidos exitosamente con paginación");

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistVideoDTO getById(Integer id) {
        logger.info("Buscando PlaylistVideoDomain con ID: {}", id);
        PlaylistVideoDomain domain = playlistVideoDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel con id " + id + " no encontrado"));
        return convertDomainToDto(domain);
    }

    @Override
    @Transactional
    public PlaylistVideoDTO save(PlaylistVideoDTO dto) {
        logger.info("Guardando nuevo PlaylistVideoDomain desde PlaylistVideoDTO: {}", dto);

        if (dto.getPlaylistId() == null || dto.getVideoId() == null) {
            throw new InvalidInputException("El ID de la playlist y el video no pueden estar vacíos");
        }

        try {
            PlaylistDomain playlist = playlistDAO.findById(dto.getPlaylistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Playlist con id " + dto.getPlaylistId() + " no encontrado"));
            VideoDomain video = videoDAO.findById(dto.getVideoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Video con id " + dto.getVideoId() + " no encontrado"));

            PlaylistVideoDomain domain = convertDtoToDomain(dto);
            domain.setPlaylist(playlist);
            domain.setVideo(video);
            PlaylistVideoDomain savedDomain = playlistVideoDao.save(domain);
            logger.info("PlaylistVideoDomain guardado exitosamente: {}", savedDomain);
            return convertDomainToDto(savedDomain);
        } catch (Exception e) {
            throw new DatabaseOperationException("Error al guardar el PlaylistVideo en la base de datos");
        }
    }

    @Override
    @Transactional
    public PlaylistVideoDTO update(Integer id, PlaylistVideoDTO dto) {
        logger.info("Actualizando PlaylistVideoDomain con ID: {}", id);

        return playlistVideoDao.findById(id)
                .map(domain -> {
                    logger.info("PlaylistVideoDomain encontrado para actualización: {}", domain);

                    PlaylistDomain playlist = playlistDAO.findById(dto.getPlaylistId()).orElse(null);
                    VideoDomain video = videoDAO.findById(dto.getVideoId()).orElse(null);

                    if (playlist != null && video != null) {
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
    public void delete(Integer id) {
        logger.info("Eliminando PlaylistVideoDomain con ID: {}", id);
        PlaylistVideoDomain domain = playlistVideoDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlaylistVideo con id " + id + " no encontrado"));

        try {
            playlistVideoDao.delete(domain);
            logger.info("PlaylistVideoDomain eliminado exitosamente");
        } catch (Exception e) {
            throw new DatabaseOperationException("Error al eliminar el PlaylistVideo de la base de datos");
        }
    }
}
