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

import edu.uchicago.cs.db.subattr.extract.parser.TSpace;
import edu.uchicago.cs.db.subattr.ir.*;
import it.unimi.dsi.fastutil.ints.*;
import scala.collection.JavaConverters;

import java.util.*;

/**
 * Build a transition table, working on complex patterns including empty and union
 */

public class TransitionBuilder2 extends TransitionBuilder {

//    Map<Integer, Integer> groupGuideMap = new HashMap<>();
//    List<Map<Integer, Integer>> transitions = new ArrayList<>();
//    int endState;

    List<Integer> prevs = new ArrayList<Integer>();

    protected void init() {
        transitions.clear();
        prevs.clear();
        prevs.add(0);
        transitions.add(new Int2IntOpenHashMap());
    }

    @Override
    public TransitionBuilder2 on(Pattern top) {
        init();

        top.visit(new NamingVisitor());
        GroupVisitor gv = new GroupVisitor();
        top.visit(gv);
        numGroup = gv.groupCount;

        TSection section = buildList(top, gv.groups);

        List<TNode> head = section.head;
        List<TNode> frontier = new LinkedList<>();

        TNode first = new TNode();
        first.state = 0;
        first.nexts = head;

        frontier.add(first);
        groupGuideMap.put(0, -1 << 2);

        int stateCounter = 1;

        // Assign states to TNode
        while (!frontier.isEmpty()) {
            TNode node = frontier.remove(0);
            Int2IntMap transition = transitions.get(node.state);
            int groupGuide = groupGuideMap.get(node.state);

            for (TNode n : node.nexts) {
                if (n.state == 0) {
                    n.state = stateCounter++;
                    transitions.add(new Int2IntOpenHashMap());
                    if (n.endGroup != -1) {
                        groupGuideMap.put(n.state, 2 | (groupGuideMap.getOrDefault(n.state, 0) & 3) | (-(n.endGroup + 2) << 2));
                    }
                    if (n.beginGroup != -1) {
                        groupGuideMap.put(n.state, 1 | (groupGuideMap.getOrDefault(n.state, 0) & 3) | (n.beginGroup << 2));
                    }
                    if (n.beginGroup == -1 && n.endGroup == -1) {
                        groupGuideMap.put(n.state, groupGuide & 0xFFFFFFFC);
                    }
                    if (n != node)
                        frontier.add(n);
                }
                transition.put(n.beginChar, n.state);
            }
        }

        endState = stateCounter;
        List<TNode> tail = section.tail;
        tail.forEach(n -> transitions.get(n.state).put(EOI, endState));
        if (section.groupCount != -1) {
            groupGuideMap.put(endState, 2 | (-(section.groupCount + 2) << 2));
        }
        transitions.add(Int2IntMaps.EMPTY_MAP);

        return this;
    }


