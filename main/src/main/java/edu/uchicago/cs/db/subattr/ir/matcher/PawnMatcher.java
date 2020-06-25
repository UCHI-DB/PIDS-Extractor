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

import edu.uchicago.cs.db.subattr.ir.PSeq;
import edu.uchicago.cs.db.subattr.ir.Pattern;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

import java.util.Arrays;

import static edu.uchicago.cs.db.subattr.ir.matcher.TransitionBuilder.*;

/**
 * The PawnMatcher does not branch
 */
public class PawnMatcher extends AbstractMatcher implements ExactMatcher {

    int[][] transitions;

    int[] groupGuide;

    int endState;

    int numGroup;

    public PawnMatcher(Pattern pattern) {
        super(pattern);
        build();
    }

    @Override
    public String[] match(String input) {
        String[] result = new String[numGroup];

        int matchStart = -1;
        int state = 0;

        int groupCount = 0;

        int pointer = 0;
        while (pointer <= input.length()) {

            int nextchar = (pointer == input.length()) ? EOI : input.charAt(pointer);
            int nextstate = transitions[state][nextchar];
            if (nextstate != -1) {
                state = nextstate;
                if ((groupGuide[state] & 2) != 0) {
                    result[groupCount++] = (input.substring(matchStart, pointer));
                }
                if ((groupGuide[state] & 1) != 0) {
                    // Start of group
                    matchStart = pointer;
                }
                pointer++;
            } else {
                return null;
            }
        }

        return (state == endState) ? result : null;
    }

    public int[][] getTransitions() {
        return transitions;
    }

    public void setTransitions(int[][] transitions) {
        this.transitions = transitions;
    }

    public int[] getGroupGuide() {
        return groupGuide;
    }

    public void setGroupGuide(int[] groupGuide) {
        this.groupGuide = groupGuide;
    }

    public int getEndState() {
        return endState;
    }

    public void setEndState(int endState) {
        this.endState = endState;
    }


    protected void build() {
        PSeq seq = (PSeq) pattern;
        TransitionBuilder tb = new TransitionBuilder2().on(seq);
        numGroup = tb.numGroup;
        transitions = new int[tb.transitions.size()][];
        for (int i = 0; i < transitions.length; i++) {
            transitions[i] = new int[257];
            Arrays.fill(transitions[i], -1);
            for (Int2IntMap.Entry entry : tb.transitions.get(i).int2IntEntrySet()) {
                switch (entry.getIntKey()) {
                    case CHAR_NUM:
                        for (int j = 0; j < 10; j++) {
                            transitions[i]['0' + j] = entry.getIntValue();
                        }
                        break;
                    case CHAR_LETTER:
                        for (int j = 0; j < 26; j++) {
                            transitions[i]['A' + j] = entry.getIntValue();
                            transitions[i]['a' + j] = entry.getIntValue();
                        }
                        break;
                    case CHAR_HEX:
                        for (int j = 0; j < 10; j++) {
                            transitions[i]['0' + j] = entry.getIntValue();
                        }
                        for (int j = 0; j < 6; j++) {
                            transitions[i]['a' + j] = entry.getIntValue();
                            transitions[i]['A' + j] = entry.getIntValue();
                        }
                        break;
                    default:
                        transitions[i][entry.getIntKey()] = entry.getIntValue();
                        break;
                }
            }
        }

        // One more state for end of input
        groupGuide = new int[transitions.length + 1];
        for (Int2IntMap.Entry entry : tb.groupGuideMap.int2IntEntrySet()) {
            groupGuide[entry.getIntKey()] = entry.getIntValue();
        }

        setEndState(tb.endState);
    }

}
