package org.titiplex.app.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Stream;

public final class FrontendRuntimeAssets {

    private static final String WEBAPP_ROOT = "/webapp";
    private static final String INDEX_RESOURCE = WEBAPP_ROOT + "/index.html";

    private FrontendRuntimeAssets() {
    }

    public static URL prepareIndexUrl() {
        URL indexUrl = FrontendRuntimeAssets.class.getResource(INDEX_RESOURCE);
        if (indexUrl == null) {
            throw new IllegalStateException("Frontend not found in classpath: " + INDEX_RESOURCE);
        }

        try {
            URI indexUri = indexUrl.toURI();

            // En dev / tests, on peut tomber sur file: directement
            if (!"jar".equalsIgnoreCase(indexUri.getScheme())) {
                return indexUrl;
            }

            Path runtimeDir = Files.createTempDirectory("nlp-studio-webapp-");
            runtimeDir.toFile().deleteOnExit();

            copyWebappTo(runtimeDir, asJarRoot(indexUri));
            return runtimeDir.resolve("index.html").toUri().toURL();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to prepare frontend runtime assets", e);
        }
    }

    private static URI asJarRoot(URI indexUri) {
        String raw = indexUri.toString();
        int bang = raw.indexOf("!/");
        if (bang < 0) {
            return indexUri;
        }
        return URI.create(raw.substring(0, bang + 2));
    }

    private static void copyWebappTo(Path targetRoot, URI jarRootUri) throws IOException {
        FileSystem fileSystem = null;
        boolean closeFs = false;

        try {
            try {
                fileSystem = FileSystems.newFileSystem(jarRootUri, Map.of());
                closeFs = true;
            } catch (FileSystemAlreadyExistsException ignored) {
                fileSystem = FileSystems.getFileSystem(jarRootUri);
            }

            Path sourceRoot = fileSystem.getPath(WEBAPP_ROOT);

            try (Stream<Path> stream = Files.walk(sourceRoot)) {
                stream.forEach(source -> copyOne(sourceRoot, source, targetRoot));
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        } finally {
            if (closeFs && fileSystem != null) {
                fileSystem.close();
            }
        }
    }

    private static void copyOne(Path sourceRoot, Path source, Path targetRoot) {
        try {
            Path relative = sourceRoot.relativize(source);
            Path target = targetRoot.resolve(relative.toString());

            if (Files.isDirectory(source)) {
                Files.createDirectories(target);
                return;
            }

            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            target.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}