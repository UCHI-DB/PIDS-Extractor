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

import edu.uchicago.cs.db.subattr.ir.Pattern;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import scala.Char;

import java.util.*;

import static edu.uchicago.cs.db.subattr.ir.matcher.TransitionBuilder.*;


public class FrontierWildcardMatcher extends AbstractMatcher implements WildcardMatcher {

    TransitionBuilder builder;
    int[] groupGuide;

    public FrontierWildcardMatcher(Pattern pattern) {
        super(pattern);
        builder = new TransitionBuilder2();
        builder.on(pattern);

        // One more state for end of input
        groupGuide = new int[builder.transitions.size() + 1];
        for (Int2IntMap.Entry entry : builder.groupGuideMap.int2IntEntrySet()) {
            groupGuide[entry.getIntKey()] = entry.getIntValue();
        }
    }

    String input;
    LinkedList<Frontier> frontiers = new LinkedList<>();
    Set<Frontier> existed = new HashSet<>();

    class Frontier {
        int pointer = 0;
        int state = 0;

        int[] buffered;
        int offset;

        Frontier() {
            buffered = new int[input.length() * builder.numGroup];
        }

        void init() {
        }

        Frontier spawn(int state, int pointer) {
            Frontier newFrontier = new Frontier();
            newFrontier.pointer = pointer;
            newFrontier.state = state;
            System.arraycopy(buffered, 0, newFrontier.buffered, 0, buffered.length);
            newFrontier.offset = offset;
            return newFrontier;
        }

        boolean success() {
            return pointer == input.length() + 1 && state == builder.endState;
        }

        // Mark the character at pointer location to be in current group
        void push(int groupIndex, int newchar, int pos) {
            if (groupIndex != -1) {
                buffered[offset++] = (groupIndex << 16) | pos;
            }
        }

        void push(int newchar, int offset) {
            push(currentGroup(), newchar, offset);
        }

        void push(int newchar) {
            push(newchar, pointer);
        }

        int currentGroup() {
            int gg = groupGuide[this.state];
            int val = gg >> 2;
            return (val < 0) ? -1 : val;
        }

        int maxGroup() {
            int gg = groupGuide[this.state];
            int val = gg >> 2;
            if (val < 0) {
                return -val - 2;
            }
            return val;
        }

        @Override
        public boolean equals(Object obj) {
            Frontier another = (Frontier) obj;
            return state == another.state &&
                    pointer == another.pointer;
        }

        @Override
        public int hashCode() {
            HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(state);
            builder.append(pointer);
            return builder.toHashCode();
        }
    }

    /**
     * Match a token sequence with corresponding pattern
     * <p>
     * As the token sequence will be toured several times, use
     * Seq rather than iterator here
     *
     * @param input token sequences as string
     * @return None if not match
     * Some(Record) if match found
     */
    @Override
    public List<String[]> match(String input) {
        this.input = input;
        this.frontiers.clear();

        Frontier first = new Frontier();
        first.state = 0;
        frontiers.add(first);
        existed.add(first);

        List<String[]> buffer = new ArrayList<>();

        while (!frontiers.isEmpty()) {
            Frontier f = frontiers.remove(0);
            existed.remove(f);
            moveForward(f);
            if (f.success()) {
                // Found one
                addResult(buffer, f);
//                return buffer;
            }
        }
        return buffer;
    }

    void addResult(List<String[]> buffer, Frontier f) {
        String[] candidate = new String[builder.numGroup];
        StringBuilder[] resultBuffer = new StringBuilder[candidate.length];
        for (int i = 0; i < candidate.length; i++) {
            resultBuffer[i] = new StringBuilder();
        }

        for (int i = 0; i < f.offset; i++) {
            int groupIndex = f.buffered[i] >> 16;
            int charpos = f.buffered[i] & 0xFFFF;
            int thechar = input.charAt(charpos);
            StringBuilder target = resultBuffer[groupIndex];

            // Merge duplicate wildcards
            char prev = target.length() != 0 ? target.charAt(target.length() - 1) : 511;
            if (!(thechar == '%' && prev == '%')) {
                resultBuffer[groupIndex].append(input.charAt(charpos));
            }
        }
        for (int i = 0; i < candidate.length; i++) {
            candidate[i] = resultBuffer[i].toString();
        }

        for (int i = 0; i < buffer.size(); i++) {
            String[] exist = buffer.get(i);
            if (MatcherHelper.merge(exist, candidate)) {
                return;
            }
        }
        buffer.add(candidate);
    }

