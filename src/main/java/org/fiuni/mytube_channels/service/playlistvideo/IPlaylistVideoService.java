package org.fiuni.mytube_channels.service.playlistvideo;

import com.fiuni.mytube.dto.playlistvideo.PlaylistVideoDTO;
import com.fiuni.mytube.dto.playlistvideo.PlaylistVideoResult;
import org.fiuni.mytube_channels.service.base.IBaseService;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface IPlaylistVideoService extends IBaseService<PlaylistVideoDTO, PlaylistVideoResult> {
    PlaylistVideoDTO update(Integer id, PlaylistVideoDTO dto); // Método para actualizar

    @Transactional
    void delete(Integer id);

    // Implementación del método para obtener todos los canales con paginación
    PlaylistVideoResult getAll(Pageable pageable);
}
