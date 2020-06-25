package edu.uchicago.cs.db.subattr.nlp;


import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import scala.Char;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Dictionary of natural languages, read from a text file
 */
public class NaturalLangDict implements Predicate<String> {

    Set<String> dictionary = new HashSet<>();
    IntSet symbols = new IntOpenHashSet();

    public NaturalLangDict(String fileName) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String value = line.trim().toLowerCase();
                if (value.length() > 1) // Single character word is distracting, do not include them for now
                    dictionary.add(value);
                value.chars().forEach(symbols::add);
            }
            reader.close();
            symbols.add(' ');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validSymbols(String input) {
        return input.toLowerCase().chars().allMatch(symbols::contains);
    }

    public boolean test(String word) {
        return word.length() > 1 && dictionary.contains(word.toLowerCase());
    }

    public static final NaturalLangDict GLOVE = new NaturalLangDict("glove_dict.txt");
}