    protected TSection buildList(Pattern pattern, Map<String, Integer> groups) {
        List<TNode> header = null;
        List<TNode> tail = null;
        int groupCount = -1;

        if (pattern instanceof PSeq) {
            PSeq seq = (PSeq) pattern;
            TSection prev = null;

            for (Pattern ptn : JavaConverters.seqAsJavaList(seq.content())) {
                TSection single = buildList(ptn, groups);
                if (header == null) {
                    header = single.head;
                } else {
                    for (TNode i : prev.tail) {
                        i.nexts.addAll(single.head);
                        if (prev.groupCount != -1) {
                            for (TNode n : single.head) {
                                n.endGroup = prev.groupCount;
                            }
                        }
                    }
                }
                prev = single;
            }
            tail = prev.tail;
            groupCount = prev.groupCount;
        } else if (pattern instanceof PUnion) {
            PUnion union = (PUnion) pattern;

            tail = new ArrayList<>();

            List<TNode> rawheader = new ArrayList<>();

            for (Pattern ptn : JavaConverters.seqAsJavaList(union.content())) {
                TSection single = buildList(ptn, groups);
                rawheader.addAll(single.head);
            }

            // Merge similar nodes
            Set<TNode> unionTails = new HashSet<>();
            header = merge(rawheader, unionTails);
            tail.addAll(unionTails);
        } else if (pattern instanceof PToken) {
            PToken token = (PToken) pattern;
            header = new ArrayList<TNode>();
            tail = new ArrayList<TNode>();
            if (token.token() instanceof TSpace) {
                TNode single = new TNode();
                single.beginChar = ' ';
                single.nexts.add(single);
                header.add(single);
                tail.add(single);
            } else {

                TNode current = null;

                String tokenvalue = token.token().value();
                for (int i = 0; i < tokenvalue.length(); i++) {
                    TNode thisnode = new TNode();
                    thisnode.beginChar = tokenvalue.charAt(i);
                    if (i == 0) {
                        header.add(thisnode);
                    } else {
                        current.nexts.add(thisnode);
                    }
                    current = thisnode;
                }
                tail.add(current);
            }
        } else if (pattern instanceof PAny) {
            PAny any = (PAny) pattern;
            // Ignore PDoubleAny here, it should not exist
            int beginChar;
            if (pattern instanceof PIntAny) {
                beginChar = ((PIntAny) pattern).hasHex() ? CHAR_HEX : CHAR_NUM;
            } else if (pattern instanceof PLetterAny) {
                beginChar = CHAR_LETTER;
            } else if (pattern instanceof PLabelAny) {
                beginChar = CHAR_LABEL;
            } else if (pattern instanceof PWordAny) {
                beginChar = CHAR_WORD;
            } else {
                throw new IllegalArgumentException(pattern.getClass().getName());
            }
            header = new ArrayList<>();
            tail = new ArrayList<>();
            TNode current = null;

            if (any.maxLength() == -1) {
                // Infinite length
                if (any.minLength() == 0) {
                    TNode infnode = new TNode();
                    infnode.beginChar = beginChar;
                    infnode.nexts.add(infnode);
                    header.add(infnode);
                    tail.add(infnode);

                    TNode emptynode = new TNode();
                    emptynode.beginChar = CHAR_EMPTY;
                    header.add(emptynode);
                    tail.add(emptynode);
                } else {
                    for (int i = 0; i < any.minLength(); i++) {
                        TNode thisnode = new TNode();
                        thisnode.beginChar = beginChar;
                        if (i == 0) {
                            header.add(thisnode);
                        } else {
                            current.nexts.add(thisnode);
                        }
                        current = thisnode;

                    }
                    // Create one more transition for inf loop
                    tail.add(current);
                    // Self loop
                    current.nexts.add(current);
                }
            } else {
                for (int i = 0; i < any.maxLength(); i++) {
                    TNode thisnode = new TNode();
                    thisnode.beginChar = beginChar;
                    if (i == 0) {
                        header.add(thisnode);
                    } else {
                        current.nexts.add(thisnode);
                    }
                    current = thisnode;
                    if (i >= any.minLength() - 1) {
                        tail.add(current);
                    }
                }
                if (any.minLength() == 0) {
                    TNode emptyMove = new TNode();
                    emptyMove.beginChar = CHAR_EMPTY;
                    header.add(emptyMove);
                    tail.add(emptyMove);
                }
            }
        } else if (pattern instanceof PEmpty$) {
            header = new ArrayList<>();
            tail = new ArrayList<>();

            TNode single = new TNode();
            single.beginChar = CHAR_EMPTY;
            header.add(single);
            tail.add(single);
        }

        TSection res = new TSection(header, tail);
        if (groups.containsKey(pattern.name())) {
            int currentGc = groups.get(pattern.name());
            res.head.forEach(n -> n.beginGroup = currentGc);
            res.groupCount = currentGc;
        } else {
            res.groupCount = groupCount;
        }
        return res;
    }

    protected static List<TNode> merge(List<TNode> frontiers, Set<TNode> tails) {
        Int2ObjectMap<TNode> unique = new Int2ObjectOpenHashMap<>();

        List<TNode> merged;
        if (frontiers.size() > 1) {
            for (TNode f : frontiers) {
                TNode exist = unique.getOrDefault(f.beginChar, null);
                if (exist != null) {
                    exist.nexts.addAll(f.nexts);
                } else {
                    unique.put(f.beginChar, f);
                }
            }
            merged = new ArrayList(unique.values());
        } else {
            merged = frontiers;
        }
        for (TNode last : merged) {
            boolean selfloop = last.nexts.contains(last);
            // Does not see a reason that self loop thing need merge
            if (!selfloop) {
                last.nexts = merge(last.nexts, tails);
                if (last.nexts.isEmpty()) {
                    tails.add(last);
                }
            } else {
                tails.add(last);
            }
        }
        return merged;
    }


    protected class TNode {
        List<TNode> nexts = new ArrayList<>();
        int beginChar;
        int beginGroup = -1;
        int endGroup = -1;
        int state;
        int groupNo;
    }

    protected class TSection {
        List<TNode> head;
        List<TNode> tail;
        int groupCount = -1;

        public TSection(List<TNode> h, List<TNode> t) {
            this.head = h;
            this.tail = t;
        }
    }

    protected static class GroupVisitor extends AbstractVisitor {

        Map<String, Integer> groups = new HashMap<>();

        int groupCount = 0;

        @Override
        public void on(Pattern ptn) {
            if (path().size() == 1 && (ptn instanceof PAny || ptn instanceof PUnion)) {
                groups.put(ptn.name(), groupCount++);
            }
        }
    }
}
