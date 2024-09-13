package org.fiuni.mytube_channels.service.playlist;

import com.fiuni.mytube.dto.playlist.PlaylistDTO;
import com.fiuni.mytube.dto.playlist.PlaylistResult;
import org.fiuni.mytube_channels.service.base.IBaseService;
import org.springframework.data.domain.Pageable;


public interface IPlaylistService extends IBaseService<PlaylistDTO, PlaylistResult> {

    // Actualizar una playlist
    PlaylistDTO update(Integer id, PlaylistDTO dto);

    // Eliminar una playlist
    void softDelete(Integer id);

    // Implementación del método para obtener todos los canales con paginación
    PlaylistResult getAll(Pageable pageable);
}
