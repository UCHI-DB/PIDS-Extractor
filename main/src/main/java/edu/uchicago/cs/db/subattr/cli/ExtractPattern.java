package edu.uchicago.cs.db.subattr.cli;

import edu.uchicago.cs.db.subattr.cli.components.PatternMiner;
import edu.uchicago.cs.db.subattr.ir.Pattern;

import java.io.File;
import java.net.URI;

public class ExtractPattern {

    public static void main(String[] args) {

//        int repetition = 10;

        URI input = new File(args[0]).toURI();
        long start = System.currentTimeMillis();
//        for (int i = 0; i < repetition; i++) {
            PatternMiner miner = new PatternMiner();
            miner.sampleSize_$eq(2000);
            Pattern pattern = miner.mine(input);
            System.out.println(pattern);
//        }
//        System.out.println("Pattern Extraction Time: " + ((double) (System.currentTimeMillis() - start) / 1000 / repetition));

    }
}
