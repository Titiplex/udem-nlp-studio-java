package org.titiplex.app.service;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Component
public class FileDialogService {

    private Stage owner;

    public void setOwner(Stage owner) {
        this.owner = owner;
    }

    public String saveTextFile(String title,
                               String suggestedFileName,
                               String content,
                               List<String> extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(suggestedFileName);

        if (extensions != null && !extensions.isEmpty()) {
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Supported files", extensions)
            );
        }

        File file = chooser.showSaveDialog(owner);
        if (file == null) {
            return "";
        }

        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            return file.getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot save file: " + e.getMessage(), e);
        }
    }

    public String openTextFile(String title, List<String> extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);

        if (extensions != null && !extensions.isEmpty()) {
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Supported files", extensions)
            );
        }

        File file = chooser.showOpenDialog(owner);
        if (file == null) {
            return "";
        }

        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read file: " + e.getMessage(), e);
        }
    }
}