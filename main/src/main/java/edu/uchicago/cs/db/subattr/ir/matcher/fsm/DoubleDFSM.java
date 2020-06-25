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

package edu.uchicago.cs.db.subattr.ir.matcher.fsm;

/**
 * state machine with the following states: states n is represented by 2^n
 * // 0: start
 * // 1: read negative sign
 * // 2: before point (end state)
 * // 3: after point
 * // 4: after point with a number (end state)
 * // 5: with e
 * // 6: with e and -
 * // 7: with e and a number (end state)
 */
public class DoubleDFSM extends AbstractDFSM {

    public DoubleDFSM() {
        super();
    }

    public boolean accept(char c) {
        switch (state) {
            case 0:
                if (c == '-') {
                    state = 1;
                } else if (Character.isDigit(c)) {
                    state = 2;
                } else if (c == '.') {
                    state = 3;
                } else {
                    return false;
                }
                break;
            case 1: {
                if (Character.isDigit(c)) {
                    state = 2;
                } else if (c == '.') {
                    state = 3;
                } else {
                    return false;
                }
                break;
            }
            case 2: {
                if (c == '.') {
                    state = 3;
                } else if (c == 'e' || c == 'E') {
                    state = 5;
                } else if (Character.isDigit(c)) {

                } else {
                    return false;
                }
                break;
            }
            case 3: {
                if (Character.isDigit(c)) {
                    state = 4;
                } else {
                    return false;
                }
                break;
            }
            case 4: {
                if (Character.isDigit(c)) {

                } else if (c == 'e' || c == 'E') {
                    state = 5;
                } else {
                    return false;
                }
                break;
            }
            case 5: {
                if (Character.isDigit(c)) {
                    state = 7;
                } else if (c == '-') {
                    state = 6;
                } else {
                    return false;
                }
                break;
            }
            case 6: {
                if (Character.isDigit(c)) {
                    state = 7;
                } else {
                    return false;
                }
                break;
            }
            case 7: {
                if (Character.isDigit(c)) {

                } else {
                    return false;
                }
                break;
            }
            default:
                return false;
        }
        return true;
    }

    public boolean done() {
        return state == 2 || state == 4 || state == 7;
    }

    public int numStates() {
        return 8;
    }

    static int[][] AVAILABLE = new int[][]{
            new int[]{1, 2, 3, 4, 5, 6, 7},
            new int[]{2, 3, 4, 5, 6, 7},
            new int[]{2, 3, 4, 5, 6, 7},
            new int[]{4, 5, 6, 7},
            new int[]{4, 5, 6, 7},
            new int[]{6, 7},
            new int[]{7},
            new int[]{7}
    };

    public int[] available() {
        return AVAILABLE[state];
    }
}
