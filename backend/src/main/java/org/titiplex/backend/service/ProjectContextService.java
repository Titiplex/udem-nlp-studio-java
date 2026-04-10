package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.dto.*;
import org.titiplex.backend.model.ActiveProjectEntity;
import org.titiplex.backend.model.LocalIdentityEntity;
import org.titiplex.backend.model.ProjectRegistryEntity;
import org.titiplex.backend.project.*;
import org.titiplex.backend.repository.ActiveProjectRepository;
import org.titiplex.backend.repository.LocalIdentityRepository;
import org.titiplex.backend.repository.ProjectRegistryRepository;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectContextService {

    private static final long ACTIVE_PROJECT_SINGLETON_ID = 1L;
    private static final long LOCAL_IDENTITY_SINGLETON_ID = 1L;

    private final ProjectRegistryRepository projectRegistryRepository;
    private final ActiveProjectRepository activeProjectRepository;
    private final LocalIdentityRepository localIdentityRepository;
    private final ManifestProjectLoader manifestProjectLoader;
    private final ProjectSecretService projectSecretService;

    public ProjectContextService(ProjectRegistryRepository projectRegistryRepository,
                                 ActiveProjectRepository activeProjectRepository,
                                 LocalIdentityRepository localIdentityRepository,
                                 ManifestProjectLoader manifestProjectLoader,
                                 ProjectSecretService projectSecretService) {
        this.projectRegistryRepository = projectRegistryRepository;
        this.activeProjectRepository = activeProjectRepository;
        this.localIdentityRepository = localIdentityRepository;
        this.manifestProjectLoader = manifestProjectLoader;
        this.projectSecretService = projectSecretService;
    }

    public List<ProjectSummaryDto> listKnownProjects() {
        UUID active = activeProjectRepository.findById(ACTIVE_PROJECT_SINGLETON_ID)
                .map(ActiveProjectEntity::getActiveProjectId)
                .orElse(null);

        return projectRegistryRepository.findAllByOrderByLastOpenedAtDescNameAsc().stream()
                .map(entity -> {
                    ProjectManifest manifest = manifestProjectLoader.load(Path.of(entity.getManifestPath()));
                    ProjectSourceDefinition defaultSource = manifest.sources().stream()
                            .filter(source -> source.id().equals(manifest.defaultSourceId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Default source not found"));

                    return new ProjectSummaryDto(
                            entity.getProjectId(),
                            entity.getName(),
                            entity.getProjectId().equals(active),
                            defaultSource.kind().name(),
                            defaultSource.host() + "/" + defaultSource.database() + " [" + defaultSource.schema() + "]",
                            entity.getLastOpenedAt()
                    );
                })
                .toList();
    }

    public ProjectSummaryDto registerProject(Path manifestPath) {
        ProjectManifest manifest = manifestProjectLoader.load(manifestPath);
        ProjectRegistryEntity entity = new ProjectRegistryEntity(
                manifest.projectId(),
                manifest.name(),
                manifestPath.toAbsolutePath().toString(),
                Instant.now(),
                false
        );
        projectRegistryRepository.save(entity);
        switchActiveProject(manifest.projectId());
        return listKnownProjects().stream()
                .filter(project -> project.projectId().equals(manifest.projectId()))
                .findFirst()
                .orElseThrow();
    }

    public ProjectDetailDto createProject(CreateProjectRequestDto request) {
        UUID projectId = UUID.randomUUID();
        Path directory = Path.of(request.directory());
        Path manifestPath = directory.resolve("project.yaml");

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create project directory: " + e.getMessage(), e);
        }

        LocalIdentityEntity localIdentity = getOrCreateLocalIdentity();
        String schema = normalizeSchema(request.schema(), request.name(), projectId);

        ProjectManifest manifest = new ProjectManifest(
                projectId,
                request.name(),
                "1",
                request.sourceId(),
                List.of(new ProjectMember(localIdentity.getPrincipalId(), localIdentity.getDisplayName(), ProjectRole.OWNER)),
                List.of(new ProjectSourceDefinition(
                        request.sourceId(),
                        ProjectSourceKind.POSTGRESQL,
                        request.host(),
                        request.port() == null ? 5432 : request.port(),
                        request.database(),
                        schema,
                        "secret://" + request.sourceId() + "/username",
                        "secret://" + request.sourceId() + "/password",
                        request.ssl()
                ))
        );

        writeManifest(manifestPath, manifest);
        registerProject(manifestPath);
        projectSecretService.saveSecret(projectId, "secret://" + request.sourceId() + "/username", request.username());
        projectSecretService.saveSecret(projectId, "secret://" + request.sourceId() + "/password", request.password());
        return getActiveProject();
    }

    public void switchActiveProject(UUID projectId) {
        ActiveProjectEntity entity = new ActiveProjectEntity(ACTIVE_PROJECT_SINGLETON_ID, projectId);
        activeProjectRepository.save(entity);

        projectRegistryRepository.findById(projectId).ifPresent(project -> {
            project.setLastOpenedAt(Instant.now());
            projectRegistryRepository.save(project);
        });
    }

    public ProjectContext getRequiredActiveContext() {
        UUID activeProjectId = activeProjectRepository.findById(ACTIVE_PROJECT_SINGLETON_ID)
                .map(ActiveProjectEntity::getActiveProjectId)
                .orElseThrow(() -> new IllegalStateException("No active project selected"));

        ProjectRegistryEntity registryEntity = projectRegistryRepository.findById(activeProjectId)
                .orElseThrow(() -> new IllegalStateException("Active project not registered: " + activeProjectId));

        ProjectManifest manifest = manifestProjectLoader.load(Path.of(registryEntity.getManifestPath()));
        LocalIdentityEntity localIdentity = getOrCreateLocalIdentity();

        ProjectMember localMember = manifest.members().stream()
                .filter(member -> member.principalId().equals(localIdentity.getPrincipalId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Local identity is not a member of project " + manifest.name()));

        ProjectSourceDefinition defaultSource = manifest.sources().stream()
                .filter(source -> source.id().equals(manifest.defaultSourceId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Default source missing for project " + manifest.name()));

        return new ProjectContext(
                manifest.projectId(),
                manifest.name(),
                Path.of(registryEntity.getManifestPath()),
                manifest,
                localMember,
                defaultSource
        );
    }

    public ProjectDetailDto getActiveProject() {
        ProjectContext ctx = getRequiredActiveContext();
        return toDetailDto(ctx, true);
    }

    public LocalIdentityEntity getOrCreateLocalIdentity() {
        return localIdentityRepository.findById(LOCAL_IDENTITY_SINGLETON_ID)
                .orElseGet(() -> localIdentityRepository.save(new LocalIdentityEntity(
                        LOCAL_IDENTITY_SINGLETON_ID,
                        "pubkey:" + UUID.randomUUID(),
                        System.getProperty("user.name", "local-user"),
                        "PUBLIC_KEY_PLACEHOLDER",
                        "PRIVATE_KEY_PLACEHOLDER"
                )));
    }

    private ProjectDetailDto toDetailDto(ProjectContext ctx, boolean active) {
        List<ProjectMemberDto> members = ctx.manifest().members().stream()
                .map(member -> new ProjectMemberDto(member.principalId(), member.displayName(), member.role().name()))
                .toList();

        List<ProjectSourceDto> sources = ctx.manifest().sources().stream()
                .map(source -> new ProjectSourceDto(
                        source.id(),
                        source.kind().name(),
                        source.host(),
                        source.port(),
                        source.database(),
                        source.schema(),
                        source.ssl(),
                        projectSecretService.resolveSecret(ctx.projectId(), source.usernameRef()).isPresent()
                                && projectSecretService.resolveSecret(ctx.projectId(), source.passwordRef()).isPresent()
                ))
                .collect(Collectors.toList());

        return new ProjectDetailDto(
                ctx.projectId(),
                ctx.name(),
                ctx.manifest().version(),
                active,
                members,
                sources
        );
    }

    private void writeManifest(Path manifestPath, ProjectManifest manifest) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("projectId", manifest.projectId().toString());
        root.put("name", manifest.name());
        root.put("version", manifest.version());
        root.put("defaultSourceId", manifest.defaultSourceId());

        List<Map<String, Object>> members = new ArrayList<>();
        for (ProjectMember member : manifest.members()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("principalId", member.principalId());
            m.put("displayName", member.displayName());
            m.put("role", member.role().name());
            members.add(m);
        }
        root.put("members", members);

        List<Map<String, Object>> sources = new ArrayList<>();
        for (ProjectSourceDefinition source : manifest.sources()) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("id", source.id());
            s.put("kind", source.kind().name());
            s.put("host", source.host());
            s.put("port", source.port());
            s.put("database", source.database());
            s.put("schema", source.schema());
            s.put("usernameRef", source.usernameRef());
            s.put("passwordRef", source.passwordRef());
            s.put("ssl", source.ssl());
            sources.add(s);
        }
        root.put("sources", sources);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        Yaml yaml = new Yaml(options);

        try {
            Files.writeString(manifestPath, yaml.dump(root));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write manifest: " + e.getMessage(), e);
        }
    }

    private String normalizeSchema(String rawSchema, String projectName, UUID projectId) {
        if (rawSchema != null && !rawSchema.isBlank()) {
            return rawSchema.trim();
        }
        String base = projectName == null ? "project" : projectName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (base.isBlank()) {
            base = "project";
        }
        String suffix = projectId.toString().replace("-", "").substring(0, 8);
        return "nlp_" + base + "_" + suffix;
    }
}