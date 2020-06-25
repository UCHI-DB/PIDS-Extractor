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

package edu.uchicago.cs.db.subattr.ir.matcher

import edu.uchicago.cs.db.subattr.extract.parser.TSymbol
import edu.uchicago.cs.db.subattr.ir._
import org.junit.Assert._
import org.junit.Test

class AdhocMatcherTest {

  @Test
  def testMatchSimple: Unit = {

    val pattern: Pattern =
      PSeq(Seq(new PIntAny(3, 4), new PToken(new TSymbol("-")), new PIntAny(4, 4)))

    val matcher = new AdhocMatcherGenerator().generate(pattern);

    var matched = matcher.`match`("131-2245")
    assertTrue(!matched.isEmpty)
    var content = matched

    assertEquals(2, content.size)
    assertEquals("131", content(0))
    assertEquals("2245", content(1))


    matched = matcher.`match`("1314-2245")
    assertTrue(!matched.isEmpty)
    content = matched

    assertEquals(2, content.size)
    assertEquals("1314", content(0))
    assertEquals("2245", content(1))

    matched = matcher.`match`("12-1143")
    assertTrue(matched == null)

    matched = matcher.`match`("232-113")
    assertTrue(matched == null)
  }

  @Test
  def testMatchLongSeq: Unit = {

    val pattern: Pattern =
      PSeq(Seq(new PIntAny(3, 4), new PToken(new TSymbol("-")), new PIntAny(2, 5),
        new PToken(new TSymbol("~")), new PIntAny(3, 5)))

    val matcher = new AdhocMatcherGenerator().generate(pattern)

    var matched = matcher.`match`("131-2245~124")
    assertTrue(!matched.isEmpty)
    assertEquals(3, matched.size)
    assertEquals("131", matched(0))
    assertEquals("2245", matched(1))
    assertEquals("124", matched(2))

    matched = matcher.`match`("131-22~1243")
    assertTrue(!matched.isEmpty)
    assertEquals(3, matched.size)
    assertEquals("131", matched(0))
    assertEquals("22", matched(1))
    assertEquals("1243", matched(2))

    matched = matcher.`match`("1314-22424~124")
    assertTrue(!matched.isEmpty)
    assertEquals(3, matched.size)
    assertEquals("1314", matched(0))
    assertEquals("22424", matched(1))
    assertEquals("124", matched(2))

    matched = matcher.`match`("131-22424~124")
    assertTrue(!matched.isEmpty)
    assertEquals(3, matched.size)
    assertEquals("131", matched(0))
    assertEquals("22424", matched(1))
    assertEquals("124", matched(2))

    matched = matcher.`match`("131-224249~124")
    assertTrue(matched == null)
  }

  @Test
  def testMatchInfSeq: Unit = {

    val pattern: Pattern =
      PSeq(Seq(new PIntAny(3, -1), new PToken(new TSymbol("-")), new PIntAny(1, -1)))

    val matcher = new AdhocMatcherGenerator().generate(pattern)

    var matched = matcher.`match`("131-2245")
    assertTrue(!matched.isEmpty)
    var content = matched

    assertEquals(2, content.size)
    assertEquals("131", content(0))
    assertEquals("2245", content(1))


    matched = matcher.`match`("1314-2245")
    assertTrue(!matched.isEmpty)
    content = matched
    assertEquals(2, content.size)
    assertEquals("1314", content(0))
    assertEquals("2245", content(1))

    matched = matcher.`match`("12-1143")
    assertTrue(matched == null)

    matched = matcher.`match`("232-113")
    assertEquals(2, matched.size)
    assertEquals("232", matched(0))
    assertEquals("113", matched(1))
  }

  @Test
  def testWeirdCase: Unit = {
    val pattern: Pattern =
      PSeq(Seq(
        new PToken(new TSymbol("(")),
        new PIntAny(3, 6),
        PUnion(
          Seq(
            new PToken(new TSymbol(")")),
            PEmpty
          )),
        new PIntAny(0, 3),
        new PToken(new TSymbol("-")),
        new PIntAny(4, 4)
      ))
    val amgen = new AdhocMatcherGenerator()
    val matcher = amgen.generate(pattern);


    val matched = matcher.`match`("(323)242-2411")
    assertEquals(4, matched.length)
    assertEquals("323", matched(0))
    assertEquals(")", matched(1))
    assertEquals("242", matched(2))
    assertEquals("2411", matched(3))

    val matched2 = matcher.`match`("(323242-2411")
    assertEquals(4, matched2.length)
    assertEquals("323242", matched2(0))
    assertEquals("", matched2(1))
    assertEquals("", matched2(2))
    assertEquals("2411", matched2(3))

  }
}
