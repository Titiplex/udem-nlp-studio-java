package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.model.LocalSecretEntity;
import org.titiplex.backend.repository.LocalSecretRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectSecretService {

    private final LocalSecretRepository localSecretRepository;

    public ProjectSecretService(LocalSecretRepository localSecretRepository) {
        this.localSecretRepository = localSecretRepository;
    }

    public void saveSecret(UUID projectId, String secretRef, String rawValue) {
        LocalSecretEntity entity = localSecretRepository.findByProjectIdAndSecretRef(projectId, secretRef)
                .orElseGet(() -> new LocalSecretEntity(UUID.randomUUID(), projectId, secretRef, ""));
        entity.setSecretJsonEncrypted(encode(rawValue));
        localSecretRepository.save(entity);
    }

    public Optional<String> resolveSecret(UUID projectId, String secretRef) {
        return localSecretRepository.findByProjectIdAndSecretRef(projectId, secretRef)
                .map(LocalSecretEntity::getSecretJsonEncrypted)
                .map(this::decode);
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}