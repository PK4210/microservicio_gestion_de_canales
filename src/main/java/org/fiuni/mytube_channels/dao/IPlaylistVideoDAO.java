package org.fiuni.mytube_channels.dao;

import com.fiuni.mytube.domain.playlistvideo.PlaylistVideoDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface IPlaylistVideoDAO extends JpaRepository<PlaylistVideoDomain, Integer> {
    // Obtener todos los canales
    @Override
    @NonNull
    Page<PlaylistVideoDomain> findAll(@NonNull Pageable pageable);
}
