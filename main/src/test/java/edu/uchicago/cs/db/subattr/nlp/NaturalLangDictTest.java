package edu.uchicago.cs.db.subattr.nlp;

import org.junit.Test;

import static org.junit.Assert.*;

public class NaturalLangDictTest {

    @Test
    public void testWord() {
        assertTrue(NaturalLangDict.GLOVE.test("street"));
        assertTrue(NaturalLangDict.GLOVE.test("road"));
        assertTrue(NaturalLangDict.GLOVE.test("greenwood"));
        assertTrue(NaturalLangDict.GLOVE.test("magnet"));
        assertTrue(NaturalLangDict.GLOVE.test("rd."));
    }

    @Test
    public void testSymbol() {
        assertTrue(NaturalLangDict.GLOVE.validSymbols("Park"));
        assertTrue(NaturalLangDict.GLOVE.validSymbols("park"));
    }
}