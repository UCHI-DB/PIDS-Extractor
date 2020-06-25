/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License,
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 */

package edu.uchicago.cs.db.subattr.ir.matcher;

import edu.uchicago.cs.db.subattr.extract.parser.TSymbol;
import edu.uchicago.cs.db.subattr.ir.*;
import org.junit.Test;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AdhocMatcherGenerator2Test {

    @Test
    public void testGenerate() {
        List<Pattern> content = new ArrayList<>();
        content.add(new PIntAny(3, 4, false));
        content.add(new PToken(new TSymbol("-")));
        content.add(new PIntAny(4, 4, false));
        Pattern pattern = new PSeq(JavaConverters.asScalaBuffer(content));

        ExactMatcher matcher = new AdhocMatcherGenerator2().generate(pattern);

        CharSequence[] matched = matcher.match("234-4111");
        assertFalse(matched == null);
        assertEquals(2, matched.length);
        assertEquals("234", matched[0].toString());
        assertEquals("4111", matched[1].toString());

        matched = matcher.match("234-411");
        assertNull(matched);
    }

    @Test
    public void testAddress() {
        List<Pattern> content = new ArrayList<>();
        content.add(new PIntAny(0, 3, false));
        content.add(new PToken(new TSymbol("|")));
        content.add(new PWordAny(0, 21));
        content.add(new PToken(new TSymbol("|")));
        content.add(new PWordAny(0, 9));
        content.add(new PToken(new TSymbol("|Suite ")));
        content.add(new PWordAny(0, 3));
        content.add(new PToken(new TSymbol("|")));
        content.add(new PWordAny(0, 20));
        content.add(new PToken(new TSymbol("|")));
        content.add(new PWordAny(0, 30));
        content.add(new PToken(new TSymbol("|")));
        content.add(new PWordAny(0, 2));
        content.add(new PToken(new TSymbol("|")));
        content.add(new PIntAny(0, 5, false));
        Pattern pattern = new PSeq(JavaConverters.asScalaBuffer(content));

        ExactMatcher matcher = new AdhocMatcherGenerator2().generate(pattern);

        String operand = "460|Maple Spruce|Court|Suite 480|Somerville|Potter County|SD|57783";
        CharSequence[] matched = matcher.match(operand);
        assertEquals(8, matched.length);
        assertEquals("460", matched[0].toString());
        assertEquals("Maple Spruce", matched[1].toString());
        assertEquals("Court", matched[2].toString());
        assertEquals("480", matched[3].toString());
        assertEquals("Somerville", matched[4].toString());
        assertEquals("Potter County", matched[5].toString());
        assertEquals("SD", matched[6].toString());
        assertEquals("57783", matched[7].toString());
        String operand2 = "|Sunset Pine||Suite ||Dickson County|TN|";

        matched = matcher.match(operand2);
        assertEquals(8, matched.length);
        assertEquals("", matched[0].toString());
        assertEquals("Sunset Pine", matched[1].toString());
        assertEquals("", matched[2].toString());
        assertEquals("", matched[3].toString());
        assertEquals("", matched[4].toString());
        assertEquals("Dickson County", matched[5].toString());
        assertEquals("TN", matched[6].toString());
        assertEquals("", matched[7].toString());

        return;
    }

    @Test
    public void testVariousAny() {
        List<Pattern> content = new ArrayList<>();
        content.add(new PIntAny(0, 3, false));
        content.add(new PToken(new TSymbol("|")));
        content.add(new PLetterAny(1, 5));
        content.add(new PToken(new TSymbol("|")));
        content.add(new PLabelAny(2, 4));
        Pattern pattern = new PSeq(JavaConverters.asScalaBuffer(content));

        ExactMatcher matcher = new AdhocMatcherGenerator2().generate(pattern);

        CharSequence[] matched = matcher.match("323|ABBA|322");
        assertEquals(3, matched.length);
        assertEquals("323",matched[0].toString());
        assertEquals("ABBA",matched[1].toString());
        assertEquals("322",matched[2].toString());

        matched = matcher.match("323|Ab|55A");
        assertEquals(3, matched.length);
        assertEquals("323",matched[0].toString());
        assertEquals("Ab",matched[1].toString());
        assertEquals("55A",matched[2].toString());

        assertNull(matcher.match("32A|ABBA|55A"));
        assertNull(matcher.match("323||55A"));
        assertNull(matcher.match("323|AB|55A42"));
    }
}
