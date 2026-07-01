package ru.anton.springtests2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anton.springtests2.dto.UserEnrichmentCreateDto;
import ru.anton.springtests2.dto.UserEnrichmentResponseDto;
import ru.anton.springtests2.exception.EntityNotFoundException;
import ru.anton.springtests2.mapper.UserEnrichmentMapper;
import ru.anton.springtests2.model.UserEnrichment;
import ru.anton.springtests2.repository.UserEnrichmentRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEnrichmentService {

    private final UserEnrichmentRepository userEnrichmentRepository;
    private final UserEnrichmentMapper userEnrichmentMapper;

    @Transactional
    public UserEnrichmentResponseDto createUserEnrichment(UserEnrichmentCreateDto dto) {

        UserEnrichment entity = userEnrichmentMapper.toEntity(dto);
        UserEnrichment saved = userEnrichmentRepository.save(entity);

        log.info("Обогащающие данные успешно созданы для userId: {}", saved.getUserId());
        return userEnrichmentMapper.toResponseDto(saved);
    }

    @Transactional
    public UserEnrichmentResponseDto getByUserId(UUID userId) {

        UserEnrichment entity = userEnrichmentRepository.findByUserId(userId)
                .filter(enrichment -> !enrichment.getIsDeleted())
                .orElseThrow(() -> {
                    log.error("Обогащающие данные для userId {} не найдены", userId);
                    return new EntityNotFoundException("Enrichment data for userId " + userId + " not found");
                });

        return userEnrichmentMapper.toResponseDto(entity);
    }
}
