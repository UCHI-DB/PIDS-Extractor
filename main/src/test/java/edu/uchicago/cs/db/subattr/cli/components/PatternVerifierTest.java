package edu.uchicago.cs.db.subattr.cli.components;

import edu.uchicago.cs.db.subattr.extract.parser.TSymbol;
import edu.uchicago.cs.db.subattr.ir.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class PatternVerifierTest {

    @Test
    public void verify() {
//        assertFalse(new PatternVerifier().verify(PSeq.apply(new Pattern[]{
//                new PIntAny(0, 10, false),
//                new PLetterAny(0, 10)
//        })));
        assertTrue(new PatternVerifier().verify(PSeq.apply(new Pattern[]{
                new PIntAny(0, 10, false),
                new PToken(new TSymbol("-")),
                new PLetterAny(0, 10)
        })));
    }
}