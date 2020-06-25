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

import static edu.uchicago.cs.db.subattr.ir.matcher.TransitionBuilder.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TransitionBuilderTest {

    @Test
    public void testBuild() {
        TransitionBuilder builder = new TransitionBuilder();

        Pattern seq = PSeq.apply(new Pattern[]{
                new PIntAny(3, 5, false),
                new PToken(new TSymbol("-/-")),
                new PIntAny(2, 4, false)});
//        patterns.add(new PToken(new TSymbol("==")));
//        patterns.add(new PIntAny(3, 5, false));

        builder.on(seq);

        assertEquals(13, builder.transitions.size());

        assertEquals(4, builder.groupGuideMap.size());
        assertEquals(1, builder.groupGuideMap.get(1));
        assertEquals(2, builder.groupGuideMap.get(6));
        assertEquals(1, builder.groupGuideMap.get(9));
        assertEquals(6, builder.groupGuideMap.get(13));

        assertEquals(1, builder.transitions.get(0).size());
        assertEquals(1, builder.transitions.get(1).size());
        assertEquals(1, builder.transitions.get(2).size());
        assertEquals(2, builder.transitions.get(3).size());
        assertEquals(2, builder.transitions.get(4).size());
        assertEquals(1, builder.transitions.get(5).size());
        assertEquals(1, builder.transitions.get(6).size());
        assertEquals(1, builder.transitions.get(7).size());
        assertEquals(1, builder.transitions.get(8).size());
        assertEquals(1, builder.transitions.get(9).size());
        assertEquals(2, builder.transitions.get(10).size());
        assertEquals(2, builder.transitions.get(11).size());
        assertEquals(1, builder.transitions.get(12).size());

        assertEquals(1, builder.transitions.get(0).get(CHAR_NUM));
        assertEquals(2, builder.transitions.get(1).get(CHAR_NUM));
        assertEquals((3), builder.transitions.get(2).get(CHAR_NUM));
        assertEquals((4), builder.transitions.get(3).get(CHAR_NUM));
        assertEquals((6), builder.transitions.get(3).get((int) '-'));
        assertEquals((5), builder.transitions.get(4).get(CHAR_NUM));
        assertEquals((6), builder.transitions.get(4).get((int) '-'));
        assertEquals((6), builder.transitions.get(5).get((int) '-'));
        assertEquals((7), builder.transitions.get(6).get((int) '/'));
        assertEquals((8), builder.transitions.get(7).get((int) '-'));
        assertEquals((9), builder.transitions.get(8).get(CHAR_NUM));
        assertEquals((10), builder.transitions.get(9).get(CHAR_NUM));
        assertEquals((11), builder.transitions.get(10).get(CHAR_NUM));
        assertEquals((12), builder.transitions.get(11).get(CHAR_NUM));
        assertEquals((13), builder.transitions.get(12).get(EOI));


        assertEquals(13, builder.endState);
    }

    @Test
    public void testAny() {
        Pattern manyany = PSeq.apply(new Pattern[]{
                new PLetterAny(0, -1),
                new PToken(new TSymbol(":")),
                new PIntAny(1, -1, false),
                new PToken(new TSymbol(":")),
                new PLabelAny(1, 1),
                new PToken(new TSymbol(":")),
                new PWordAny(2, -1),
        });

        TransitionBuilder builder = new TransitionBuilder();
        builder.on(manyany);

        assertEquals(10, builder.endState);
        assertEquals(10, builder.transitions.size());

        int[][] transitions = new int[][]{
                new int[]{CHAR_LETTER, 1, CHAR_EMPTY, 2},
                new int[]{CHAR_LETTER, 1, ':', 3},
                new int[]{':', 3},
                new int[]{CHAR_NUM, 4},
                new int[]{CHAR_NUM, 4, ':', 5},
                new int[]{CHAR_LABEL, 6},
                new int[]{':', 7},
                new int[]{CHAR_WORD, 8},
                new int[]{CHAR_WORD, 9},
                new int[]{CHAR_WORD, 9, EOI, 10}
        };

        for (int i = 0; i < transitions.length; i++) {
            int[] trans = transitions[i];
            assertEquals(trans.length / 2, builder.transitions.get(i).size());
            for (int j = 0; j < trans.length; j += 2) {
                assertEquals(trans[j + 1], builder.transitions.get(i).get(trans[j]));
            }
        }
    }

    @Test
    public void testBuild2() {
        TransitionBuilder builder = new TransitionBuilder();

        Pattern seq = PSeq.apply(new Pattern[]{
                new PIntAny(3, 5, false),
                new PToken(new TSymbol("-")),
                new PIntAny(2, 4, false)});

        builder.on(seq);

        assertEquals(11, builder.transitions.size());

        assertEquals(4, builder.groupGuideMap.size());
        assertEquals((1), builder.groupGuideMap.get(1));
        assertEquals((2), builder.groupGuideMap.get(6));
        assertEquals((1), builder.groupGuideMap.get(7));
        assertEquals((6), builder.groupGuideMap.get(11));
    }


}
