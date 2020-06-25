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

import edu.uchicago.cs.db.subattr.extract.parser.TSymbol;
import edu.uchicago.cs.db.subattr.ir.PIntAny;
import edu.uchicago.cs.db.subattr.ir.PSeq;
import edu.uchicago.cs.db.subattr.ir.PToken;
import edu.uchicago.cs.db.subattr.ir.Pattern;
import edu.uchicago.cs.db.subattr.ir.matcher.adhoc.AdhocMatcherLongStepExample;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MatcherPerfTest {

    public static void main(String[] args) {

        List<Pattern> content = new ArrayList<>();
        content.add(new PIntAny(3, 4, false));
        content.add(new PToken(new TSymbol("-")));
        content.add(new PIntAny(4, 4, false));
        content.add(new PToken(new TSymbol("-")));

        Pattern pattern = new PSeq(JavaConverters.asScalaBuffer(content));

//        ExactMatcher matcher1 = new NaiveMatcherJavaVer(pattern);
//        ExactMatcher matcher1 = new AdhocMatcherExample();
//        ExactMatcher matcher1 = new AdhocMatcherGenerator().generate(pattern);
        ExactMatcher matcher1 = new AdhocMatcherLongStepExample();

        Random random = new Random(System.currentTimeMillis());
//        String s = String.format("%d-%d", Math.abs(random.nextInt() % 1000),
//                Math.abs(random.nextInt() % 10000));

        String s = "1414-2491";
//        System.out.println(matcher1.match(s));
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            matcher1.match(s);
        }
        System.out.println(System.currentTimeMillis() - start);

    }



}
