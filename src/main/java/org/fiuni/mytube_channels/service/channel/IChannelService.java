package org.fiuni.mytube_channels.service.channel;

import com.fiuni.mytube.dto.channel.ChannelDTO;
import com.fiuni.mytube.dto.channel.ChannelResult;
import org.fiuni.mytube_channels.service.base.IBaseService;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface IChannelService extends IBaseService<ChannelDTO, ChannelResult> {

    // Implementación del método para obtener todos los canales con paginación
    ChannelResult getAll(Pageable pageable);

    // Eliminar un canal de forma lógica (marcando como eliminado)
    void softDelete(Integer id);

    // Método para actualizar un canal
    ChannelDTO update(Integer id, ChannelDTO dto);

    List<ChannelDTO> findByChannelName(String channelName);

    List<ChannelDTO> findAllOrderBySubscribersCountDesc();

    List<ChannelDTO> findActiveChannels();

}
