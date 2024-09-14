package org.fiuni.mytube_channels.dao;

import com.fiuni.mytube.domain.playlist.PlaylistDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPlaylistDAO extends JpaRepository<PlaylistDomain, Integer> {
    // Obtener todos los canales que no están eliminados
    Page<PlaylistDomain> findAllByDeletedFalse(Pageable pageable);

    boolean existsByPlaylistName(String playlistName);
}

