package org.fiuni.mytube_channels.converter;

import com.fiuni.mytube.dto.channel.ChannelDTO;
import com.fiuni.mytube.domain.channel.ChannelDomain;
import org.fiuni.mytube_channels.dao.IUserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelConverter implements IBaseConverter<ChannelDTO, ChannelDomain> {

    private static final Logger logger = LoggerFactory.getLogger(ChannelConverter.class);

    @Autowired
    private IUserDAO userDAO;

    @Override
    public ChannelDomain dtoToDomain(ChannelDTO dto) {
        logger.info("Convirtiendo ChannelDTO a ChannelDomain: {}", dto);

        ChannelDomain domain = new ChannelDomain();

        domain.setUser(userDAO.findById(dto.getUserId())
                .orElseThrow(
                        () -> new RuntimeException("User not found with ID: " + dto.getUserId())));
        // Maneja cuando no se encuentra el usuario domain.setChannelName(dto.getChannelName());

        domain.setCreationDate(dto.getCreationDate());
        domain.setChannelName(dto.getChannelName());
        domain.setChannelDescription(dto.getChannelDescription());
        domain.setChannelUrl(dto.getChannelUrl());
        domain.setSubscribersCount(dto.getSubscribersCount());
        domain.setDeleted(Boolean.FALSE);

        logger.info("ChannelDTO convertido a ChannelDomain: {}", domain);
        // Aquí asignarías el UserDomain correspondiente según el userId del DTO
        return domain;
    }

    @Override
    public ChannelDTO domainToDto(ChannelDomain domain) {
        logger.info("Convirtiendo ChannelDomain a ChannelDTO: {}", domain);

        ChannelDTO dto = new ChannelDTO();
        dto.set_id(domain.getId());
        dto.setChannelName(domain.getChannelName());
        dto.setChannelDescription(domain.getChannelDescription());
        dto.setCreationDate(domain.getCreationDate());
        dto.setChannelUrl(domain.getChannelUrl());
        dto.setSubscribersCount(domain.getSubscribersCount());
        dto.setUserId(domain.getUser().getId()); // Asignar el userId desde el UserDomain

        logger.info("ChannelDomain convertido a ChannelDTO: {}", dto);
        return dto;
    }
}
