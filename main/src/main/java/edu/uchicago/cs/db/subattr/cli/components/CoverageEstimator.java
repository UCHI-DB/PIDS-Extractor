package edu.uchicago.cs.db.subattr.cli.components;

import edu.uchicago.cs.db.subattr.ir.Pattern;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class CoverageEstimator {

    private Pattern pattern;

    public CoverageEstimator(Pattern pattern) {
        this.pattern = pattern;
    }

    public double estimate(String filename) {
        try {
            URI fileUri = new URI(filename);

            ColumnExtractor extractor = new ColumnExtractor(pattern);

            AtomicInteger counter = new AtomicInteger(0);
            AtomicInteger valid = new AtomicInteger(0);
            extractor.setMatchConsumer((index, data) -> {
                counter.incrementAndGet();
                valid.incrementAndGet();
            });
            extractor.setOutlierConsumer((index, outlier) -> {
                counter.incrementAndGet();
            });
            extractor.extract(fileUri);

            return valid.doubleValue() / counter.doubleValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
