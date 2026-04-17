package org.titiplex.io;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.titiplex.model.AlignedToken;
import org.titiplex.model.CorrectionEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CorrectedDocxWriter {
    private static final int MAX_CHARS_PER_LINE = 60;
    private static final BaseStyle DEFAULT_STYLE = new BaseStyle("Times New Roman", 12);

    // Approximation raisonnable pour Word : 1 caractère ~= 120 twips en Times 12
    private static final int TWIPS_PER_CHAR = 120;
    private static final int GAP_TWIPS = 220;
    private static final int NUMBER_ZONE_TWIPS = 520;

    public void write(Path sourcePath, Path output, List<CorrectionEntry> entries) throws IOException {
        SourceLayout layout = detectSourceLayout(sourcePath);

        try (XWPFDocument document = new XWPFDocument()) {
            for (int i = 0; i < entries.size(); i++) {
                appendEntry(document, entries.get(i), layout);

                if (i < entries.size() - 1) {
                    XWPFParagraph spacer = document.createParagraph();
                    spacer.setSpacingBefore(0);
                    spacer.setSpacingAfter(0);
                }
            }

            try (OutputStream out = Files.newOutputStream(output)) {
                document.write(out);
            }
        }
    }

    private void appendEntry(XWPFDocument document, CorrectionEntry entry, SourceLayout layout) {
        List<TokenPair> pairs = toPairs(entry.corrected().alignedTokens());
        List<List<TokenPair>> chunks = wrapPairs(pairs);

        if (chunks.isEmpty()) {
            chunks = List.of(List.of());
        }

        for (int i = 0; i < chunks.size(); i++) {
            List<TokenPair> chunk = chunks.get(i);
            List<Integer> tabStops = computeTabStops(chunk);

            XWPFParagraph chujParagraph = document.createParagraph();
            applyParagraphStyle(chujParagraph, layout.exampleParagraphStyle());
            applyTabStops(chujParagraph, tabStops);
            writeTabbedLine(
                    chujParagraph,
                    i == 0 ? entry.id() + "." : "",
                    chunk.stream().map(TokenPair::chuj).toList(),
                    layout.baseStyle()
            );

            XWPFParagraph glossParagraph = document.createParagraph();
            applyParagraphStyle(glossParagraph, layout.glossParagraphStyle());
            applyTabStops(glossParagraph, tabStops);
            writeTabbedLine(
                    glossParagraph,
                    "",
                    chunk.stream().map(TokenPair::gloss).toList(),
                    layout.baseStyle()
            );
        }

        if (entry.corrected().translation() != null && !entry.corrected().translation().isBlank()) {
            XWPFParagraph translationParagraph = document.createParagraph();
            applyParagraphStyle(translationParagraph, layout.translationParagraphStyle());
            writeTranslationLine(translationParagraph, entry.corrected().translation(), layout.baseStyle());
        }
    }

    private List<Integer> computeTabStops(List<TokenPair> chunk) {
        List<Integer> stops = new ArrayList<>();
        int current = NUMBER_ZONE_TWIPS;

        for (TokenPair pair : chunk) {
            stops.add(current);
            int widthChars = Math.max(1, Math.max(pair.chuj().length(), pair.gloss().length()));
            current += (widthChars * TWIPS_PER_CHAR) + GAP_TWIPS;
        }

        return stops;
    }

    private void applyTabStops(XWPFParagraph paragraph, List<Integer> stops) {
        CTPPr pPr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();

        if (pPr.isSetTabs()) {
            pPr.unsetTabs();
        }

        CTTabs tabs = pPr.addNewTabs();
        for (Integer pos : stops) {
            CTTabStop tab = tabs.addNewTab();
            tab.setVal(STTabJc.LEFT);
            tab.setLeader(STTabTlc.NONE);
            tab.setPos(pos);
        }
    }

    private void writeTabbedLine(XWPFParagraph paragraph, String firstCell, List<String> cells, BaseStyle style) {
        XWPFRun run = paragraph.createRun();
        run.setBold(false);
        run.setItalic(false);
        run.setFontFamily(style.fontFamily());
        run.setFontSize(style.fontSize());

        if (firstCell != null && !firstCell.isEmpty()) {
            run.setText(firstCell);
        }

        for (String cell : cells) {
            run.addTab();
            run.setText(cell == null ? "" : cell);
        }
    }

    private void writeTranslationLine(XWPFParagraph paragraph, String text, BaseStyle style) {
        XWPFRun run = paragraph.createRun();
        run.setBold(false);
        run.setItalic(false);
        run.setFontFamily(style.fontFamily());
        run.setFontSize(style.fontSize());
        run.addTab();
        run.addTab();
        run.addTab();
        run.setText("'" + text + "'");
    }

    private SourceLayout detectSourceLayout(Path sourcePath) {
        if (sourcePath == null || !Files.exists(sourcePath) || !sourcePath.toString().toLowerCase().endsWith(".docx")) {
            return new SourceLayout(DEFAULT_STYLE, null, null, null);
        }

        try (XWPFDocument source = new XWPFDocument(Files.newInputStream(sourcePath))) {
            BaseStyle style = detectBaseStyle(source);

            List<XWPFParagraph> nonBlankParagraphs = source.getParagraphs().stream()
                    .filter(p -> p.getText() != null && !p.getText().isBlank())
                    .toList();

            CTPPr example = !nonBlankParagraphs.isEmpty() && nonBlankParagraphs.get(0).getCTP().isSetPPr()
                    ? (CTPPr) nonBlankParagraphs.get(0).getCTP().getPPr().copy()
                    : null;

            CTPPr gloss = nonBlankParagraphs.size() > 1 && nonBlankParagraphs.get(1).getCTP().isSetPPr()
                    ? (CTPPr) nonBlankParagraphs.get(1).getCTP().getPPr().copy()
                    : example;

            CTPPr translation = nonBlankParagraphs.size() > 2 && nonBlankParagraphs.get(2).getCTP().isSetPPr()
                    ? (CTPPr) nonBlankParagraphs.get(2).getCTP().getPPr().copy()
                    : gloss;

            return new SourceLayout(style, example, gloss, translation);
        } catch (Exception ignored) {
            return new SourceLayout(DEFAULT_STYLE, null, null, null);
        }
    }

    private BaseStyle detectBaseStyle(XWPFDocument source) {
        for (XWPFParagraph paragraph : source.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.text();
                if (text == null || text.isBlank()) {
                    continue;
                }

                String fontFamily = run.getFontFamily();
                int fontSize = run.getFontSize();

                return new BaseStyle(
                        fontFamily == null || fontFamily.isBlank() ? DEFAULT_STYLE.fontFamily() : fontFamily,
                        fontSize <= 0 ? DEFAULT_STYLE.fontSize() : fontSize
                );
            }
        }
        return DEFAULT_STYLE;
    }

    private void applyParagraphStyle(XWPFParagraph paragraph, CTPPr template) {
        if (template != null) {
            paragraph.getCTP().setPPr((CTPPr) template.copy());
        } else {
            paragraph.setSpacingBefore(0);
            paragraph.setSpacingAfter(0);
        }
    }

    private List<TokenPair> toPairs(List<AlignedToken> alignedTokens) {
        List<TokenPair> out = new ArrayList<>();
        for (AlignedToken token : alignedTokens) {
            out.add(new TokenPair(
                    safe(token.chujSurface()),
                    safe(token.glossSurface())
            ));
        }
        return out;
    }

    private List<List<TokenPair>> wrapPairs(List<TokenPair> pairs) {
        List<List<TokenPair>> out = new ArrayList<>();
        if (pairs.isEmpty()) {
            return out;
        }

        int i = 0;
        while (i < pairs.size()) {
            List<TokenPair> chunk = new ArrayList<>();
            int width = 0;

            while (i < pairs.size()) {
                TokenPair pair = pairs.get(i);
                int colWidth = Math.max(pair.chuj().length(), pair.gloss().length()) + 2;

                if (width > 0 && width + colWidth > MAX_CHARS_PER_LINE) {
                    break;
                }

                chunk.add(pair);
                width += colWidth;
                i++;
            }

            out.add(chunk);
        }

        return out;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record BaseStyle(String fontFamily, int fontSize) {
    }

    private record TokenPair(String chuj, String gloss) {
    }

    private record SourceLayout(
            BaseStyle baseStyle,
            CTPPr exampleParagraphStyle,
            CTPPr glossParagraphStyle,
            CTPPr translationParagraphStyle
    ) {
    }
}