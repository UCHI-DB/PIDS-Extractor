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

import edu.uchicago.cs.db.subattr.datagen.DataGen;
import edu.uchicago.cs.db.subattr.extract.parser.TSymbol;
import edu.uchicago.cs.db.subattr.ir.PIntAny;
import edu.uchicago.cs.db.subattr.ir.PSeq;
import edu.uchicago.cs.db.subattr.ir.PToken;
import edu.uchicago.cs.db.subattr.ir.Pattern;
import org.junit.Test;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MatcherPerf2Test {

    static final int[] lengths = new int[]{5, 10, 20, 50, 80};

    public static class Input {

        String[] data = new String[100];

        PawnMatcher pawnMatcher;

        RegexMatcher regexMatcher;

        ExactMatcher adhocMatcher;

        String[] buffer;

        public void setup(int length) {
            buffer = new String[length];
            for (int i = 0; i < data.length; i++) {
                data[i] = gen();
            }

            List<Pattern> content = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                content.add(new PIntAny(5, 5, false));
                if (i != length - 1)
                    content.add(new PToken(new TSymbol("-")));
            }

            Pattern pattern = new PSeq(JavaConverters.asScalaBuffer(content));

            pawnMatcher = new PawnMatcher(pattern);
            regexMatcher = new RegexMatcher(pattern);
            adhocMatcher = new AdhocMatcherGenerator().generate(pattern);
        }

        protected String gen() {
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = DataGen.randNum(5);
            }
            return String.join("-", buffer);
        }
    }

    protected Input input(int length) {
        Input input = new Input();
        input.setup(length);
        return input;
    }

    @Test
    public void regex() {
        for (int length : lengths) {
            Input input = input(length);
            for (String d : input.data) {
                assertEquals(length, input.regexMatcher.match(d).length);
            }
        }
    }

    @Test
    public void stateMachine() {
        for (int length : lengths) {
            Input input = input(length);
            for (String d : input.data) {
                assertEquals(length, input.pawnMatcher.match(d).length);
            }
        }
    }

    @Test
    public void adhoc() {
        for (int length : lengths) {
            Input input = input(length);
            for (String d : input.data) {
                assertEquals(d, length, input.adhocMatcher.match(d).length);
            }
        }
    }

}
