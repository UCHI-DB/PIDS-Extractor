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

import com.google.common.collect.ImmutableMap;
import edu.uchicago.cs.db.subattr.extract.parser.TInt;
import edu.uchicago.cs.db.subattr.extract.parser.TSymbol;
import edu.uchicago.cs.db.subattr.extract.parser.TWord;
import edu.uchicago.cs.db.subattr.ir.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.uchicago.cs.db.subattr.ir.matcher.TransitionBuilder.*;
import static org.junit.Assert.*;

public class TransitionBuilder2Test {

    @Test
    public void testBuildList() {
        Pattern complex = PSeq.apply(new Pattern[]{
                new PIntAny(3, 5, false),
                new PToken(new TSymbol("-")),
                PUnion.apply(new Pattern[]{
                        PEmpty$.MODULE$,
                        PSeq.apply(new Pattern[]{
                                new PIntAny(4, 6, false),
                                new PToken(new TSymbol("~"))
                        })
                }),
                new PToken(new TSymbol("/")),
                new PIntAny(3, 3, false)
        });

        complex.visit(new NamingVisitor());
        Map<String, Integer> groups = new HashMap<>();
        groups.put("_0_0", 0);
        groups.put("_0_2", 1);
        groups.put("_0_4", 2);

        TransitionBuilder2.TSection section = new TransitionBuilder2().buildList(complex, groups);
        assertEquals(2, section.groupCount);


        List<TransitionBuilder2.TNode> head = section.head;
        assertEquals(1, head.size());

        TransitionBuilder2.TNode current = head.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertTrue(current.beginGroup == 0);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(2, current.nexts.size());

        TransitionBuilder2.TNode current1 = current.nexts.get(1);
        assertEquals('-', current1.beginChar);

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(2, current.nexts.size());

        current1 = current.nexts.get(1);
        assertEquals('-', current1.beginChar);

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals('-', current.beginChar);
        assertEquals(0, current.endGroup);
        assertEquals(2, current.nexts.size());

        current1 = current.nexts.get(0);
        assertEquals(CHAR_EMPTY, current1.beginChar);
        assertTrue(current1.beginGroup == 1);
        assertEquals(1, current1.nexts.size());

        current1 = current1.nexts.get(0);
        assertEquals('/', current1.beginChar);
        assertEquals(1, current1.endGroup);
        assertEquals(1, current1.nexts.size());

        current = current.nexts.get(1);
        assertTrue(current.beginGroup == 1);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(2, current.nexts.size());

        current1 = current.nexts.get(1);
        assertEquals('~', current1.beginChar);
        assertEquals(1, current1.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(2, current.nexts.size());

        current1 = current.nexts.get(1);
        assertEquals('~', current1.beginChar);
        assertEquals(1, current1.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals('~', current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals('/', current.beginChar);
        assertEquals(1, current.endGroup);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertTrue(current.beginGroup == 2);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(0, current.nexts.size());
    }

    @Test
    public void testInfiniteLoop() {
        Pattern complex = PSeq.apply(new Pattern[]{
                new PIntAny(3, -1, false),
                new PToken(new TSymbol("-")),
                new PIntAny(1, -1, false)});
        complex.visit(new NamingVisitor());
        Map<String, Integer> groups = new HashMap<>();
        groups.put("_0_0", 0);
        groups.put("_0_2", 1);

        TransitionBuilder2.TSection section = new TransitionBuilder2().buildList(complex, groups);
        assertEquals(1, section.groupCount);

        List<TransitionBuilder2.TNode> head = section.head;
        assertEquals(1, head.size());

        TransitionBuilder2.TNode current = head.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertTrue(current.beginGroup == 0);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertTrue(current.beginGroup == -1);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertTrue(current.beginGroup == -1);
        assertEquals(2, current.nexts.size());

        TransitionBuilder2.TNode current1 = current.nexts.get(0);
        assertEquals(current1, current);

        current = current.nexts.get(1);
        assertEquals('-', current.beginChar);
        assertEquals(-1, current.beginGroup);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.beginGroup);
        assertEquals(1, current.nexts.size());

        assertEquals(current.nexts.get(0), current);
    }

    @Test
    public void testSupportOfAny() {
        Pattern manyany = PSeq.apply(new Pattern[]{
                new PLetterAny(0, -1),
                new PToken(new TSymbol(":")),
                new PIntAny(2, -1, false),
                new PToken(new TSymbol(":")),
                new PLabelAny(1, 2),
                new PToken(new TSymbol(":")),
                new PWordAny(1, -1)
        });
        manyany.visit(new NamingVisitor());
        Map<String, Integer> groups = new HashMap<>();
        groups.put("_0_0", 0);
        groups.put("_0_2", 1);
        groups.put("_0_4", 2);
        groups.put("_0_6", 3);
        groups.put("_0_8", 4);

        TransitionBuilder2.TSection section = new TransitionBuilder2().buildList(manyany, groups);
        assertEquals(3, section.groupCount);

        assertEquals(2, section.head.size());
        TransitionBuilder2.TNode current = section.head.get(0);
        assertEquals(CHAR_LETTER, current.beginChar);
        assertEquals(2, current.nexts.size());
        assertEquals(current, current.nexts.get(0));

        TransitionBuilder2.TNode other = section.head.get(1);
        assertEquals(CHAR_EMPTY, other.beginChar);
        assertEquals(1, other.nexts.size());
        assertEquals(other.nexts.get(0), current.nexts.get(1));

        current = current.nexts.get(1);
        assertEquals((int) ':', current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(2, current.nexts.size());
        assertEquals(current, current.nexts.get(0));

        current = current.nexts.get(1);
        assertEquals((int) ':', current.beginChar);
        assertEquals(1, current.nexts.size());

        current = current.nexts.get(0);
        assertEquals(CHAR_LABEL, current.beginChar);
        assertEquals(2, current.nexts.size());

        other = current.nexts.get(1);
        current = current.nexts.get(0);

        assertEquals(CHAR_LABEL, current.beginChar);
        assertEquals((int) ':', other.beginChar);
        assertEquals(1, current.nexts.size());
        assertEquals(current.nexts.get(0), other);

        current = other;
        assertEquals(1, current.nexts.size());
        current = current.nexts.get(0);
        assertEquals(CHAR_WORD, current.beginChar);
        assertEquals(1, current.nexts.size());
        assertEquals(current, current.nexts.get(0));
    }


    @Test
    public void testZeroOrInf() {
        Pattern complex = PSeq.apply(new Pattern[]{
                new PIntAny(0, -1, false),
                new PToken(new TSymbol("-")),
                new PIntAny(1, -1, false)});
        complex.visit(new NamingVisitor());
        Map<String, Integer> groups = new HashMap<>();
        groups.put("_0_0", 0);
        groups.put("_0_2", 1);

        TransitionBuilder2.TSection section = new TransitionBuilder2().buildList(complex, groups);
        assertEquals(1, section.groupCount);

        List<TransitionBuilder2.TNode> head = section.head;
        assertEquals(2, head.size());

        TransitionBuilder2.TNode current = head.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertTrue(current.beginGroup == 0);
        assertEquals(2, current.nexts.size());
        assertEquals(current, current.nexts.get(0));

        TransitionBuilder2.TNode other = head.get(1);
        assertEquals(CHAR_EMPTY, other.beginChar);
        assertTrue(other.beginGroup == 0);
        assertEquals(1, other.nexts.size());

        current = current.nexts.get(1);
        assertEquals('-', current.beginChar);
        assertEquals(-1, current.beginGroup);
        assertEquals(1, current.nexts.size());
        assertEquals(current, other.nexts.get(0));

        current = current.nexts.get(0);
        assertEquals(CHAR_NUM, current.beginChar);
        assertEquals(1, current.beginGroup);
        assertEquals(1, current.nexts.size());

        assertEquals(current.nexts.get(0), current);
    }

    @Test
    public void testOn() {
        Pattern complex = PSeq.apply(new Pattern[]{
                new PIntAny(3, 5, false),
                new PToken(new TSymbol("-")),
                PUnion.apply(new Pattern[]{
                        PEmpty$.MODULE$,
                        PSeq.apply(new Pattern[]{
                                new PIntAny(4, 6, false),
                                new PToken(new TSymbol("~"))
                        })
                }),
                new PToken(new TSymbol("/")),
                new PIntAny(3, 3, false)
        });

        complex.visit(new NamingVisitor());
        Map<String, Integer> groups = new HashMap<>();
        groups.put("_0_0", 0);
        groups.put("_0_2", 1);
        groups.put("_0_4", 2);

        TransitionBuilder2 tb = new TransitionBuilder2().on(complex);

        assertEquals(20, tb.transitions.size());

        assertEquals(1, tb.transitions.get(0).size());
        assertEquals((1), tb.transitions.get(0).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(1).size());
        assertEquals((2), tb.transitions.get(1).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(2).size());
        assertEquals((3), tb.transitions.get(2).get(CHAR_NUM));

        assertEquals(2, tb.transitions.get(3).size());
        assertEquals((4), tb.transitions.get(3).get(CHAR_NUM));
        assertEquals((5), tb.transitions.get(3).get((int) '-'));

        assertEquals(2, tb.transitions.get(4).size());
        assertEquals((6), tb.transitions.get(4).get(CHAR_NUM));
        assertEquals((5), tb.transitions.get(4).get((int) '-'));

        assertEquals(1, tb.transitions.get(6).size());
        assertEquals((5), tb.transitions.get(6).get((int) '-'));

        assertEquals(2, tb.transitions.get(5).size());
        assertEquals((7), tb.transitions.get(5).get(CHAR_EMPTY));
        assertEquals((8), tb.transitions.get(5).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(7).size());
        assertEquals((9), tb.transitions.get(7).get((int) '/'));

        assertEquals(1, tb.transitions.get(8).size());
        assertEquals((10), tb.transitions.get(8).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(9).size());
        assertEquals((11), tb.transitions.get(9).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(10).size());
        assertEquals((12), tb.transitions.get(10).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(11).size());
        assertEquals((13), tb.transitions.get(11).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(12).size());
        assertEquals((14), tb.transitions.get(12).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(13).size());
        assertEquals((15), tb.transitions.get(13).get(CHAR_NUM));

        assertEquals(2, tb.transitions.get(14).size());
        assertEquals((16), tb.transitions.get(14).get(CHAR_NUM));
        assertEquals((17), tb.transitions.get(14).get((int) '~'));

        assertEquals(1, tb.transitions.get(15).size());
        assertEquals((19), tb.transitions.get(15).get(EOI));

        assertEquals(2, tb.transitions.get(16).size());
        assertEquals((17), tb.transitions.get(16).get((int) '~'));
        assertEquals((18), tb.transitions.get(16).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(17).size());
        assertEquals((9), tb.transitions.get(17).get((int) '/'));

        assertEquals(1, tb.transitions.get(18).size());
        assertEquals((17), tb.transitions.get(18).get((int) '~'));

        assertEquals(20, tb.groupGuideMap.size());
        assertEquals((-1 << 2), tb.groupGuideMap.get(0));
        assertEquals((0 << 2) + 1, tb.groupGuideMap.get(1));
        assertEquals((0 << 2), tb.groupGuideMap.get(2));
        assertEquals((0 << 2), tb.groupGuideMap.get(3));
        assertEquals((0 << 2), tb.groupGuideMap.get(4));
        assertEquals((-2 << 2) + 2, tb.groupGuideMap.get(5));
        assertEquals((0 << 2), tb.groupGuideMap.get(6));
        assertEquals((1 << 2) + 1, tb.groupGuideMap.get(7));
        assertEquals((1 << 2) + 1, tb.groupGuideMap.get(8));
        assertEquals((-3 << 2) + 2, tb.groupGuideMap.get(9));
        assertEquals((1 << 2), tb.groupGuideMap.get(10));
        assertEquals((2 << 2) + 1, tb.groupGuideMap.get(11));
        assertEquals((1 << 2), tb.groupGuideMap.get(12));
        assertEquals((2 << 2), tb.groupGuideMap.get(13));
        assertEquals((1 << 2), tb.groupGuideMap.get(14));
        assertEquals((2 << 2), tb.groupGuideMap.get(15));
        assertEquals((1 << 2), tb.groupGuideMap.get(16));
        assertEquals((1 << 2), tb.groupGuideMap.get(17));
        assertEquals((1 << 2), tb.groupGuideMap.get(18));
        assertEquals((-4 << 2) + 2, tb.groupGuideMap.get(19));
    }

    @Test
    public void testOnWithEmpty() {
        Pattern complex = PSeq.apply(new Pattern[]{
                new PIntAny(3, 3, false),
                PUnion.apply(new Pattern[]{
                        PEmpty$.MODULE$,
                        new PToken(new TSymbol("~"))
                }),
                new PIntAny(3, 3, false)
        });

        complex.visit(new NamingVisitor());
        Map<String, Integer> groups = new HashMap<>();
        groups.put("_0_0", 0);
        groups.put("_0_1", 1);
        groups.put("_0_2", 2);

        TransitionBuilder2 tb = new TransitionBuilder2().on(complex);

        assertEquals(9, tb.endState);

        assertEquals(1, tb.transitions.get(0).size());
        assertEquals((1), tb.transitions.get(0).get(CHAR_NUM));
        assertEquals(1, tb.transitions.get(1).size());
        assertEquals((2), tb.transitions.get(1).get(CHAR_NUM));
        assertEquals(1, tb.transitions.get(2).size());
        assertEquals((3), tb.transitions.get(2).get(CHAR_NUM));
        assertEquals(2, tb.transitions.get(3).size());
        assertEquals((4), tb.transitions.get(3).get(CHAR_EMPTY));
        assertEquals((5), tb.transitions.get(3).get((int) '~'));
        assertEquals(1, tb.transitions.get(4).size());
        assertEquals((6), tb.transitions.get(4).get(CHAR_NUM));
        assertEquals(1, tb.transitions.get(5).size());
        assertEquals((6), tb.transitions.get(5).get(CHAR_NUM));
        assertEquals(1, tb.transitions.get(6).size());
        assertEquals((7), tb.transitions.get(6).get(CHAR_NUM));
        assertEquals(1, tb.transitions.get(7).size());
        assertEquals((8), tb.transitions.get(7).get(CHAR_NUM));
        assertEquals(1, tb.transitions.get(8).size());
        assertEquals((9), tb.transitions.get(8).get(EOI));

        assertEquals(10, tb.groupGuideMap.size());
        assertEquals(-1 << 2, tb.groupGuideMap.get(0));
        assertEquals(1, tb.groupGuideMap.get(1));
        assertEquals(0, tb.groupGuideMap.get(2));
        assertEquals(0, tb.groupGuideMap.get(3));
        assertEquals((1 << 2) + 3, tb.groupGuideMap.get(4));
        assertEquals((1 << 2) + 3, tb.groupGuideMap.get(5));
        assertEquals((2 << 2) + 3, tb.groupGuideMap.get(6));
        assertEquals(2 << 2, tb.groupGuideMap.get(7));
        assertEquals(2 << 2, tb.groupGuideMap.get(8));
        assertEquals((-4 << 2) + 2, tb.groupGuideMap.get(9));
    }

    @Test
    public void testOnInfinite() {
        Pattern complex = PSeq.apply(new Pattern[]{
                new PIntAny(3, -1, false),
                new PToken(new TSymbol("-")),
                new PIntAny(1, -1, false)});
        complex.visit(new NamingVisitor());
        Map<String, Integer> groups = new HashMap<>();
        groups.put("_0_0", 0);
        groups.put("_0_2", 1);

        TransitionBuilder2 tb = new TransitionBuilder2().on(complex);

        assertEquals(7, tb.transitions.size());

        assertEquals(1, tb.transitions.get(0).size());
        assertEquals((1), tb.transitions.get(0).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(1).size());
        assertEquals((2), tb.transitions.get(1).get(CHAR_NUM));

        assertEquals(1, tb.transitions.get(2).size());
        assertEquals((3), tb.transitions.get(2).get(CHAR_NUM));

        assertEquals(2, tb.transitions.get(3).size());
        assertEquals((3), tb.transitions.get(3).get(CHAR_NUM));
        assertEquals((4), tb.transitions.get(3).get((int) '-'));

        assertEquals(1, tb.transitions.get(4).size());
        assertEquals((5), tb.transitions.get(4).get(CHAR_NUM));

        assertEquals(2, tb.transitions.get(5).size());
        assertEquals((5), tb.transitions.get(5).get(CHAR_NUM));
        assertEquals((6), tb.transitions.get(5).get(EOI));

        assertEquals(7, tb.groupGuideMap.size());
        assertEquals(-1 << 2, tb.groupGuideMap.get(0));
        assertEquals(1, tb.groupGuideMap.get(1));
        assertEquals(0, tb.groupGuideMap.get(2));
        assertEquals(0, tb.groupGuideMap.get(3));
        assertEquals((-2 << 2) + 2, tb.groupGuideMap.get(4));
        assertEquals((1 << 2) + 1, tb.groupGuideMap.get(5));
        assertEquals((-3 << 2) + 2, tb.groupGuideMap.get(6));
    }

    @Test
    public void testOnZeroLength() {
        Pattern complex = PSeq.apply(new Pattern[]{
                new PIntAny(1, 2, false),
                PUnion.apply(new Pattern[]{
                        PEmpty$.MODULE$,
                        new PToken(new TSymbol("~"))
                }),
                new PIntAny(0, 2, false)
        });

        TransitionBuilder2 tb = new TransitionBuilder2().on(complex);

        return;
    }

    @Test
    public void testMergeOnInterestingPattern() {
        //<U>(s,<S>(ADT,/,PRT),<S>(Kajqol,,),<S>(Khan,_,Youth),<S>(',s),<S>(PNF,#,93922),<wordany 1:-1>,<empty>,<S>(#,2),<wordany 1:-1>,),<S>(IN,,),<S>(Zadran,_,Women),<wordany 1:-1>,S,<S>(Leveling,,))
        Pattern complex = PUnion.apply(new Pattern[]{
                new PToken(new TWord("s")),
                PSeq.apply(new Pattern[]{
                        new PToken(new TWord("ADT")),
                        new PToken(new TSymbol("/")),
                        new PToken(new TWord("PRT"))
                }),
                PSeq.apply(new Pattern[]{
                        new PToken(new TWord("Kajqol")),
                        new PToken(new TSymbol(","))
                }),
                PSeq.apply(new Pattern[]{
                        new PToken(new TWord("Khan")),
                        new PToken(new TSymbol("_")),
                        new PToken(new TWord("Youth"))
                }),
                PSeq.apply(new Pattern[]{
                        new PToken(new TSymbol("'")),
                        new PToken(new TWord("s"))
                }),
                PSeq.apply(new Pattern[]{
                        new PToken(new TWord("PNF")),
                        new PToken(new TSymbol("#")),
                        new PToken(new TInt("93922"))
                }),
                new PWordAny(1, -1),
                PEmpty$.MODULE$,
                PSeq.apply(new Pattern[]{
                        new PToken(new TSymbol("#")),
                        new PToken(new TInt("2"))
                }),
                new PWordAny(1, -1),
                new PToken(new TSymbol(")")),
                PSeq.apply(new Pattern[]{
                        new PToken(new TWord("IN")),
                        new PToken(new TSymbol(","))
                }),
                PSeq.apply(new Pattern[]{
                        new PToken(new TWord("Zadran")),
                        new PToken(new TSymbol("_")),
                        new PToken(new TWord("Women"))
                }),
                new PWordAny(1, -1),
                new PToken(new TWord("S")),
                PSeq.apply(new Pattern[]{
                        new PToken(new TWord("Leveling")),
                        new PToken(new TSymbol(","))
                })
        });
        TransitionBuilder2 tb = new TransitionBuilder2().on(complex);
        assertEquals(66, tb.endState);
    }

    @Test
    public void testBuildListOnUnion() {
        Pattern complex = PUnion.apply(new Pattern[]{
                new PToken(new TWord("ABAT")),
                new PToken(new TWord("AXAMP"))
        });
        complex.name_$eq("_1");

        TransitionBuilder2.TSection sec = new TransitionBuilder2().buildList(complex, ImmutableMap.of("_1", 0));

        assertEquals(1, sec.head.size());
        assertEquals(2, sec.tail.size());

        assertEquals('A', sec.head.get(0).beginChar);
        Set<Integer> chars = sec.tail.stream().map(t -> t.beginChar).collect(Collectors.toSet());
        assertEquals(2, chars.size());
        assertTrue(chars.contains((int) 'P'));
        assertTrue(chars.contains((int) 'T'));
    }

    @Test
    public void testUnion() {
        Pattern complex = PUnion.apply(new Pattern[]{
                new PToken(new TWord("ABA")),
                new PToken(new TWord("AXA"))
        });

        TransitionBuilder2 tb = new TransitionBuilder2().on(complex);

        assertEquals(7, tb.transitions.size());

        assertEquals(1, tb.transitions.get(0).size());
        assertEquals(1, tb.transitions.get(0).get('A'));

        assertEquals(2, tb.transitions.get(1).size());
        assertEquals(2, tb.transitions.get(1).get('B'));
        assertEquals(3, tb.transitions.get(1).get('X'));

        assertEquals(1, tb.transitions.get(2).size());
        assertEquals(4, tb.transitions.get(2).get('A'));

        assertEquals(1, tb.transitions.get(3).size());
        assertEquals(5, tb.transitions.get(3).get('A'));

        assertEquals(1, tb.transitions.get(5).size());
        assertEquals(6, tb.transitions.get(5).get(EOI));
        assertEquals(1, tb.transitions.get(4).size());
        assertEquals(6, tb.transitions.get(4).get(EOI));
    }

    @Test
    public void testWeirdCase() {
        Pattern pattern =
                PSeq.apply(new Pattern[]{
                        new PToken(new TSymbol("(")),
                        new PIntAny(3, 6, false),
                        PUnion.apply(
                                new Pattern[]{
                                        new PToken(new TSymbol(")")),
                                        PEmpty$.MODULE$
                                }),
                        new PIntAny(0, 3, false),
                        new PToken(new TSymbol("-")),
                        new PIntAny(4, 4, false)
                });
        TransitionBuilder2 tb2 = new TransitionBuilder2();
        tb2.on(pattern);

        assertEquals(19, tb2.endState);
        assertEquals(1, tb2.transitions.get(0).get('('));
        assertEquals(2, tb2.transitions.get(1).get(CHAR_NUM));
        assertEquals(3, tb2.transitions.get(2).get(CHAR_NUM));
        assertEquals(4, tb2.transitions.get(3).get(CHAR_NUM));
        assertEquals(5, tb2.transitions.get(4).get(CHAR_NUM));
        assertEquals(6, tb2.transitions.get(4).get(CHAR_EMPTY));
        assertEquals(7, tb2.transitions.get(4).get(')'));
        assertEquals(7, tb2.transitions.get(5).get(')'));
        assertEquals(8, tb2.transitions.get(5).get(CHAR_NUM));
        assertEquals(6, tb2.transitions.get(5).get(CHAR_EMPTY));
        assertEquals(10, tb2.transitions.get(6).get(CHAR_EMPTY));
        assertEquals(9, tb2.transitions.get(6).get(CHAR_NUM));
        assertEquals(10, tb2.transitions.get(7).get(CHAR_EMPTY));
        assertEquals(9, tb2.transitions.get(7).get(CHAR_NUM));
        assertEquals(7, tb2.transitions.get(8).get(')'));
        assertEquals(11, tb2.transitions.get(8).get(CHAR_NUM));
        assertEquals(6, tb2.transitions.get(8).get(CHAR_EMPTY));
        assertEquals(12, tb2.transitions.get(9).get(CHAR_NUM));
        assertEquals(13, tb2.transitions.get(9).get('-'));
        assertEquals(13, tb2.transitions.get(10).get('-'));
        assertEquals(7, tb2.transitions.get(11).get(')'));
        assertEquals(6, tb2.transitions.get(11).get(CHAR_EMPTY));
        assertEquals(13, tb2.transitions.get(12).get('-'));
        assertEquals(14, tb2.transitions.get(12).get(CHAR_NUM));
        assertEquals(15, tb2.transitions.get(13).get(CHAR_NUM));
        assertEquals(13, tb2.transitions.get(14).get('-'));
        assertEquals(16, tb2.transitions.get(15).get(CHAR_NUM));
        assertEquals(17, tb2.transitions.get(16).get(CHAR_NUM));
        assertEquals(18, tb2.transitions.get(17).get(CHAR_NUM));
        assertEquals(19, tb2.transitions.get(18).get(EOI));

        assertEquals(4, tb2.numGroup);

        assertEquals(20, tb2.groupGuideMap.size());
        assertEquals(-4, tb2.groupGuideMap.get(0));
        assertEquals(-4, tb2.groupGuideMap.get(1));
        assertEquals(1, tb2.groupGuideMap.get(2));
        assertEquals(0, tb2.groupGuideMap.get(3));
        assertEquals(0, tb2.groupGuideMap.get(4));
        assertEquals(0, tb2.groupGuideMap.get(5));
        assertEquals(7, tb2.groupGuideMap.get(6));
        assertEquals(7, tb2.groupGuideMap.get(7));
        assertEquals(0, tb2.groupGuideMap.get(8));
        assertEquals(11, tb2.groupGuideMap.get(9));
        assertEquals(11, tb2.groupGuideMap.get(10));
        assertEquals(0, tb2.groupGuideMap.get(11));
        assertEquals(8, tb2.groupGuideMap.get(12));
        assertEquals(-14, tb2.groupGuideMap.get(13));
        assertEquals(8, tb2.groupGuideMap.get(14));
        assertEquals(13, tb2.groupGuideMap.get(15));
        assertEquals(12, tb2.groupGuideMap.get(16));
        assertEquals(12, tb2.groupGuideMap.get(17));
        assertEquals(12, tb2.groupGuideMap.get(18));
        assertEquals(-18, tb2.groupGuideMap.get(19));
    }

    @Test
    public void testOnComplexPattern() {
//        <S>(<letterany 0:11>,<intany 1:12, false>,<labelany 0:10>,<intany 0:12, false>,
//        <letterany 0:8>,<intany 0:12, false>,
//        <letterany 0:5>,<intany 0:9, false>,<letterany 0:4>,<intany 0:7, false>,<letterany 0:1>,<intany 0:3, false>)
        Pattern ptn = PSeq.apply(new Pattern[]{
                new PLetterAny(0, 11),
                new PIntAny(1, 12, false),
                new PLabelAny(0, 10),
                new PIntAny(0, 12, false),
                new PLetterAny(0, 8),
                new PIntAny(0, 12, false),
                new PLetterAny(0, 8),
                new PIntAny(0, 12, false),
                new PLetterAny(0, 5),
                new PIntAny(0, 9, false),
                new PLetterAny(0, 4),
                new PIntAny(0, 7, false),
                new PLetterAny(0, 1),
                new PIntAny(0, 3, false)
        });
        TransitionBuilder tb = new TransitionBuilder2().on(ptn);
        assertEquals(128,tb.endState);
    }
}


