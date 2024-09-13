package org.fiuni.mytube_channels.dao;

import com.fiuni.mytube.domain.video.VideoDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IVideoDAO extends JpaRepository<VideoDomain, Integer> {

}