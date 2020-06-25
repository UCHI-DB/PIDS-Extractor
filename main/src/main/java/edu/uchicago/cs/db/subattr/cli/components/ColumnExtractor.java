package edu.uchicago.cs.db.subattr.cli.components;

import com.google.common.base.CharMatcher;
import edu.uchicago.cs.db.common.functional.IntObjectConsumer;
import edu.uchicago.cs.db.subattr.ir.PSeq;
import edu.uchicago.cs.db.subattr.ir.Pattern;
import edu.uchicago.cs.db.subattr.ir.matcher.AdhocMatcherGenerator;
import edu.uchicago.cs.db.subattr.ir.matcher.AdhocMatcherGenerator2;
import edu.uchicago.cs.db.subattr.ir.matcher.ExactMatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class ColumnExtractor {

    Pattern pattern;
    ExactMatcher matcher;
    IntObjectConsumer<CharSequence[]> matchConsumer;
    IntObjectConsumer<CharSequence> outlierConsumer;

    public ColumnExtractor(Pattern pattern) {
        this.pattern = pattern;
        this.matcher = new AdhocMatcherGenerator2().generate(pattern);
    }

    public void extract(Supplier<String> supplier) {
        pattern.naming();

        if (pattern instanceof PSeq) {
            String line = null;
            int counter = 0;
            while ((line = supplier.get()) != null) {
                if(!line.trim().isEmpty()) {
                    CharSequence[] matched = matcher.match(line);
                    if (matched != null) {
                        matchConsumer.consume(counter++, matched);
                    } else {
                        outlierConsumer.consume(counter++, line);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void extract(URI inputFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(inputFile)));
            extract(() -> {
                try {
                    String line = null;
                    // Skip lines with non-ascii characters
                    while ((line = reader.readLine()) != null) {
                        if (CharMatcher.ascii().matchesAllOf(line)) {
                            break;
                        }
                    }
                    return line;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public IntObjectConsumer<CharSequence[]> getMatchConsumer() {
        return matchConsumer;
    }

    public void setMatchConsumer(IntObjectConsumer<CharSequence[]> matchConsumer) {
        this.matchConsumer = matchConsumer;
    }

    public IntObjectConsumer<CharSequence> getOutlierConsumer() {
        return outlierConsumer;
    }

    public void setOutlierConsumer(IntObjectConsumer<CharSequence> outlierConsumer) {
        this.outlierConsumer = outlierConsumer;
    }
}
