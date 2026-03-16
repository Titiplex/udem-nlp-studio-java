package org.titiplex.cli;

import org.titiplex.align.TokenAligner;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.AnnotationConfigLoader;
import org.titiplex.io.*;
import org.titiplex.pipeline.ConlluPipeline;
import org.titiplex.pipeline.CorrectionPipeline;
import org.titiplex.rules.RuleEngine;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: java ... Main <input.docx|input.txt> <correction.yaml> <pos.yaml> <output.conllu>");
            System.exit(1);
        }

        Path inputPath = Path.of(args[0]);
        Path rulesYaml = Path.of(args[1]);
        Path posYaml = Path.of(args[2]);
        Path outputConllu = Path.of(args[3]);

        BlockReader reader = inputPath.toString().toLowerCase().endsWith(".docx") ? new DocxReader() : new RawTextReader();
        YamlRuleLoader ruleLoader = new YamlRuleLoader();
        RuleEngine ruleEngine;
        try (FileInputStream ruleIn = new FileInputStream(rulesYaml.toFile())) {
            ruleEngine = new RuleEngine(ruleLoader.load(ruleIn));
        }

        AnnotationConfig annotationConfig = new AnnotationConfigLoader().load(posYaml);

        CorrectionPipeline correctionPipeline = new CorrectionPipeline(new TokenAligner(), ruleEngine);
        ConlluPipeline conlluPipeline = new ConlluPipeline(annotationConfig);

        StringBuilder sb = new StringBuilder();
        try (FileInputStream in = new FileInputStream(inputPath.toFile())) {
            for (var rawBlock : reader.read(in)) {
                var corrected = correctionPipeline.process(rawBlock);
                sb.append(conlluPipeline.toEntry(corrected).toConlluString());
            }
        }

        new ConlluWriter().writeRaw(outputConllu, sb.toString());
    }
}
