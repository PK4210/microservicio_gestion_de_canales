package org.fiuni.mytube_channels.service.channel;

import com.fiuni.mytube.dto.channel.ChannelDTO;
import com.fiuni.mytube.dto.channel.ChannelResult;
import com.fiuni.mytube.domain.channel.ChannelDomain;
import org.fiuni.mytube_channels.converter.ChannelConverter;
import org.fiuni.mytube_channels.dao.IChannelDAO;
import org.fiuni.mytube_channels.exception.ResourceNotFoundException;
import org.fiuni.mytube_channels.service.base.BaseServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChannelServiceImpl extends BaseServiceImpl<ChannelDTO, ChannelDomain, ChannelResult> implements IChannelService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelServiceImpl.class);

    @Autowired
    private IChannelDAO channelDao;

    @Autowired
    private ChannelConverter converter;


    @Override
    protected ChannelDTO convertDomainToDto(ChannelDomain domain) {
        logger.info("Convirtiendo ChannelDomain a ChannelDTO: {}", domain);
        ChannelDTO dto = converter.domainToDto(domain);
        logger.info("ChannelDomain convertido a ChannelDTO: {}", dto);
        return dto;
    }

    @Override
    protected ChannelDomain convertDtoToDomain(ChannelDTO dto) {
        logger.info("Convirtiendo ChannelDTO a ChannelDomain: {}", dto);
        ChannelDomain domain = converter.dtoToDomain(dto);
        logger.info("ChannelDTO convertido a ChannelDomain: {}", domain);
        return domain;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value="sd", key="'channel_'+#id")
    public ChannelDTO getById(Integer id) {
        ChannelDomain domain = channelDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel con id " + id + " no encontrado"));
        return converter.domainToDto(domain);
    }

    @Override
    @Transactional
    public ChannelDTO update(Integer id, ChannelDTO dto) {
        logger.info("Actualizando ChannelDomain con ID: {}", id);
        ChannelDomain domain = channelDao.findById(id).orElse(null);

        if (domain != null) {
            logger.info("ChannelDomain encontrado para actualización: {}", domain);

            domain.setChannelName(dto.getChannelName());
            domain.setChannelDescription(dto.getChannelDescription());
            domain.setChannelUrl(dto.getChannelUrl());
            domain.setSubscribersCount(dto.getSubscribersCount());

            ChannelDomain updatedDomain = channelDao.save(domain);
            logger.info("ChannelDomain actualizado exitosamente: {}", updatedDomain);
            return convertDomainToDto(updatedDomain);
        } else {
            logger.warn("Error al actualizar ChannelDomain, no encontrado para ID: {}", id);
            return null;
        }
    }

    // Implementación del método para obtener todos los canales sin paginación (si es necesario)
    @Override
    public ChannelResult getAll() {
        return null;
    }

    // Implementación del método para obtener todos los canales con paginación
    @Override
    public ChannelResult getAll(Pageable pageable) {
        logger.info("Buscando todos los ChannelDomains con paginación");

        Page<ChannelDomain> page = channelDao.findAllByDeletedFalse(pageable);  // Paginación
        List<ChannelDTO> channelList = page.getContent().stream()
                .map(this::convertDomainToDto)
                .toList();// Convertir a DTOs

        ChannelResult result = new ChannelResult();
        result.setChannels(channelList);

        logger.info("Todos los ChannelDomains obtenidos exitosamente con paginación");

        return result;
    }

    @Override
    @Transactional
    public void softDelete(Integer id) {
        logger.info("Eliminación lógica de ChannelDomain con ID: {}", id);
        ChannelDomain domain = channelDao.findById(id).orElse(null);
        if (domain != null) {
            domain.setDeleted(Boolean.TRUE);
            channelDao.save(domain);
            logger.info("ChannelDomain eliminado lógicamente con ID: {}", id);
        } else {
            logger.warn("ChannelDomain no encontrado para eliminación lógica con ID: {}", id);
        }
    }

    @Override
    @Transactional
    public ChannelDTO save(ChannelDTO dto) {
        logger.info("Guardando nuevo ChannelDomain desde ChannelDTO: {}", dto);
        ChannelDomain domain = convertDtoToDomain(dto);
        ChannelDomain savedDomain = channelDao.save(domain);
        logger.info("ChannelDomain guardado exitosamente: {}", savedDomain);
        return convertDomainToDto(savedDomain);
    }

    @Override
    public List<ChannelDTO> findByChannelName(String channelName) {
        List<ChannelDomain> domains = channelDao.findByChannelNameContaining(channelName);
        return convertDomainListToDtoList(domains);
    }

    @Override
    public List<ChannelDTO> findAllOrderBySubscribersCountDesc() {
        List<ChannelDomain> domains = channelDao.findAllByOrderBySubscribersCountDesc();
        return convertDomainListToDtoList(domains);
    }

    @Override
    public List<ChannelDTO> findActiveChannels() {
        List<ChannelDomain> domains = channelDao.findByDeletedFalse();
        return convertDomainListToDtoList(domains);
    }
}
