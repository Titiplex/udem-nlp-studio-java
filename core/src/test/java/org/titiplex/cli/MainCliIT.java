package org.titiplex.cli;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainCliIT {

    @Test
    void prepareShouldGenerateConlluFromTxtAndYamlFixtures() throws Exception {
        Path input = resourcePath("cli/sample-input.txt");
        Path correction = resourcePath("cli/sample-correction.yaml");
        Path annotation = resourcePath("cli/sample-annotation.yaml");
        Path output = Files.createTempFile("nlp-studio-", ".conllu");

        org.titiplex.cli.Main.main(new String[]{
                "prepare",
                input.toString(),
                correction.toString(),
                annotation.toString(),
                output.toString()
        });

        assertTrue(Files.exists(output));
        String content = Files.readString(output, StandardCharsets.UTF_8);

        assertTrue(content.contains("# sent_id = 1"));
        assertTrue(content.contains("# text = ix-naq"));
        assertTrue(content.contains("VERB"));
        assertTrue(content.contains("Pers[subj]=1"));
    }

    @Test
    void statsShouldGenerateReadableReport() throws Exception {
        Path input = resourcePath("cli/sample-input.txt");
        Path correction = resourcePath("cli/sample-correction.yaml");
        Path output = Files.createTempFile("nlp-studio-stats-", ".txt");

        org.titiplex.cli.Main.main(new String[]{
                "stats",
                input.toString(),
                correction.toString(),
                output.toString()
        });

        assertTrue(Files.exists(output));
        String content = Files.readString(output, StandardCharsets.UTF_8);

        assertTrue(content.contains("Number of different tokens"));
        assertTrue(content.contains("Number of different morphemes"));
        assertTrue(content.contains("Percentage of"));
        assertTrue(content.contains("Tokens:"));
    }

    @Test
    void correctDocxShouldGenerateNonEmptyDocx() throws Exception {
        Path input = resourcePath("cli/sample-input.txt");
        Path correction = resourcePath("cli/sample-correction.yaml");
        Path output = Files.createTempFile("nlp-studio-corrected-", ".docx");

        org.titiplex.cli.Main.main(new String[]{
                "correct-docx",
                input.toString(),
                correction.toString(),
                output.toString()
        });

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0, "Generated DOCX should not be empty");
    }

    private Path resourcePath(String relative) {
        try {
            return Path.of(getClass().getClassLoader().getResource(relative).toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot resolve test resource: " + relative, e);
        }
    }
}