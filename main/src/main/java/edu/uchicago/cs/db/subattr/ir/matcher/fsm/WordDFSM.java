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

import edu.uchicago.cs.db.subattr.ir.matcher.MatcherHelper;

public class WordDFSM extends AbstractDFSM {


    public WordDFSM() {
        super();
    }

    @Override
    public int numStates() {
        return 2;
    }

    @Override
    public boolean accept(char c) {
        if (MatcherHelper.isLetter(c)) {
            state = 1;
            return true;
        }
        return false;
    }

    @Override
    public boolean done() {
        return state == 1;
    }

    int[][] AVAILABLE = new int[][]{
            new int[]{1}, new int[]{1}
    };

    @Override
    public int[] available() {
        return AVAILABLE[state];
    }
}
