package edu.uchicago.cs.db.subattr.datagen;

import org.junit.Test;

import static org.junit.Assert.*;

public class GenAddressTest {

    @Test
    public void generate() throws Exception {
        GenAddress ga = new GenAddress();
        assertEquals(8, ga.generate().split(",").length);
    }
}