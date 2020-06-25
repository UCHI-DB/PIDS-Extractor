package edu.uchicago.cs.db.subattr.cli;

import edu.uchicago.cs.db.subattr.cli.components.ColumnExtractor;
import edu.uchicago.cs.db.subattr.cli.components.CoverageEstimator;
import edu.uchicago.cs.db.subattr.cli.components.FileFilter;
import edu.uchicago.cs.db.subattr.cli.components.PatternMiner;
import edu.uchicago.cs.db.subattr.cli.components.PatternVerifier;
import edu.uchicago.cs.db.subattr.ir.PSeq;
import edu.uchicago.cs.db.subattr.ir.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class BatchPatternWithCoverage {

    static Logger logger = LoggerFactory.getLogger(BatchPatternWithCoverage.class);

    public static void main(String[] args) throws Exception {
        int sampleSize = Integer.parseInt(args[1]);

        long seed = args.length >= 3 ? Long.parseLong(args[2]) : System.currentTimeMillis();
        System.out.println(seed);
        BatchPatternWithCoverage core = new BatchPatternWithCoverage(sampleSize, seed);

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(args[0]))) {
            stream.map(line -> line.trim().split(","))
                    .parallel().map(core::extract).forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int sampleSize;

    private PatternVerifier verifier;

    private long seed;

    BatchPatternWithCoverage(int sampleSize, long seed) {
        this.sampleSize = sampleSize;
        this.seed = seed;
        this.verifier = new PatternVerifier();
    }


    protected String extract(String[] fields) {
//            if (keys.contains(fields[0])) {
        logger.info("Processing " + fields[0] + ":" + fields[1]);
        String id = fields[0];
        String filename = fields[1];
        Pattern pattern = null;
        try {
            URI fileUri = new URI(filename);
            PatternMiner miner = new PatternMiner();
            miner.seed_$eq(seed);
            miner.sampleSize_$eq(sampleSize);
            pattern = miner.mine(fileUri);

            if (pattern != null && pattern instanceof PSeq && verifier.verify(pattern)) {
                logger.info("Processing " + fields[0] + ":" + pattern.toString());
                double coverage = new CoverageEstimator(pattern).estimate(filename);
                return id + "," + coverage + "," + pattern;
            } else {
                return id + ",NULL,NULL";
            }
        } catch (Exception e) {
            logger.error("Error Processing " + fields[0] + "," + pattern, e);
            return id + ",NULL,NULL";
        }
    }
}
