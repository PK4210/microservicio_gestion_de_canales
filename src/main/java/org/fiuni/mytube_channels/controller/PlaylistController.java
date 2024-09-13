package org.fiuni.mytube_channels.controller;

import com.fiuni.mytube.dto.playlist.PlaylistDTO;
import com.fiuni.mytube.dto.playlist.PlaylistResult;
import org.fiuni.mytube_channels.service.playlist.IPlaylistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    @Autowired
    private IPlaylistService playlistService;

    // Crear una nueva playlist
    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(@RequestBody PlaylistDTO playlist) {
        logger.info("Solicitud para crear una nueva playlist: {}", playlist);
        PlaylistDTO createdPlaylist = playlistService.save(playlist);
        logger.info("Playlist creada exitosamente: {}", createdPlaylist);
        return new ResponseEntity<>(createdPlaylist, HttpStatus.CREATED);
    }

    // Obtener una playlist por ID
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDTO> getPlaylistById(@PathVariable Integer id) {
        logger.info("Solicitud para obtener playlist con ID: {}", id);
        PlaylistDTO playlist = playlistService.getById(id);
        if (playlist != null) {
            logger.info("Playlist encontrada con ID: {}", id);
            return new ResponseEntity<>(playlist, HttpStatus.OK);
        } else {
            logger.warn("Playlist no encontrada con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Obtener todas las playlists sin paginación
    @GetMapping
    public ResponseEntity<List<PlaylistDTO>> getAllPlaylists(Pageable pageable) {
        logger.info("Solicitud para obtener todas las playlists.");

        PlaylistResult result = playlistService.getAll(pageable);

        logger.info("Todas las playlists obtenidas exitosamente.");
        return new ResponseEntity<>(result.getPlaylists(), HttpStatus.OK);
    }

    // Actualizar una playlist existente
    @PutMapping("/{id}")
    public ResponseEntity<PlaylistDTO> updatePlaylist(@PathVariable Integer id, @RequestBody PlaylistDTO playlist) {
        logger.info("Solicitud para actualizar playlist con ID: {}", id);
        PlaylistDTO updatedPlaylist = playlistService.update(id, playlist);
        if (updatedPlaylist != null) {
            logger.info("Playlist actualizada exitosamente con ID: {}", id);
            return new ResponseEntity<>(updatedPlaylist, HttpStatus.OK);
        } else {
            logger.warn("Error al actualizar la playlist con ID: {}. Playlist no encontrada.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar una playlist (puedes modificar para eliminación lógica si lo prefieres)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Integer id) {
        logger.info("Solicitud para eliminar playlist con ID: {}", id);
        playlistService.softDelete(id);
        logger.info("Playlist eliminada exitosamente con ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
