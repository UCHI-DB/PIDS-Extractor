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

import edu.uchicago.cs.db.subattr.ir.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static edu.uchicago.cs.db.subattr.ir.matcher.TransitionBuilder.*;

public class TransitionTable {

    int numGroup = -1;

    int start() {
        return 1;
    }

    int next(int fromState, int symbol) {
        StateNode current = states.get(fromState);

        StateNode next = states.get(fromState).next.getOrDefault(symbol, null);
        if (null != next) {
            return next.num;
        }
        if (current.next.containsKey(CHAR_NUM) && MatcherHelper.isDigit((char) symbol)) {
            return current.next.get(CHAR_NUM).num;
        }
        if (current.next.containsKey(CHAR_HEX) && MatcherHelper.isHex((char) symbol)) {
            return current.next.get(CHAR_HEX).num;
        }
        if (current.next.containsKey(CHAR_LETTER) && MatcherHelper.isLetter((char) symbol)) {
            return current.next.get(CHAR_LETTER).num;
        }
        if (current.next.containsKey(CHAR_WORD) && MatcherHelper.isWord((char) symbol)) {
            return current.next.get(CHAR_WORD).num;
        }
        return -1;
    }

    Pattern get(int state) {
        if (-1 == state) {
            return null;
        }
        return states.get(state).pattern;
    }

    int group(int state) {
        if (-1 == state) {
            return -1;
        }
        return states.get(state).groupNo;
    }

    Object2IntMap<String> nameToGroupNo = new Object2IntOpenHashMap<>();

    List<StateNode> states = new ArrayList<>();

    void build(Pattern pattern) {
        numGroup = 0;

        states.add(new StateNode());
        states.add(new StateNode());
        states.get(0).num = 0;
        states.get(1).num = 1;
        states.get(1).pattern = pattern;

        pattern.visit(new AbstractVisitor() {
            @Override
            public void on(Pattern ptn) {
                if (path().size() == 1 && (ptn instanceof PAny || ptn instanceof PUnion)) {
                    nameToGroupNo.put(ptn.name(), numGroup++);
                }
            }
        });
        StatePiece built = buildPiece(pattern);
        for (StateNode s : built.heads) {
            states.get(1).next.put(s.beginChar, s);
        }
        for (StateNode t : built.tails) {
            t.next.put(EOI, states.get(0));
        }
    }

    protected StatePiece buildPiece(Pattern pattern) {
        StatePiece piece = null;
        if (pattern instanceof PSeq) {
            PSeq seq = (PSeq) pattern;
            List<StateNode> head = null;
            StatePiece prev = null;
            for (Pattern c : JavaConverters.seqAsJavaList(seq.content())) {
                StatePiece current = buildPiece(c);
                if (null == head) {
                    head = current.heads;
                }
                if (prev != null) {
                    prev.connect(current);
                }
                prev = current;
            }
            piece = new StatePiece(head, prev.tails);
        } else if (pattern instanceof PUnion) {
            PUnion union = (PUnion) pattern;
            List<StateNode> heads = new ArrayList<>();
            List<StateNode> tails = new ArrayList<>();
            for (Pattern c : JavaConverters.seqAsJavaList(union.content())) {
                StatePiece current = buildPiece(c);
                heads.addAll(current.heads);
                tails.addAll(current.tails);
            }
            piece = new StatePiece(heads, tails);
        } else if (pattern instanceof PAny) {
            StateNode node = new StateNode();
            node.num = states.size();
            states.add(node);
            if (pattern instanceof PIntAny) {
                PIntAny pia = (PIntAny) pattern;
                node.beginChar = pia.hasHex() ? CHAR_HEX : CHAR_NUM;
            } else if (pattern instanceof PWordAny) {
                node.beginChar = CHAR_LETTER;
            } else if (pattern instanceof PLabelAny) {
                node.beginChar = CHAR_LABEL;
            } else if (pattern instanceof PWordAny) {
                node.beginChar = CHAR_WORD;
            } else {
                throw new IllegalArgumentException();
            }
            node.pattern = pattern;
            piece = new StatePiece(node);
        } else if (pattern instanceof PToken) {
            StateNode node = new StateNode();
            PToken pt = (PToken) pattern;
            node.num = states.size();
            states.add(node);
            node.beginChar = pt.token().value().charAt(0);
            node.pattern = pattern;
            piece = new StatePiece(node);
        } else if (pattern instanceof PEmpty$) {
            StateNode node = new StateNode();
            node.num = states.size();
            states.add(node);
            node.beginChar = CHAR_EMPTY;
            node.pattern = pattern;
            piece = new StatePiece(node);
        } else {
            throw new IllegalArgumentException(pattern.getClass().getSimpleName());
        }

        if (nameToGroupNo.containsKey(pattern.name())) {
            piece.assignGroup(nameToGroupNo.getInt(pattern.name()));
        }

        return piece;
    }


    class StateNode {
        int num;
        int beginChar;
        int groupNo = -1;
        Pattern pattern;
        Int2ObjectMap<StateNode> next = new Int2ObjectOpenHashMap<>();
    }

    class StatePiece {
        List<StateNode> heads;
        List<StateNode> tails;

        StatePiece(List<StateNode> h, List<StateNode> t) {
            this.heads = h;
            this.tails = t;
        }

        StatePiece(StateNode node) {
            this.heads = Collections.singletonList(node);
            this.tails = this.heads;
        }

        void connect(StatePiece next) {
            for (StateNode t : tails) {
                for (StateNode h : next.heads) {
                    t.next.put(h.beginChar, h);
                }
            }
        }

        public void assignGroup(int group) {
            List<StateNode> buffer = new ArrayList<>();
            int pointer = 0;
            buffer.addAll(heads);
            while (pointer < buffer.size()) {
                StateNode current = buffer.get(pointer);
                current.groupNo = group;
                buffer.addAll(current.next.values());
                pointer++;
            }
        }
    }

}
