package org.titiplex.io;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.titiplex.model.RawBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class DocxReader implements BlockReader {

    @Override
    public List<RawBlock> read(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<RawBlock> blocks = new ArrayList<>();
            List<String> current = new ArrayList<>();
            boolean inBlock = false;
            int id = 1;

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                text = text == null ? "" : text.trim();

                if (inBlock && text.isEmpty()) {
                    RawBlock block = flushBlock(id++, current);
                    if (block != null) {
                        blocks.add(block);
                    }
                    current.clear();
                    inBlock = false;
                    continue;
                }

                if (!inBlock && !text.isEmpty() && Character.isDigit(text.charAt(0))) {
                    inBlock = true;
                }

                if (inBlock) {
                    current.add(text);
                }
            }

            if (inBlock && !current.isEmpty()) {
                RawBlock block = flushBlock(id, current);
                if (block != null) {
                    blocks.add(block);
                }
            }

            return blocks;
        }
    }

    private RawBlock flushBlock(int id, List<String> lines) {
        if (lines.isEmpty()) {
            return null;
        }

        String translation = lines.getLast().trim();

        StringBuilder chuj = new StringBuilder();
        StringBuilder gloss = new StringBuilder();

        for (int i = 0; i < lines.size() - 1; i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            if (i % 2 == 0) {
                if (!chuj.isEmpty()) chuj.append(' ');
                chuj.append(line);
            } else {
                if (!gloss.isEmpty()) gloss.append(' ');
                gloss.append(line);
            }
        }

        return new RawBlock(id, chuj.toString(), gloss.toString(), translation);
    }
}