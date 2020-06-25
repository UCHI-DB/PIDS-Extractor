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

import org.apache.commons.lang3.StringUtils;

public class MatcherHelper {

    public static int equalString(String source, String input, int start) {
        return equalString(source, 0, input, start);
    }

    public static int equalString(String source, int offset, String input, int start) {
        int minRound = Math.min(input.length() - start, source.length() - offset);
        for (int i = 0; i < minRound; i++) {
            if (source.charAt(offset + i) != input.charAt(start + i)) {
                return i;
            }
        }
        return minRound;
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isHex(char c) {
        return ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'));
    }

    public static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static boolean isLabel(char c) {
        return isLetter(c) || isDigit(c);
    }

    /**
     * Accept characters in words/phrases
     *
     * @param c
     * @return
     */
    public static boolean isWord(char c) {
        return isDigit(c) || isLetter(c) || c == ' ' || c == '.' || c == '-';
    }


    public static int isInteger(String input, int start, int end, boolean hasHex) {
        int minEnd = Math.min(input.length(), end);
        for (int i = start; i < minEnd; i++) {
            char c = input.charAt(i);
            if (!hasHex ? isHex(c) : isDigit(c))
                return i - start;
        }
        return minEnd - start;
    }

    public static boolean isDouble(String input) {
        return input.length() == isDouble(input, 0, input.length());
    }

    // Use a state machine with the following states:
    // 0: start
    // 1: read negative sign
    // 2: before point (end state)
    // 3: after point
    // 4: after point with a number (end state)
    // 5: with e
    // 6: with e and -
    // 7: with e and a number (end state)
    public static int isDouble(String s, int start, int end) {
        int minEnd = Math.min(end, s.length());
        int state = 1;

        for (int i = start; i < minEnd; i++) {
            char c = s.charAt(i);
            switch (state) {
                case 1:
                    if (c == '-') {
                        state = 2;
                    } else if (Character.isDigit(c)) {
                        state = 4;
                    } else if (c == '.') {
                        state = 8;
                    } else {
                        return i - start;
                    }
                    break;
                case 2: {
                    if (Character.isDigit(c)) {
                        state = 4;
                    } else if (c == '.') {
                        state = 8;
                    } else {
                        return i - start;
                    }
                    break;
                }
                case 4: {
                    if (c == '.') {
                        state = 8;
                    } else if (c == 'e' || c == 'E') {
                        state = 32;
                    } else if (Character.isDigit(c)) {

                    } else {
                        return i - start;
                    }
                    break;
                }
                case 8: {
                    if (Character.isDigit(c)) {
                        state = 16;
                    } else {
                        return i - start;
                    }
                    break;
                }
                case 16: {
                    if (Character.isDigit(c)) {

                    } else if (c == 'e' || c == 'E') {
                        state = 32;
                    } else {
                        return i - start;
                    }
                    break;
                }
                case 32: {
                    if (Character.isDigit(c)) {
                        state = 128;
                    } else if (c == '-') {
                        state = 64;
                    } else {
                        return i - start;
                    }
                    break;
                }
                case 64: {
                    if (Character.isDigit(c)) {
                        state = 128;
                    } else {
                        return i - start;
                    }
                    break;
                }
                case 128: {
                    if (Character.isDigit(c)) {

                    } else {
                        return i - start;
                    }
                    break;
                }
                default:
                    return i - start;
            }
        }
        return (state & 148) != 0 ? (minEnd - start) : (minEnd - start - 1);
    }


    public static boolean merge(String[] a, String[] b) {
        int mergeIndex = -1;
        for (int i = 0; i < a.length; i++) {
            String ap = a[i];
            String bp = b[i];
            if (!ap.equals(bp)) {
                if (mergeIndex != -1) {
                    return false;
                } else {
                    mergeIndex = i;
                }
            }
        }
        // Equal
        if (mergeIndex == -1) {
            return true;
        }

        // Try to merge the singularity
        String ap = a[mergeIndex];
        String bp = b[mergeIndex];

        if (ap.equals("%")) {
            return true;
        }
        if (bp.equals("%")) {
            a[mergeIndex] = "%";
            return true;
        }
        String acore = StringUtils.strip(ap, "%");
        String bcore = StringUtils.strip(bp, "%");
        // Not equal, but after strip equal, can only be either x and x%, x and %x, %x and x%, or prefix/suffix and %x%
        // In the latter two cases, the result should be %x%
        if (acore.equals(bcore)) {
            if (acore.equals(ap)) {
                a[mergeIndex] = bp;
            } else if (acore.equals(bp)) {
                a[mergeIndex] = ap;
            } else {
                a[mergeIndex] = String.format("%%%s%%", acore);
            }
            return true;
        }
        return false;
    }
}
