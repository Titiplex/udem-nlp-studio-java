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
            List<String> nonBlankLines = new ArrayList<>();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    nonBlankLines.add(text.trim());
                }
            }

            List<RawBlock> blocks = new ArrayList<>();
            int id = 1;
            for (int i = 0; i < nonBlankLines.size(); ) {
                String chuj = nonBlankLines.get(i++);
                String gloss = i < nonBlankLines.size() ? nonBlankLines.get(i++) : "";
                String translation = i < nonBlankLines.size() ? nonBlankLines.get(i++) : "";

                // absorb extra translation continuation line if the next line clearly is not a gloss
                if (i < nonBlankLines.size() && !looksLikeGlossLine(nonBlankLines.get(i)) && !looksLikeChujLine(nonBlankLines.get(i))) {
                    translation = translation + " " + nonBlankLines.get(i++);
                }
                blocks.add(new RawBlock(id++, chuj, gloss, translation.trim()));
            }
            return blocks;
        }
    }

    private boolean looksLikeGlossLine(String line) {
        if (line == null || line.isBlank()) return false;
        return line.contains("-") || line.matches(".*\\b(A[123]|B[123]|PL|SG|PFV|IPFV|PROG|VT|VI)\\b.*");
    }

    private boolean looksLikeChujLine(String line) {
        if (line == null || line.isBlank()) return false;
        return !looksLikeGlossLine(line) && line.matches(".*[a-zA-Záéíóúàèìòù'’].*");
    }
}
