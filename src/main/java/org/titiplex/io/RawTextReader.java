package org.titiplex.io;

import org.titiplex.model.RawBlock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reader for plain-text interlinear blocks.
 * Supports both:
 * - blank-line separated blocks with 3 lines
 * - numbered examples with optional continuation lines
 */
public final class RawTextReader implements BlockReader {
    @Override
    public List<RawBlock> read(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(InterlinearBlockParser.normalize(line));
            }

            boolean hasNumberedExamples = lines.stream().anyMatch(InterlinearBlockParser::isNumberedStart);
            if (hasNumberedExamples) {
                return InterlinearBlockParser.parseNumberedLines(lines);
            }

            List<RawBlock> out = new ArrayList<>();
            List<String> current = new ArrayList<>();
            int id = 1;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    if (!current.isEmpty()) {
                        out.add(toBlock(id++, current));
                        current.clear();
                    }
                    continue;
                }
                current.add(trimmed);
            }
            if (!current.isEmpty()) {
                out.add(toBlock(id, current));
            }
            return out;
        }
    }

    private RawBlock toBlock(int id, List<String> lines) {
        String chuj = !lines.isEmpty() ? lines.get(0) : "";
        String gloss = lines.size() > 1 ? lines.get(1) : "";
        String translation = lines.size() > 2 ? String.join(" ", lines.subList(2, lines.size())) : "";
        return new RawBlock(id, chuj, gloss, translation);
    }
}
