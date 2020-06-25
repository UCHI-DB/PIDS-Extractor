package edu.uchicago.cs.db.subattr.cli.components;

import it.unimi.dsi.fastutil.ints.IntSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class FileFilter {

    public static Set<String> makeFilter(String fileName) {
        try {
            return Files.lines(Paths.get(fileName)).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
