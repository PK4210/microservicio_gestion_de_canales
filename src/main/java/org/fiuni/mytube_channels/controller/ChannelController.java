package org.fiuni.mytube_channels.controller;

import com.fiuni.mytube.dto.channel.ChannelDTO;
import com.fiuni.mytube.dto.channel.ChannelResult;
import org.fiuni.mytube_channels.service.channel.IChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/channels")
public class ChannelController {

    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);

    @Autowired
    private IChannelService channelService;

    @PostMapping
    public ResponseEntity<ChannelDTO> createChannel(@RequestBody ChannelDTO channelDto) {
        logger.info("Solicitud para crear un nuevo canal: {}", channelDto);
        ChannelDTO createdChannel = channelService.save(channelDto);
        logger.info("Canal creado exitosamente: {}", createdChannel);
        return new ResponseEntity<>(createdChannel, HttpStatus.CREATED);
    }

    // Obtener todos los canales sin paginación
    @GetMapping
    public ResponseEntity<List<ChannelDTO>> getAllChannels(Pageable pageable) {
        logger.info("Solicitud para obtener todos los canales.");

        ChannelResult result = channelService.getAll(pageable);

        // Revisa si los datos ya están duplicados aquí
        logger.info("Lista de canales obtenida: {}", result.get_dtos());
        logger.info("Número total de canales: {}", result.getTotal());

        return new ResponseEntity<>(result.getChannels(), HttpStatus.OK);
    }


    // Obtener un canal por ID
    @GetMapping("/{id}")
    public ResponseEntity<ChannelDTO> getChannelById(@PathVariable("id") Integer id) {
        logger.info("Solicitud para obtener canal con ID: {}", id);
        ChannelDTO channel = channelService.getById(id);
        if (channel != null) {
            logger.info("Canal encontrado con ID: {}", id);
            return new ResponseEntity<>(channel, HttpStatus.OK);
        } else {
            logger.warn("Canal no encontrado con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //Actualizar un canal por ID
    @PutMapping("/{id}")
    public ResponseEntity<ChannelDTO> updateChannel(@PathVariable("id") Integer id, @RequestBody ChannelDTO channelDto) {
        logger.info("Solicitud para actualizar canal con ID: {}", id);
        ChannelDTO updatedChannel = channelService.update(id, channelDto);
        if (updatedChannel != null) {
            logger.info("Canal actualizado exitosamente con ID: {}", id);
            return new ResponseEntity<>(updatedChannel, HttpStatus.OK);
        } else {
            logger.warn("No se pudo actualizar el canal con ID: {}. Canal no encontrado.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //Eliminar un canal de manera
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable("id") Integer id) {
        logger.info("Solicitud para eliminar canal con ID: {}", id);
        channelService.softDelete(id);
        logger.info("Canal eliminado exitosamente con ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChannelDTO>> searchChannelsByName(@RequestParam String name) {
        try {
            List<ChannelDTO> channels = channelService.findByChannelName(name);
            return new ResponseEntity<>(channels, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error al buscar canales por nombre", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/top-subscribers")
    public ResponseEntity<List<ChannelDTO>> getTopChannels() {
        List<ChannelDTO> channels = channelService.findAllOrderBySubscribersCountDesc();
        return new ResponseEntity<>(channels, HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ChannelDTO>> getActiveChannels() {
        List<ChannelDTO> channels = channelService.findActiveChannels();
        return new ResponseEntity<>(channels, HttpStatus.OK);
    }
}
