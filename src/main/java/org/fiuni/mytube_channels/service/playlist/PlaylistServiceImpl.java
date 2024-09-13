package org.fiuni.mytube_channels.service.playlist;

import com.fiuni.mytube.domain.playlist.PlaylistVisibility;
import com.fiuni.mytube.dto.channel.ChannelDTO;
import org.fiuni.mytube_channels.converter.PlaylistConverter;
import org.fiuni.mytube_channels.dao.IPlaylistDAO;
import com.fiuni.mytube.domain.playlist.PlaylistDomain;
import com.fiuni.mytube.dto.playlist.PlaylistDTO;
import com.fiuni.mytube.dto.playlist.PlaylistResult;
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
public class PlaylistServiceImpl extends BaseServiceImpl<PlaylistDTO, PlaylistDomain, PlaylistResult> implements IPlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistServiceImpl.class);

    @Autowired
    private IPlaylistDAO playlistDao;

    @Autowired
    private PlaylistConverter converter;

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
    public PlaylistResult getAll(Pageable pageable) {
        logger.info("Buscando todos los ChannelDomains con paginación");

        Page<PlaylistDomain> page = playlistDao.findAllByDeletedFalse(pageable);  // Paginación
        List<PlaylistDTO> playlistList = page.getContent().stream()
                .map(this::convertDomainToDto)
                .toList();// Convertir a DTOs

        PlaylistResult result = new PlaylistResult();
        result.setPlaylists(playlistList);

        logger.info("Todos los ChannelDomains obtenidos exitosamente con paginación");

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistDTO getById(Integer id) {
        logger.info("Buscando PlaylistDomain por ID: {}", id);
        PlaylistDomain domain = playlistDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist con id " + id + " no encontrado"));
        return converter.domainToDto(domain);
    }

    @Override
    @Transactional
    public PlaylistDTO save(PlaylistDTO dto) {
        logger.info("Guardando nuevo PlaylistDomain desde PlaylistDTO: {}", dto);
        PlaylistDomain domain = convertDtoToDomain(dto);
        PlaylistDomain savedDomain = playlistDao.save(domain);
        logger.info("PlaylistDomain guardado exitosamente: {}", savedDomain);
        return convertDomainToDto(savedDomain);
    }

    @Override
    @Transactional
    public PlaylistDTO update(Integer id, PlaylistDTO dto) {
        logger.info("Actualizando PlaylistDomain con ID: {}", id);
        PlaylistDomain domain = playlistDao.findById(id).orElse(null);
        if (domain != null) {
            logger.info("PlaylistDomain encontrado para actualización: {}", domain);
            domain.setPlaylistName(dto.getPlaylistName());
            domain.setCreationDate(dto.getCreationDate());
            domain.setVisibility(PlaylistVisibility.valueOf(dto.getVisibility()));

            PlaylistDomain updatedDomain = playlistDao.save(domain);
            logger.info("PlaylistDomain actualizado exitosamente: {}", updatedDomain);
            return convertDomainToDto(updatedDomain);
        } else {
            logger.warn("Error al actualizar PlaylistDomain, no encontrado para ID: {}", id);
            return null;
        }
    }

    @Transactional
    @Override
    public void softDelete(Integer id) {
        logger.info("Eliminando PlaylistDomain con ID: {}", id);
        PlaylistDomain domain = playlistDao.findById(id).orElse(null);
        if (domain != null) {
            domain.setDeleted(Boolean.TRUE);
            playlistDao.save(domain);
        } else {
            logger.info("PlaylistDomain no encontro el elemento con ID: {}", id);
        }
    }
}
