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
 * Expected default shape per block:
 * 1) Chuj line
 * 2) gloss line
 * 3) translation line
 * Blocks are separated by one or more blank lines.
 */
public final class RawTextReader implements BlockReader {
    @Override
    public List<RawBlock> read(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<RawBlock> out = new ArrayList<>();
            List<String> current = new ArrayList<>();
            int id = 1;
            String line;
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
        String chuj = lines.size() > 0 ? lines.get(0) : "";
        String gloss = lines.size() > 1 ? lines.get(1) : "";
        String translation = lines.size() > 2 ? String.join(" ", lines.subList(2, lines.size())) : "";
        return new RawBlock(id, chuj, gloss, translation);
    }
}