    void moveForward(Frontier f) {
        f.init();
        while (f.pointer <= input.length() && f.state != builder.endState) {
            int nextchar = (f.pointer == input.length()) ? EOI : input.charAt(f.pointer);

            if (nextchar == '%') {
                if (f.pointer == input.length() - 1) {
                    // % is the last symbol, simply add a '%' to all group and match
                    int gi = f.maxGroup();
                    // In these cases, the previous group has no space for more symbols
                    if (f.currentGroup() < 0) {
                        gi += 1;
                    } else if (isLastStateOfGroup(f.state)) {
                        gi += 1;
                    }
                    while (gi < builder.numGroup) {
                        f.push(gi, '%', f.pointer);
                        gi++;
                    }

                    f.pointer = input.length() + 1;
                    f.state = builder.endState;
                    continue;
                }


                // Get the next symbol that is not wildcard
                int lookahead = (f.pointer == input.length() - 1) ? EOI : input.charAt(f.pointer + 1);
                // Do a bfs on the transition tree, recording the path only contains group marker.
                // Stop on states containing the transition of lookahead symbol
                IntSet visited = new IntOpenHashSet();
                IntList toSearch = new IntArrayList();
                toSearch.addAll(builder.transitions.get(f.state).values());
                int searchPointer = 0;

                Int2IntMap found = new Int2IntOpenHashMap();
                while (searchPointer < toSearch.size()) {
                    int currentState = toSearch.getInt(searchPointer);
                    visited.add(currentState);

                    int toState = transit(currentState, lookahead);
                    IntList nextToSearch = new IntArrayList(builder.transitions.get(currentState).values());

                    if (toState != -1) {
                        nextToSearch.rem(toState);
                        visited.add(toState);
                        // Found the first state with next transition as lookahead
                        // Start the spawn here, and add % to all previous groups
                        found.put(toState, currentState);
                        // add all other states
                    }

                    for (int nextState : nextToSearch) {
                        if (!visited.contains(nextState)) {
                            visited.add(nextState);
                            toSearch.add(nextState);
                        }
                    }

                    searchPointer++;
                }

                // Create new frontier moving to ALL next states, keep the wildcard / not keep the wildcard
                for (Int2IntMap.Entry fentry : found.int2IntEntrySet()) {
                    int consumeState = fentry.getIntKey();
                    int keepState = fentry.getIntValue();

                    int consumeGroupInfo = groupGuide[consumeState];
                    int consumeCurrentGroup = consumeGroupInfo >> 2;
                    int consumeGroupCount = consumeCurrentGroup < 0 ? -consumeCurrentGroup - 2 : consumeCurrentGroup;

                    int keepGroupInfo = groupGuide[keepState];
                    int keepCurrentGroup = keepGroupInfo >> 2;
                    int keepGroupCount = keepCurrentGroup < 0 ? -keepCurrentGroup - 2 : keepCurrentGroup;

                    // Check if there's still space for current group
                    int groupOffset = isLastStateOfGroup(f.state) ? 1 : 0;
                    int fromGroup = (f.currentGroup() < 0 ? f.maxGroup() + 1 : f.currentGroup()) + groupOffset;


                    // This frontier assumes the wildcard has been consumed
                    Frontier consumed = f.spawn(consumeState, f.pointer + 1);
                    for (int g = fromGroup; g <= consumed.maxGroup(); g++) {
                        consumed.push(g, '%', f.pointer);
                    }
                    if (!existed.contains(consumed)) {
                        existed.add(consumed);
                        frontiers.add(0, consumed);
                    }
                    // This frontier assumes the wildcard is not used
                    Frontier keep = f.spawn(keepState, f.pointer);
                    for (int g = fromGroup; g <= keep.maxGroup(); g++) {
                        keep.push(g, '%', f.pointer);
                    }
                    if (!existed.contains(keep)) {
                        existed.add(keep);
                        frontiers.add(0, keep);
                    }
                }
                // Current Frontier: eat the wildcard and continue executing
                f.pointer++;
            } else {
                int nextstate = transit(f.state, nextchar);
                if (nextstate != -1) {
                    f.state = nextstate;
                    f.push(nextchar);

                    f.pointer++;
                } else {
                    f.state = -1;
                    return;
                }
            }
        }

    }

    boolean isLastStateOfGroup(int state) {
        Int2IntMap transitions = builder.transitions.get(state);
        if (transitions.size() == 1) {
            int onlyState = transitions.values().iterator().nextInt();
            if ((groupGuide[onlyState] & 2) != 0) {
                return true;
            }
        }
        return false;
    }

    int transit(int state, int symbol) {
        if (state >= builder.transitions.size())
            return -1;
        Int2IntMap nextmap = builder.transitions.get(state);

        if (nextmap.containsKey(symbol)) {
            return nextmap.get(symbol);
        }
        if (nextmap.containsKey(CHAR_NUM) && MatcherHelper.isDigit((char) symbol)) {
            return nextmap.get(CHAR_NUM);
        }
        if (nextmap.containsKey(CHAR_HEX) && MatcherHelper.isHex((char) symbol)) {
            return nextmap.get(CHAR_HEX);
        }
        if (nextmap.containsKey(CHAR_LETTER) && MatcherHelper.isLetter((char) symbol)) {
            return nextmap.get(CHAR_LETTER);
        }
        if (nextmap.containsKey(CHAR_LABEL) && MatcherHelper.isLabel((char) symbol)) {
            return nextmap.get(CHAR_LABEL);
        }
        if(nextmap.containsKey(CHAR_WORD) && MatcherHelper.isWord((char)symbol)) {
            return nextmap.get(CHAR_WORD);
        }
        return -1;
    }
}
