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

package edu.uchicago.cs.db.subattr.ir.matcher.adhoc;

import edu.uchicago.cs.db.subattr.ir.matcher.ExactMatcher;

public class AdhocMatcherExample2 implements ExactMatcher {

    @Override
    public String[] match(String input) {
        int state = 0;
        int matchStart = 0;
        int i = 0;
        String[] matched = new String[3];

        for (i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            switch (state) {
                case 0:
                    matchStart = i;
                case 1:
                case 2:
                    switch (c) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            state += 1;
                            break;
                        default:
                            return null;
                    }
                    break;
                case 3:
                    switch (c) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            state = 4;
                            break;
                        case '-':
                            matched[0] = input.substring(matchStart, i);
                            state = 5;
                            break;
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                        case 'G':
                        case 'H':
                        case 'I':
                        case 'J':
                        case 'K':
                        case 'L':
                        case 'M':
                        case 'N':
                        case 'O':
                        case 'P':
                        case 'Q':
                        case 'R':
                        case 'S':
                        case 'T':
                        case 'U':
                        case 'V':
                        case 'W':
                        case 'X':
                        case 'Y':
                        case 'Z':
                        default:
                            return null;
                    }
                    break;
                case 4:
                    switch (c) {
                        case '-':
                            matched[1] = input.substring(matchStart, i);
                            state = 5;
                            break;
                        default:
                            return null;
                    }
                    break;
                case 5:
                    matchStart = i;
                case 6:
                case 7:
                case 8:
                    switch (c) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            state += 1;
                            break;
                        default:
                            return null;
                    }
                    break;
            }
        }
        if (state == 9) {
            matched[2] = input.substring(matchStart, i);
            return matched;
        }

        return null;
    }
}
