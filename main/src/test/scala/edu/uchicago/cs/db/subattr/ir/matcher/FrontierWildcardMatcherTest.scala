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

class FrontierWildcardMatcherTest {

  @Test
  def testMatchSimple: Unit = {

    val pattern: Pattern =
      PSeq(Seq(new PIntAny(3, 4), new PToken(new TSymbol("-")), new PIntAny(4, 4)))

    val matcher = new FrontierWildcardMatcher(pattern)

    var matched = matcher.`match`("131-2245")
    assertEquals(1, matched.size())
    var content = matched.get(0)

    assertEquals(2, content.length)
    assertEquals("131", content(0))
    assertEquals("2245", content(1))


    matched = matcher.`match`("1314-2245")
    assertEquals(1, matched.size())
    content = matched.get(0)

    assertEquals(2, content.length)
    assertEquals("1314", content(0))
    assertEquals("2245", content(1))

    matched = matcher.`match`("12-1143")
    assertTrue(matched.isEmpty)

    matched = matcher.`match`("232-113")
    assertTrue(matched.isEmpty)
  }

  @Test
  def testMatchLongSeq: Unit = {

    val pattern: Pattern =
      PSeq(Seq(new PIntAny(3, 4), new PToken(new TSymbol("-")), new PIntAny(2, 5),
        new PToken(new TSymbol("~")), new PIntAny(3, 5)))

    val matcher = new FrontierWildcardMatcher(pattern)

    var matched = matcher.`match`("131-2245~124")
    assertEquals(1, matched.size())
    var content = matched.get(0)
    assertEquals(3, content.length)
    assertEquals("131", content(0))
    assertEquals("2245", content(1))
    assertEquals("124", content(2))

    matched = matcher.`match`("131-22~1243")
    assertEquals(1, matched.size())
    content = matched.get(0)
    assertEquals(3, content.length)
    assertEquals("131", content(0))
    assertEquals("22", content(1))
    assertEquals("1243", content(2))

    matched = matcher.`match`("1314-22424~124")
    assertEquals(1, matched.size())
    content = matched.get(0)
    assertEquals(3, content.length)
    assertEquals("1314", content(0))
    assertEquals("22424", content(1))
    assertEquals("124", content(2))

    matched = matcher.`match`("131-22424~124")
    assertEquals(1, matched.size())
    content = matched.get(0)
    assertEquals(3, content.length)
    assertEquals("131", content(0))
    assertEquals("22424", content(1))
    assertEquals("124", content(2))

    matched = matcher.`match`("131-224249~124")
    assertTrue(matched.isEmpty)
  }

  @Test
  def testSingleWildcard(): Unit = {
    val pattern: Pattern =
      PSeq(Seq(new PIntAny(3, 4), new PToken(new TSymbol("-")), new PIntAny(4, 4)))

    val matcher = new FrontierWildcardMatcher(pattern)

    var matched = matcher.`match`("13%")
    assertEquals(1, matched.size())
    var content = matched.get(0)
    assertEquals("13%", content(0))
    assertEquals("%", content(1))

    matched = matcher.`match`("132%")
    assertEquals(1, matched.size())
    content = matched.get(0)
    assertEquals("132%", content(0))
    assertEquals("%", content(1))

    //    content = matched.get(1)
    //    assertEquals("132%", content(0))
    //    assertEquals("%", content(1))

    matched = matcher.`match`("132-%")
    assertEquals(1, matched.size())
    content = matched.get(0)
    assertEquals("132", content(0))
    assertEquals("%", content(1))

    matched = matcher.`match`("132-%")
    assertEquals(1, matched.size())
    content = matched.get(0)
    assertEquals("132", content(0))
    assertEquals("%", content(1))

    matched = matcher.`match`("132-31%")
    assertEquals(1, matched.size())
    content = matched.get(0)
    assertEquals("132", content(0))
    assertEquals("31%", content(1))

    matched = matcher.`match`("132-3149%")
    assertEquals(1, matched.size())
    content = matched.get(0)
    assertEquals("132", content(0))
    assertEquals("3149", content(1))
  }

  @Test
  def testMultiWildcard(): Unit = {
    val pattern: Pattern =
      PSeq(Seq(new PIntAny(3, 3), new PToken(new TSymbol("-")), new PIntAny(3, 3),
        new PToken(new TSymbol("-")), new PIntAny(4, 4)))

    val matcher = new FrontierWildcardMatcher(pattern)

    var matched = matcher.`match`("13%4%")
    assertEquals(3, matched.size())
    var content = matched.get(0)
    assertEquals("134", content(0))
    assertEquals("%", content(1))
    assertEquals("%", content(2))

    content = matched.get(1)
    assertEquals("13%", content(0))
    assertEquals("%4%", content(1))
    assertEquals("%", content(2))

    content = matched.get(2)
    assertEquals("13%", content(0))
    assertEquals("%", content(1))
    assertEquals("%4%", content(2))

  }

  @Test
  def testComplexWildcard(): Unit = {
    val pattern: Pattern =
      PSeq(Seq(new PIntAny(0, 4), new PToken(new TSymbol("#")), new PWordAny(0, 20),
        new PToken(new TSymbol("-")), new PWordAny(0, 20), new PToken(new TSymbol("!")), new PWordAny(0, 20),
        new PToken(new TSymbol("*")), new PWordAny(0, 10)))

    val matcher = new FrontierWildcardMatcher(pattern)
    var matched = matcher.`match`("%!ppa*%")
    assertEquals(1, matched.size())
  }

  @Test
  def testAddressWildcard: Unit = {
    val pattern = PSeq(Seq(
      new PIntAny(0, 4, false),
      new PToken(new TSymbol("#")),
      //      new PWordAny(0, 2),
      //      new PToken(new TSymbol("$")),
      //      new PWordAny(0, 3),
      //      new PToken(new TSymbol(",Suite ")),
      //      new PWordAny(0, 3),
      //      new PToken(new TSymbol("&")),
      new PWordAny(0, 2),
      new PToken(new TSymbol("+")),
      new PWordAny(0, 2),
      new PToken(new TSymbol("*")),
      new PWordAny(0, 2),
      new PToken(new TSymbol("!")),
      new PIntAny(0, 5, false)))

    val matcher = new FrontierWildcardMatcher(pattern)
    var matched = matcher.`match`("%*TN!%")
    assertEquals(1, matched.size())
    val m = matched.get(0)
    assertEquals("%", m(0))
    assertEquals("%", m(1))
    assertEquals("%", m(2))
    assertEquals("TN", m(3))
    assertEquals("%", m(4))
  }

  @Test
  def testSupportWordAndLabel: Unit = {
    val pattern = PSeq(Seq(
      new PIntAny(0, 4, false),
      new PToken(new TSymbol(",")),
      new PWordAny(2, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(2, -1)
    ))

    val matcher = new FrontierWildcardMatcher(pattern)
    val matched = matcher.`match`("313,Kimbark%")
    assertEquals(1,matched.size())
    val m = matched.get(0)

    assertEquals("313",m(0))
    assertEquals("Kimbark%",m(1))
  }
}
