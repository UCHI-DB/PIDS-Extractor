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
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.List;

/**
 * Build a transition table, only on simple sequence of patterns
 */

public class TransitionBuilder {

    public static final int EOI = 256;
    public static final int CHAR_NUM = 257;
    public static final int CHAR_HEX = 258;
    public static final int CHAR_LETTER = 259;
    public static final int CHAR_EMPTY = 260;
    public static final int CHAR_LABEL = 261;
    public static final int CHAR_WORD = 262;

    Int2IntMap groupGuideMap = new Int2IntOpenHashMap();
    List<Int2IntMap> transitions = new ArrayList<>();
    int endState;
    int numGroup;

    public TransitionBuilder on(Pattern main) {
        List<Pattern> patterns = null;
        if (main instanceof PSeq) {
            patterns = JavaConverters.seqAsJavaList(((PSeq) main).content());
        } else {
            patterns = new ArrayList<>();
            patterns.add(main);
        }

        int stateCounter = 0;

        transitions.clear();

        int prevMin = 0;
        int prevMax = 0;

        int curMin = -1;
        int curMax = -1;

        boolean groupStarted = false;

        transitions.add(new Int2IntOpenHashMap());

        stateCounter = 1;

        numGroup = 0;
        int groupCounter = 0;

        for (Pattern pattern : patterns) {
            int startState = stateCounter;
            int beginChar;

            if (groupStarted) {
                // End previous group
                groupGuideMap.put(startState,
                        groupGuideMap.getOrDefault(startState, 0) | 2 | (groupCounter++) << 2);
                groupStarted = false;
            }

            if (pattern instanceof PToken) {
                String tokenvalue = ((PToken) pattern).token().value();
                beginChar = tokenvalue.charAt(0);
                curMin = tokenvalue.length();
                curMax = tokenvalue.length();

                for (int i = 0; i < tokenvalue.length(); i++) {
                    Int2IntOpenHashMap current = new Int2IntOpenHashMap();
                    if (i + 1 < tokenvalue.length()) {
                        current.put((int) tokenvalue.charAt(i + 1), startState + i + 1);
                    }
                    transitions.add(current);
                }
            } else if (pattern instanceof PAny) {
                PAny any = (PAny) pattern;
                if (pattern instanceof PIntAny) {
                    beginChar = ((PIntAny) pattern).hasHex() ? CHAR_HEX : CHAR_NUM;
                } else if (pattern instanceof PLetterAny) {
                    beginChar = CHAR_LETTER;
                } else if (pattern instanceof PLabelAny) {
                    beginChar = CHAR_LABEL;
                } else if (pattern instanceof PWordAny) {
                    beginChar = CHAR_WORD;
                } else {
                    throw new IllegalArgumentException();
                }
                curMin = any.minLength();
                curMax = any.maxLength();
                boolean hasEmpty = false;
                if (curMin == 0) {
                    hasEmpty = true;
                    curMin = 1;
                }
                if (curMax == -1) {
                    for (int i = 0; i < curMin; i++) {
                        Int2IntOpenHashMap current = new Int2IntOpenHashMap();
                        current.put(beginChar, startState + i + 1);
                        transitions.add(current);
                    }
                    transitions.get(transitions.size() - 1).put(beginChar, transitions.size() - 1);
                    curMax = curMin;
                } else {
                    for (int i = 0; i < curMax; i++) {
                        Int2IntOpenHashMap current = new Int2IntOpenHashMap();
                        if (i != curMax - 1) {
                            current.put(beginChar, startState + i + 1);
                        }
                        transitions.add(current);
                    }
                }
                if (hasEmpty) {
                    Int2IntOpenHashMap current = new Int2IntOpenHashMap();
                    for (int i = prevMin; i <= prevMax; i++) {
                        transitions.get(i).put(CHAR_EMPTY, transitions.size());
                    }
                    transitions.add(current);
                    curMax += 1;
                }

                groupGuideMap.put(startState, groupGuideMap.getOrDefault(startState, 0) | 1);
                groupStarted = true;
                numGroup++;
            } else if (pattern instanceof PEmpty$) {
                continue;
            } else {
                throw new IllegalArgumentException();
            }

            for (int i = prevMin; i <= prevMax; i++) {
                transitions.get(i).put(beginChar, startState);
            }
            prevMin = startState + curMin - 1;
            prevMax = startState + curMax - 1;

            stateCounter = prevMax + 1;
        }


        this.endState = stateCounter;
        // EOI move to final state
        for (int i = prevMin; i <= prevMax; i++) {
            transitions.get(i).put(EOI, this.endState);
        }
        if (groupStarted) {
            groupGuideMap.put(this.endState, 2 | (groupCounter << 2));
        }

        return this;
    }


    public static int translate(char c) {
        if (c >= '0' && c <= '9') {
            return CHAR_NUM;
        }
        if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
            return CHAR_HEX;
        }
        if ((c >= 'g' && c <= 'z') || (c >= 'G' && c <= 'Z')) {
            return CHAR_LETTER;
        }
        return c;
    }
}
