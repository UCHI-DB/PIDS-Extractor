package edu.uchicago.cs.db.subattr.cli;

import edu.uchicago.cs.db.subattr.cli.components.CoverageEstimator;
import edu.uchicago.cs.db.subattr.cli.components.PatternMiner;
import edu.uchicago.cs.db.subattr.cli.components.PatternVerifier;
import edu.uchicago.cs.db.subattr.ir.PSeq;
import edu.uchicago.cs.db.subattr.ir.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class BatchPattern {

    static Logger logger = LoggerFactory.getLogger(BatchPattern.class);

    public static void main(String[] args) throws Exception {
        int sampleSize = Integer.parseInt(args[1]);

        BatchPattern core = new BatchPattern(sampleSize);

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(args[0]))) {
            stream.parallel().map(core::extract).forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int sampleSize;

    private PatternVerifier verifier;

    BatchPattern(int sampleSize) {
        this.sampleSize = sampleSize;
        this.verifier = new PatternVerifier();
    }

    protected String extract(String inputLine) {
        String[] fields = inputLine.trim().split(",");
//            if (keys.contains(fields[0])) {
        logger.info("Processing " + fields[0] + ":" + fields[1]);
        String id = fields[0];
        String filename = fields[1];
        Pattern pattern = null;
        try {
            URI fileUri = new URI(filename);
            PatternMiner miner = new PatternMiner();
            miner.sampleSize_$eq(sampleSize);
            pattern = miner.mine(fileUri);

            if (pattern != null && pattern instanceof PSeq && verifier.verify(pattern)) {
                return id + "," + pattern;
            } else {
                return id + ",NULL";
            }
        } catch (Exception e) {
            logger.error("Error Processing " + fields[0] + "," + pattern, e);
            return id + ",NULL";
        }
    }
}
