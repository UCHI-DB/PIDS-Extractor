package edu.uchicago.cs.db.subattr.cli;

import edu.uchicago.cs.db.subattr.cli.components.CatamaranExtractor;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class CompressColumnCatamaran {

    public static void main(String[] args) throws Exception {
        URI input = new File(args[1]).toURI();

        CatamaranExtractor extractor = new CatamaranExtractor(args[0]);
        extractor.extract(input);

        // Check the
    }
}
