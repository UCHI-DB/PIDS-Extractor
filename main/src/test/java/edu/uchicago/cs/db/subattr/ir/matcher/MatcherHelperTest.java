package edu.uchicago.cs.db.subattr.ir.matcher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatcherHelperTest {

    @Test
    public void testEqualString() {
        String a = "!df4252x4e2";
        assertEquals(4, MatcherHelper.equalString("4252", a, 3));
        assertEquals(4, MatcherHelper.equalString("4252aac", a, 3));
        assertEquals(3, MatcherHelper.equalString("42517", a, 3));
    }
}
