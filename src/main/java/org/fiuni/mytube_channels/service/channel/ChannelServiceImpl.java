package org.fiuni.mytube_channels.service.channel;

import com.fiuni.mytube.dto.channel.ChannelDTO;
import com.fiuni.mytube.dto.channel.ChannelResult;
import com.fiuni.mytube.domain.channel.ChannelDomain;
import org.fiuni.mytube_channels.converter.ChannelConverter;
import org.fiuni.mytube_channels.dao.IChannelDAO;
import org.fiuni.mytube_channels.exception.*;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChannelServiceImpl extends BaseServiceImpl<ChannelDTO, ChannelDomain, ChannelResult> implements IChannelService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelServiceImpl.class);

    @Autowired
    private IChannelDAO channelDao;

    @Autowired
    private ChannelConverter converter;

    @Autowired
    private CacheManager cacheManager;

    @Override
    protected ChannelDTO convertDomainToDto(ChannelDomain domain) {
        ChannelDTO dto = converter.domainToDto(domain);
        return dto;
    }

    @Override
    protected ChannelDomain convertDtoToDomain(ChannelDTO dto) {
        ChannelDomain domain = converter.dtoToDomain(dto);
        return domain;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mytube_channels", key = "'channel_' + #id")
    public ChannelDTO getById(Integer id) {
        logger.info("Obteniendo canal por ID: {}", id);
        ChannelDomain domain = channelDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel con id " + id + " no encontrado"));
        logger.info("Canal con ID {} encontrado", id);
        return converter.domainToDto(domain);
    }

    @Override
    @Transactional(rollbackFor = {ResourceNotFoundException.class, InvalidInputException.class, Exception.class})
    @CachePut(value = "mytube_channels", key = "'channel_' + #dto._id")
    public ChannelDTO update(Integer id, ChannelDTO dto) {
        logger.info("Iniciando actualización del ChannelDomain con ID: {}", id);

        ChannelDomain domain = channelDao.findById(id).orElse(null);
        if (domain == null) {
            logger.warn("No se encontró ChannelDomain con ID: {}", id);
            throw new ResourceNotFoundException("Channel con id " + id + " no encontrado");
        }

        // Verificar si el nombre del canal ha cambiado
        if (!domain.getChannelName().equals(dto.getChannelName())) {
            validateChannelName(dto.getChannelName()); // Solo validar si el nombre es diferente
            domain.setChannelName(dto.getChannelName());
        }

        logger.info("ChannelDomain encontrado para actualización. Actualizando campos...");
        try {
            // Solo actualizar la descripción del canal
            domain.setChannelDescription(dto.getChannelDescription());
            // Mantener los demás campos sin cambios para evitar conflictos
            domain.setChannelUrl(domain.getChannelUrl());
            domain.setSubscribersCount(domain.getSubscribersCount());

            ChannelDomain updatedDomain = channelDao.save(domain);
            logger.info("ChannelDomain actualizado exitosamente: {}", updatedDomain);

            ChannelDTO updatedDto = convertDomainToDto(updatedDomain);
            dto.set_id(updatedDomain.getId());
            logger.info("Actualización exitosa del canal, guardado en caché con clave: channel_{}", dto.get_id());
            return updatedDto;
        } catch (Exception e) {
            logger.error("Error durante la actualización del ChannelDomain con ID: {}. Realizando rollback.", id, e);
            throw new DatabaseOperationException("Error al actualizar el canal con ID: " + id);
        }
    }


    // Implementación del método para obtener todos los canales sin paginación (si es necesario)
    @Override
    public ChannelResult getAll() {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelResult getAll(Pageable pageable) {
        logger.info("Buscando todos los ChannelDomains con paginación");

        try {
            Page<ChannelDomain> page = channelDao.findAllByDeletedFalse(pageable);
            List<ChannelDTO> channelList = page.getContent().stream()
                    .map(this::convertDomainToDto)
                    .toList();

            channelList.forEach(channel -> {
                cacheManager.getCache("mytube_channels").put("channel_" + channel.get_id(), channel);
            });

            ChannelResult result = new ChannelResult();
            result.setChannels(channelList);

            logger.info("Todos los ChannelDomains obtenidos exitosamente con paginación");
            return result;
        } catch (Exception e) {
            logger.error("Error al obtener los canales con paginación de la base de datos.", e);
            throw new DatabaseOperationException("Error al obtener los canales con paginación de la base de datos");
        }
    }


    @Override
    @Transactional(rollbackFor = {ResourceNotFoundException.class, Exception.class})
    @CacheEvict(value = "mytube_channels", key = "'channel_' + #id")
    public void softDelete(Integer id) {
        logger.info("Iniciando eliminación lógica del ChannelDomain con ID: {}", id);

        ChannelDomain domain = channelDao.findById(id).orElse(null);
        if (domain == null) {
            logger.warn("No se encontró ChannelDomain con ID: {}", id);
            throw new ResourceNotFoundException("Channel con id " + id + " no encontrado");
        }

        try {
            domain.setDeleted(true);  // Eliminación lógica
            channelDao.save(domain);  // Persistir el cambio en la base de datos
            logger.info("ChannelDomain eliminado lógicamente con éxito: {}", domain);
        } catch (Exception e) {
            logger.error("Error durante la eliminación lógica del ChannelDomain con ID: {}. Realizando rollback.", id, e);
            throw new DatabaseOperationException("Error al eliminar el canal con ID: " + id);
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {DatabaseOperationException.class, InvalidInputException.class, Exception.class}, timeout =  1)
    @CachePut(value = "mytube_channels", key = "'channel_' + #dto._id")
    public ChannelDTO save(ChannelDTO dto) {
        logger.info("Iniciando guardado de nuevo ChannelDomain.");

        validateChannelName(dto.getChannelName());

        try {
            ChannelDomain domain = convertDtoToDomain(dto);
            ChannelDomain savedDomain = channelDao.save(domain);
            logger.info("ChannelDomain guardado exitosamente con ID: {}", savedDomain.getId());

            dto.set_id(savedDomain.getId());
            logger.info("Guardado exitoso del canal en caché con ID: {}", dto.get_id());
            logger.info("Transacción completada exitosamente. No se realizó rollback.");
            return dto;
        } catch (Exception e) {
            logger.error("Error al guardar el canal. Realizando rollback: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error al guardar el canal en la base de datos");
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<ChannelDTO> findByChannelName(String channelName) {
        logger.info("Buscando canales con nombre: {}", channelName);
        List<ChannelDomain> domains = channelDao.findByChannelNameContaining(channelName);
        logger.info("Canales encontrados: {}", domains.size());
        return convertDomainListToDtoList(domains);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<ChannelDTO> findAllOrderBySubscribersCountDesc() {
        logger.info("Buscando todos los canales ordenados por número de suscriptores.");
        List<ChannelDomain> domains = channelDao.findAllByOrderBySubscribersCountDesc();
        logger.info("Canales ordenados por suscriptores encontrados: {}", domains.size());
        return convertDomainListToDtoList(domains);
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public List<ChannelDTO> findActiveChannels() {
        logger.info("Intentando ejecutar findActiveChannels sin transacción (Propagation.NEVER)");

        try {
            List<ChannelDomain> domains = channelDao.findByDeletedFalse();
            logger.info("findActiveChannels ejecutado exitosamente sin transacción");
            return convertDomainListToDtoList(domains);
        } catch (Exception e) {
            logger.error("Error al ejecutar findActiveChannels sin transacción", e);
            throw e;
        }
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    public void validateChannelName(String channelName) {
        logger.info("Validando nombre del canal: {}", channelName);

        if (channelName == null || channelName.isEmpty()) {
            logger.warn("El nombre del canal está vacío. Lanzando InvalidInputException.");
            throw new InvalidInputException("El nombre del canal no puede estar vacío");
        }

        if (channelDao.existsByChannelName(channelName)) {
            logger.warn("Ya existe un canal con el nombre {}", channelName);
            throw new ConflictException("Ya existe un canal con el nombre " + channelName);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelDTO> findByUserId(Integer userId) {
        logger.info("Buscando canales asociados al usuario con ID: {}", userId);
        try {
            List<ChannelDomain> domains = channelDao.findByUserId(userId);
            if (domains.isEmpty()) {
                logger.warn("No se encontraron canales para el usuario con ID: {}", userId);
                throw new ResourceNotFoundException("No se encontraron canales para el usuario con ID: " + userId);
            }
            logger.info("Canales encontrados para el usuario con ID: {}", userId);
            return convertDomainListToDtoList(domains);
        } catch (Exception e) {
            logger.error("Error al buscar canales para el usuario con ID: {}", userId, e);
            throw e;
        }
    }

}
