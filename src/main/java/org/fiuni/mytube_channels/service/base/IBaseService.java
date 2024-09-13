package org.fiuni.mytube_channels.service.base;

import com.fiuni.mytube.dto.base.BaseDTO;
import com.fiuni.mytube.dto.base.BaseResult;

public interface IBaseService<DTO extends BaseDTO, R extends BaseResult<DTO>> {
    DTO save(DTO dto);

    DTO getById(Integer id);

    R getAll();
    }

