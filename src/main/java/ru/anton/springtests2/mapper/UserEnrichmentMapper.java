package ru.anton.springtests2.mapper;

import org.mapstruct.Mapper;
import ru.anton.springtests2.dto.UserEnrichmentCreateDto;
import ru.anton.springtests2.dto.UserEnrichmentResponseDto;
import ru.anton.springtests2.model.UserEnrichment;

@Mapper(componentModel = "spring")
public interface UserEnrichmentMapper {

    UserEnrichment toEntity(UserEnrichmentCreateDto dto);

    UserEnrichmentResponseDto toResponseDto(UserEnrichment entity);
}
