package org.fiuni.mytube_channels.converter;

public interface IBaseConverter<DTO, DOMAIN> {
    DOMAIN dtoToDomain(DTO dto);
    DTO domainToDto(DOMAIN domain);
}
