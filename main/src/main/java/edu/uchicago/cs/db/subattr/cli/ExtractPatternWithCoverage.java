package edu.uchicago.cs.db.subattr.cli;

import edu.uchicago.cs.db.subattr.cli.components.CoverageEstimator;
import edu.uchicago.cs.db.subattr.cli.components.PatternMiner;
import edu.uchicago.cs.db.subattr.cli.components.PatternVerifier;
import edu.uchicago.cs.db.subattr.ir.PSeq;
import edu.uchicago.cs.db.subattr.ir.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class ExtractPatternWithCoverage {

    static Logger logger = LoggerFactory.getLogger(ExtractPatternWithCoverage.class);

    public static void main(String[] args) throws Exception {
        int sampleSize = Integer.parseInt(args[1]);

        PatternVerifier verifier = new PatternVerifier();

        Pattern pattern = null;
        try {
            URI fileUri = new URI(args[0]);
            PatternMiner miner = new PatternMiner();
            miner.sampleSize_$eq(sampleSize);
            pattern = miner.mine(fileUri);

            if (pattern != null && pattern instanceof PSeq && verifier.verify(pattern)) {
                System.out.println(pattern);
                CoverageEstimator estimator = new CoverageEstimator(pattern);
                System.out.println(estimator.estimate(args[0]));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
