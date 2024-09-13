package org.fiuni.mytube_channels.converter;

import com.fiuni.mytube.dto.playlist.PlaylistDTO;
import com.fiuni.mytube.domain.playlist.PlaylistDomain;
import com.fiuni.mytube.domain.playlist.PlaylistVisibility;
import com.fiuni.mytube.domain.user.UserDomain;
import org.fiuni.mytube_channels.dao.IUserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlaylistConverter implements IBaseConverter<PlaylistDTO, PlaylistDomain> {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistConverter.class);

    @Autowired
    private IUserDAO userDAO;

    @Override
    public PlaylistDTO domainToDto(PlaylistDomain domain) {
        logger.info("Convirtiendo PlaylistDomain a PlaylistDTO: {}", domain);

        PlaylistDTO dto = new PlaylistDTO();

        dto.set_id(domain.getId());
        dto.setUserId(domain.getUser().getId());
        dto.setPlaylistName(domain.getPlaylistName());
        dto.setCreationDate(domain.getCreationDate());
        dto.setVisibility(domain.getVisibility().toString());  // Convertir enum a String

        logger.info("PlaylistDomain convertido a PlaylistDTO: {}", dto);
        return dto;
    }

    @Override
    public PlaylistDomain dtoToDomain(PlaylistDTO dto) {
        logger.info("Convirtiendo PlaylistDTO a PlaylistDomain: {}", dto);

        PlaylistDomain domain = new PlaylistDomain();

        domain.setUser(userDAO.findById(dto.getUserId())
                .orElseThrow(
                        () -> new RuntimeException("User not found with ID: " + dto.getUserId())));
        // Maneja cuando no se encuentra el usuario domain.setChannelName(dto.getChannelName());


        domain.setPlaylistName(dto.getPlaylistName());
        domain.setVisibility(PlaylistVisibility.valueOf(dto.getVisibility()));  // Convertir String a enum
        domain.setDeleted(false);

        logger.info("PlaylistDTO convertido a PlaylistDomain: {}", domain);
        return domain;
    }
}
