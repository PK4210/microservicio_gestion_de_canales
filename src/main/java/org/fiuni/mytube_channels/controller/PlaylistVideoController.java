package org.fiuni.mytube_channels.controller;

import com.fiuni.mytube.dto.playlistvideo.PlaylistVideoDTO;
import com.fiuni.mytube.dto.playlistvideo.PlaylistVideoResult;
import org.fiuni.mytube_channels.service.playlistvideo.IPlaylistVideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlist_videos")
public class PlaylistVideoController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistVideoController.class);

    @Autowired
    private IPlaylistVideoService playlistVideoService;

    @GetMapping
    public ResponseEntity<List<PlaylistVideoDTO>> getAllPlaylistVideos(Pageable pageable) {
        logger.info("Recibiendo solicitud para obtener todos los PlaylistVideos");

        // Obtenemos la lista de PlaylistVideoDTO desde el servicio
        PlaylistVideoResult result = playlistVideoService.getAll(pageable);

        // Comprobamos si hay resultados y los devolvemos
        if (result.getPlaylistVideos().isEmpty()) {
            logger.info("No se encontraron PlaylistVideos");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        logger.info("Retornando {} PlaylistVideoDTOs", result.getPlaylistVideos().size());
        return new ResponseEntity<>(result.getPlaylistVideos(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistVideoDTO> getPlaylistVideoById(@PathVariable Integer id) {
        logger.info("Recibiendo solicitud para obtener PlaylistVideo con ID: {}", id);
        PlaylistVideoDTO dto = playlistVideoService.getById(id);
        if (dto != null) {
            logger.info("PlaylistVideo encontrado con ID: {}", id);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } else {
            logger.warn("PlaylistVideo no encontrado con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<PlaylistVideoDTO> createPlaylistVideo(@RequestBody PlaylistVideoDTO dto) {
        logger.info("Recibiendo solicitud para crear un nuevo PlaylistVideo");
        PlaylistVideoDTO created = playlistVideoService.save(dto);
        logger.info("PlaylistVideo creado exitosamente con ID: {}", created.get_id());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaylistVideoDTO> updatePlaylistVideo(@PathVariable Integer id, @RequestBody PlaylistVideoDTO dto) {
        logger.info("Recibiendo solicitud para actualizar PlaylistVideo con ID: {}", id);
        PlaylistVideoDTO updated = playlistVideoService.update(id, dto);
        if (updated != null) {
            logger.info("PlaylistVideo actualizado exitosamente con ID: {}", id);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } else {
            logger.warn("Error al actualizar PlaylistVideo con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylistVideo(@PathVariable Integer id) {
        logger.info("Recibiendo solicitud para eliminar PlaylistVideo con ID: {}", id);
        playlistVideoService.delete(id);
        logger.info("PlaylistVideo eliminado exitosamente con ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
