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

package edu.uchicago.cs.db.subattr.cli.components

import edu.uchicago.cs.db.subattr.extract.parser._
import edu.uchicago.cs.db.subattr.ir._
import org.junit.Assert._
import org.junit.Test

import scala.io.Source

/**
  * Created by harper on 4/4/17.
  */
class PatternMinerTest {

  @Test
  def testMineFromRealData1: Unit = {
    val resource = Thread.currentThread().getContextClassLoader.getResourceAsStream("pattern/realdata1")
    val input = Source.fromInputStream(resource)
      .getLines().map(Tokenizer.tokenize(_).toSeq).toSeq
    val pm = new PatternMiner
    val pattern = pm.mine(input)

    val expected = PSeq.collect(
      new PLabelAny(3),
      new PToken(new TSymbol("-")),
      new PLabelAny(1, -1),
      PUnion.collect(
        new PToken(new TSymbol("-")),
        PEmpty
      ),
      new PIntAny(0, -1, true),
      PUnion.collect(
        new PToken(new TSymbol("-")),
        PEmpty
      ),
      new PIntAny(0, -1),
      PUnion.collect(
        new PToken(new TSymbol("-")),
        PEmpty
      ),
      new PIntAny(0, -1)
    )
    assertEquals(expected, pattern)
  }

  @Test
  def testMineFromRealData2: Unit = {
    val resource = Thread.currentThread().getContextClassLoader.getResourceAsStream("pattern/realdata2")
    val input = Source.fromInputStream(resource)
      .getLines().map(Tokenizer.tokenize(_).toSeq).toSeq
    val pm = new PatternMiner
    val pattern = pm.mine(input)

    val expected = PSeq.collect(
      new PIntAny(4),
      new PToken(new TSymbol("-")),
      new PIntAny(2),
      new PToken(new TSymbol("-")),
      new PIntAny(2),
      new PToken(new TSpace),
      new PIntAny(2),
      new PToken(new TSymbol(":")),
      new PIntAny(2),
      new PToken(new TSymbol(":")),
      new PIntAny(2, false),
      new PToken(new TSymbol(".")),
      new PIntAny(6, false)
    )

    assertEquals(expected, pattern)
  }

  @Test
  def testMineAddress: Unit = {
    val resource = Thread.currentThread().getContextClassLoader.getResourceAsStream("pattern/sample_address")
    val input = Source.fromInputStream(resource)
      .getLines().map(Tokenizer.tokenize(_).toSeq).toSeq
    val pm = new PatternMiner
    val pattern = pm.mine(input)

    val expected = PSeq.collect(
      new PIntAny(1, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(2, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(1, -1),
      new PToken(new TSymbol(",")),
      new PToken(new TWord("Suite")),
      new PToken(new TSpace()),
      new PLabelAny(1, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(1, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(3, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(2),
      new PToken(new TSymbol(",")),
      new PIntAny(5)
    )

    assertEquals(expected, pattern)
  }

  @Test
  def testGenerate: Unit = {
    val input = Source.fromInputStream(Thread.currentThread().getContextClassLoader
      .getResourceAsStream("pattern/pattern_miner_sample")).getLines().toSeq

    val pattern = new PatternMiner().mine(input.map(Tokenizer.tokenize(_).toSeq))

    assertTrue(pattern.isInstanceOf[PSeq])
    val seq = pattern.asInstanceOf[PSeq]
    assertEquals(5, seq.content.size)

    assertEquals(new PIntAny(1, -1), seq.content.head)
    assertEquals(new PToken(new TSymbol("-")), seq.content(1))
    assertEquals(new PIntAny(4), seq.content(2))
    assertEquals(new PToken(new TSymbol("-")), seq.content(3))
    assertEquals(new PLetterAny(4), seq.content(4))
  }

  @Test
  def testGenerateOnEmpty: Unit = {
    val input = (1 to 10).map(i => i % 2 match {
      case 1 => "abc-123"
      case 0 => "abc-"
    })

    val pattern = new PatternMiner().mine(input.map(Tokenizer.tokenize(_).toSeq))
    assertEquals(
      PSeq(Seq(
        new PToken(new TWord("abc")),
        new PToken(new TSymbol("-")),
        new PIntAny(0, -1))),
      pattern)
  }

  @Test
  def testGenerate2: Unit = {
    val source = Source.fromInputStream(Thread.currentThread().getContextClassLoader
      .getResourceAsStream("pattern/sample_address"))
    val input = source.getLines().toSeq

    val pattern = new PatternMiner().mine(input.map(Tokenizer.tokenize(_).toSeq))

    assertTrue(pattern.isInstanceOf[PSeq])
    val seq = pattern.asInstanceOf[PSeq]
    assertEquals(17, seq.content.size)

    assertEquals(new PIntAny(1, -1), seq.content(0))
    assertEquals(new PToken(new TSymbol(",")), seq.content(1))
    assertEquals(new PWordAny(2,-1), seq.content(2))
    assertEquals(new PToken(new TSymbol(",")), seq.content(3))
    assertEquals(new PWordAny(1,-1), seq.content(4))
    source.close()
  }
}
