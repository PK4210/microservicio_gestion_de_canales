package org.fiuni.mytube_channels.dao;

import com.fiuni.mytube.domain.channel.ChannelDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IChannelDAO extends JpaRepository<ChannelDomain, Integer> {

    // Obtener todos los canales que no están eliminados
    Page<ChannelDomain> findAllByDeletedFalse(Pageable pageable);

    List<ChannelDomain> findByDeletedFalse();

    List<ChannelDomain> findByChannelNameContaining(String channelName);

    List<ChannelDomain> findAllByOrderBySubscribersCountDesc();

}