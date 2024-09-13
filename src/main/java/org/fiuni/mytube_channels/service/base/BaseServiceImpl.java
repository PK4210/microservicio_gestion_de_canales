package org.fiuni.mytube_channels.service.base;

import com.fiuni.mytube.dto.base.BaseDTO;
import com.fiuni.mytube.dto.base.BaseResult;
import com.fiuni.mytube.dto.channel.ChannelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseServiceImpl<DTO extends BaseDTO, DOMAIN, RESULT extends BaseResult<DTO>>
        implements IBaseService<DTO, RESULT> {

    private static final Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);

    protected abstract DTO convertDomainToDto(DOMAIN domain);

    protected abstract DOMAIN convertDtoToDomain(DTO dto);

    protected List<DTO> convertDomainListToDtoList(List<DOMAIN> domainList) {
        logger.info("Convirtiendo lista de dominios a DTOs: {}", domainList);
        List<DTO> dtoList = domainList.stream()
                .map(this::convertDomainToDto)
                .collect(Collectors.toList());
        logger.info("Lista de dominios convertida a DTOs: {}", dtoList);
        return dtoList;
    }

    protected List<DOMAIN> convertDtoListToDomainList(List<DTO> dtoList) {
        logger.info("Convirtiendo lista de DTOs a dominios: {}", dtoList);
        List<DOMAIN> domainList = dtoList.stream()
                .map(this::convertDtoToDomain)
                .collect(Collectors.toList());
        logger.info("Lista de DTOs convertida a dominios: {}", domainList);
        return domainList;
    }

    public abstract List<ChannelDTO> findAllOrderBySubscribersCountDesc();
}
