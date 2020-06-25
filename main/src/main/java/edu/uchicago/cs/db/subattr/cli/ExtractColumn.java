package edu.uchicago.cs.db.subattr.cli;

import edu.uchicago.cs.db.subattr.cli.components.ColumnExtractor;
import edu.uchicago.cs.db.subattr.cli.components.PatternMiner;
import edu.uchicago.cs.db.subattr.ir.Pattern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

public class ExtractColumn {

    public static void main(String[] args) throws IOException {
        URI input = new File(args[0]).toURI();

        long start = System.currentTimeMillis();
        Pattern pattern = new PatternMiner().mine(input);
        System.out.println("Pattern Extraction Time: " + ((double) (System.currentTimeMillis() - start) / 1000));

        start = System.currentTimeMillis();
        ColumnExtractor extractor = new ColumnExtractor(pattern);
        PrintWriter tsvWriter = new PrintWriter(new FileWriter(new File(args[1])));
        PrintWriter outlierWriter = new PrintWriter(new FileWriter(new File(args[1]+".outlier")));
        extractor.setMatchConsumer((index, data) -> {
            tsvWriter.println(String.join("\t", data));
        });
        extractor.setOutlierConsumer((index, outlier) -> {
            outlierWriter.println(outlier);
        });
        extractor.extract(input);
        tsvWriter.close();
        outlierWriter.close();
        System.out.println("Columns Extraction Time: " + ((double) (System.currentTimeMillis() - start) / 1000));
    }
}
