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
    //hola
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mytube_channels", key = "'channel_' + #id")
    public ChannelDTO getById(Integer id) {
        ChannelDomain domain = channelDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel con id " + id + " no encontrado"));
        return converter.domainToDto(domain);
    }

    @Override
    @Transactional(rollbackFor = {ResourceNotFoundException.class, InvalidInputException.class ,Exception.class})  // Rollback ante excepciones
    @CachePut(value = "mytube_channels", key = "'channel_' + #dto._id")
    public ChannelDTO update(Integer id, ChannelDTO dto) {
        logger.info("Actualizando ChannelDomain con ID: {}", id);

        // Validación de entrada
        if (dto.getChannelName() == null || dto.getChannelName().isEmpty()) {
            logger.warn("El nombre del canal está vacío, lanzando InvalidInputException");
            throw new InvalidInputException("El nombre del canal no puede estar vacío");
        }

        // Buscar el canal por ID, lanzar excepción si no se encuentra
        ChannelDomain domain = channelDao.findById(id).orElse(null);
        if (domain == null) {
            logger.warn("Error al actualizar ChannelDomain, no encontrado para ID: {}", id);
            throw new ResourceNotFoundException("Channel con id " + id + " no encontrado");
        }

        logger.info("ChannelDomain encontrado para actualización: {}", domain);

        try {
            // Actualizar los campos del dominio con los valores del DTO
            domain.setChannelName(dto.getChannelName());
            domain.setChannelDescription(dto.getChannelDescription());
            domain.setChannelUrl(dto.getChannelUrl());
            domain.setSubscribersCount(dto.getSubscribersCount());

            // Guardar el dominio actualizado en la base de datos
            ChannelDomain updatedDomain = channelDao.save(domain);
            logger.info("ChannelDomain actualizado exitosamente: {}", updatedDomain);

            // Convertir el dominio actualizado a DTO
            ChannelDTO updatedDto = convertDomainToDto(updatedDomain);

            // Asignar el _id del dominio actualizado al DTO antes de la cache
            dto.set_id(updatedDomain.getId());
            logger.info("Actualización exitosa del canal, guardando en caché con clave: channel_{}", dto.get_id());

            return updatedDto;
        } catch (Exception e) {
            logger.error("Error al actualizar el ChannelDomain con ID: {}", id, e);
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
            // Realizar la búsqueda con paginación
            Page<ChannelDomain> page = channelDao.findAllByDeletedFalse(pageable);  // Paginación
            List<ChannelDTO> channelList = page.getContent().stream()
                    .map(this::convertDomainToDto)
                    .toList();  // Convertir a DTOs

            // Guardar en caché los canales obtenidos
            channelList.forEach(channel -> {
                cacheManager.getCache("mytube_channels").put("channel_" + channel.get_id(), channel);
            });

            ChannelResult result = new ChannelResult();
            result.setChannels(channelList);

            logger.info("Todos los ChannelDomains obtenidos exitosamente con paginación");

            return result;
        } catch (Exception e) {
            logger.error("Error al obtener los canales con paginación", e);
            throw new DatabaseOperationException("Error al obtener los canales con paginación");
        }
    }


    @Override
    @Transactional(rollbackFor = {ResourceNotFoundException.class, Exception.class})  // Rollback ante excepciones específicas y generales
    @CacheEvict(value = "mytube_channels", key = "'channel_' + #id")
    public void softDelete(Integer id) {
        logger.info("Eliminación lógica de ChannelDomain con ID: {}", id);

        // Buscar el canal por ID, lanzar excepción si no se encuentra
        ChannelDomain domain = channelDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Canal con id " + id + " no encontrado"));

        try {
            // Marcar el canal como eliminado lógicamente
            domain.setDeleted(Boolean.TRUE);
            channelDao.save(domain);
            logger.info("ChannelDomain eliminado lógicamente con ID: {}", id);

            // Eliminar manualmente del caché si es necesario
            cacheManager.getCache("mytube_channels").evict("channel_" + id);
        } catch (Exception e) {
            logger.error("Error al eliminar lógicamente el canal con ID: {}", id, e);
            throw new DatabaseOperationException("Error al eliminar lógicamente el canal con ID: " + id);
        }
    }

    @Override
    @Transactional(rollbackFor = {DatabaseOperationException.class, Exception.class})  // Rollback ante cualquier excepción
    @CachePut(value = "mytube_channels", key = "'channel_' + #dto._id")
    public ChannelDTO save(ChannelDTO dto) {
        logger.info("Guardando nuevo ChannelDomain desde ChannelDTO: {}", dto);

        // Validación de entrada
        if (dto.getChannelName() == null || dto.getChannelName().isEmpty()) {
            logger.warn("El nombre del canal está vacío, lanzando InvalidInputException");
            throw new InvalidInputException("El nombre del canal no puede estar vacío");
        }

        // Comprobar si ya existe un canal con el mismo nombre
        if (channelDao.existsByChannelName(dto.getChannelName())) {
            logger.warn("Ya existe un canal con el mismo nombre");
            throw new ConflictException("Ya existe un canal con el nombre " + dto.getChannelName());
        }

        try {
            // Convertir DTO a Domain
            ChannelDomain domain = convertDtoToDomain(dto);

            // Guardar en la base de datos y obtener el _id generado
            ChannelDomain savedDomain = channelDao.save(domain);
            logger.info("ChannelDomain guardado exitosamente: {}", savedDomain);

            // Asignar el _id generado en la base de datos al DTO
            dto.set_id(savedDomain.getId()); // Asegúrate de que savedDomain tiene el ID generado
            logger.info("Guardando canal en caché con ID: {}", dto.get_id());

            // No es necesario guardar manualmente en el caché, @CachePut lo hará automáticamente
            return dto;
        } catch (Exception e) {
            logger.error("Error al guardar el canal en la base de datos", e);
            throw new DatabaseOperationException("Error al guardar el canal en la base de datos");
        }
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
