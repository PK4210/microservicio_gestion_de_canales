package org.fiuni.mytube_channels.dao;

import com.fiuni.mytube.domain.channel.ChannelDomain;
import com.fiuni.mytube.domain.user.UserDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserDAO extends JpaRepository<UserDomain, Integer> {

}