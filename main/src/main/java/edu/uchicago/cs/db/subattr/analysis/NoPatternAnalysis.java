package edu.uchicago.cs.db.subattr.analysis;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class NoPatternAnalysis {

    public static void main(String[] args) throws Exception {
        Map<Object, Long> counted = Files.lines(Paths.get(args[0]))
                .map(l -> analysis(l.split(","))).collect(Collectors.groupingBy(p -> p, Collectors.counting()));
        counted.forEach((key, count) -> System.out.println(key + "," + count));
    }

    protected static String analysis(String[] filedata) {
        String id = filedata[0];
        URI file = new File(filedata[2].substring(7)).toURI();

        try {
            double avgSymbol = Files.lines(Paths.get(file)).filter(StringUtils::isNotBlank).limit(500)
                    .mapToDouble(line -> line.length() - line.toString().chars().filter(Character::isAlphabetic).count())
                    .average().getAsDouble();
            System.out.println(id + "," + avgSymbol);
            if (avgSymbol == 0) {
                return "word";
            }
            return "other";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
