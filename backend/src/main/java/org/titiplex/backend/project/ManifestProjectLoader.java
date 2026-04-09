package org.titiplex.backend.project;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class ManifestProjectLoader {

    private final Yaml yaml = new Yaml();

    public ProjectManifest load(Path manifestPath) {
        try (InputStream in = Files.newInputStream(manifestPath)) {
            Object raw = yaml.load(in);
            if (!(raw instanceof Map<?, ?> root)) {
                throw new IllegalArgumentException("Invalid project manifest root");
            }

            Map<String, Object> map = toStringKeyMap(root);
            UUID projectId = UUID.fromString(String.valueOf(map.get("projectId")));
            String name = String.valueOf(map.get("name"));
            String version = String.valueOf(map.getOrDefault("version", "1"));
            String defaultSourceId = String.valueOf(map.get("defaultSourceId"));

            List<ProjectMember> members = new ArrayList<>();
            Object membersRaw = map.get("members");
            if (membersRaw instanceof List<?> list) {
                for (Object item : list) {
                    if (!(item instanceof Map<?, ?> memberMapRaw)) continue;
                    Map<String, Object> memberMap = toStringKeyMap(memberMapRaw);
                    members.add(new ProjectMember(
                            String.valueOf(memberMap.get("principalId")),
                            String.valueOf(memberMap.getOrDefault("displayName", "")),
                            ProjectRole.valueOf(String.valueOf(memberMap.get("role")).toUpperCase())
                    ));
                }
            }

            List<ProjectSourceDefinition> sources = new ArrayList<>();
            Object sourcesRaw = map.get("sources");
            if (sourcesRaw instanceof List<?> list) {
                for (Object item : list) {
                    if (!(item instanceof Map<?, ?> sourceMapRaw)) continue;
                    Map<String, Object> sourceMap = toStringKeyMap(sourceMapRaw);
                    sources.add(new ProjectSourceDefinition(
                            String.valueOf(sourceMap.get("id")),
                            ProjectSourceKind.valueOf(String.valueOf(sourceMap.get("kind")).toUpperCase()),
                            String.valueOf(sourceMap.get("host")),
                            Integer.parseInt(String.valueOf(sourceMap.getOrDefault("port", 5432))),
                            String.valueOf(sourceMap.get("database")),
                            String.valueOf(sourceMap.getOrDefault("schema", "public")),
                            String.valueOf(sourceMap.get("usernameRef")),
                            String.valueOf(sourceMap.get("passwordRef")),
                            Boolean.parseBoolean(String.valueOf(sourceMap.getOrDefault("ssl", true)))
                    ));
                }
            }

            return new ProjectManifest(projectId, name, version, defaultSourceId, members, sources);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load manifest: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> toStringKeyMap(Map<?, ?> raw) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (var entry : raw.entrySet()) {
            out.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return out;
    }
}