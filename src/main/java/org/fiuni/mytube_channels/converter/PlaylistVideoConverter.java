package org.fiuni.mytube_channels.converter;

import com.fiuni.mytube.domain.playlistvideo.PlaylistVideoDomain;
import com.fiuni.mytube.dto.playlistvideo.PlaylistVideoDTO;
import org.fiuni.mytube_channels.dao.IPlaylistDAO;
import org.fiuni.mytube_channels.dao.IVideoDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlaylistVideoConverter implements IBaseConverter<PlaylistVideoDTO, PlaylistVideoDomain> {

    @Autowired
    public PlaylistVideoConverter(IPlaylistDAO playlistDAO, IVideoDAO videoDAO) {
        this.playlistDAO = playlistDAO;
        this.videoDAO = videoDAO;
    }

    @Autowired
    private IPlaylistDAO playlistDAO;

    @Autowired
    private IVideoDAO videoDAO;

    @Override
    public PlaylistVideoDTO domainToDto(PlaylistVideoDomain domain) {
        PlaylistVideoDTO dto = new PlaylistVideoDTO();
        dto.set_id(domain.getId());
        dto.setPlaylistId(domain.getPlaylist().getId());
        dto.setVideoId(domain.getVideo().getId());
        return dto;
    }

    @Override
    public PlaylistVideoDomain dtoToDomain(PlaylistVideoDTO dto) {
        PlaylistVideoDomain domain = new PlaylistVideoDomain();

        domain.setPlaylist(playlistDAO.findById(dto.getPlaylistId())
                .orElseThrow(
                        () -> new RuntimeException("Playlist not found with ID: " + dto.getPlaylistId())));

        domain.setVideo(videoDAO.findById(dto.getVideoId())
                .orElseThrow(
                        () -> new RuntimeException("Video not found with ID: " + dto.getVideoId())));
        return domain;
    }

}
