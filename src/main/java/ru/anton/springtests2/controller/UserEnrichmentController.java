package ru.anton.springtests2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.anton.springtests2.dto.UserEnrichmentCreateDto;
import ru.anton.springtests2.dto.UserEnrichmentResponseDto;
import ru.anton.springtests2.service.UserEnrichmentService;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/enrich")
@RequiredArgsConstructor
public class UserEnrichmentController {

    private final UserEnrichmentService userEnrichmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserEnrichmentResponseDto createEnrichment(@Valid @RequestBody UserEnrichmentCreateDto dto) {
        return userEnrichmentService.createUserEnrichment(dto);
    }

    @GetMapping("/{id}")
    public UserEnrichmentResponseDto getEnrichment(@PathVariable("id") UUID userId) {
        return userEnrichmentService.getByUserId(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEnrichment(@PathVariable("id") UUID userId) {
        userEnrichmentService.deleteByUserId(userId);
    }
}
