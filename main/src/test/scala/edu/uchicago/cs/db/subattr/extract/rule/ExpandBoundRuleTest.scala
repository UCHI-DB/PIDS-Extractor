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
 * under the License.
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 */

package edu.uchicago.cs.db.subattr.extract.rule

import edu.uchicago.cs.db.subattr.extract.parser._
import edu.uchicago.cs.db.subattr.ir._
import org.junit.Assert._
import org.junit.Test


class ExpandBoundRuleTest {

  @Test
  def testNotEnoughVariance: Unit = {
    val input = PSeq.collect(
      new PIntAny(1),
      new PToken(new TSymbol("/")),
      new PIntAny(1, 2),
      new PToken(new TSymbol("/")),
      new PWordAny(4),
      new PToken(new TSpace),
      new PIntAny(2),
      new PToken(new TSymbol(":")),
      new PLabelAny(2)
    )
    val rule = new ExpandBoundRule
    val output = rule.rewrite(input)

    assertTrue(rule.happened)

    val outseq = output.asInstanceOf[PSeq]
    assertEquals(9, outseq.content.size)

    val expect = PSeq.collect(
      new PIntAny(1),
      new PToken(new TSymbol("/")),
      new PIntAny(1, -1),
      new PToken(new TSymbol("/")),
      new PWordAny(4),
      new PToken(new TSpace),
      new PIntAny(2),
      new PToken(new TSymbol(":")),
      new PLabelAny(2)
    )
    assertEquals(expect, output)
  }

  @Test
  def testHappen: Unit = {
    val input = PSeq.collect(
      new PIntAny(1),
      new PToken(new TSymbol("/")),
      new PIntAny(1, 10),
      new PToken(new TSymbol("/")),
      new PWordAny(4),
      new PToken(new TSpace),
      new PIntAny(2),
      new PToken(new TSymbol(":")),
      new PLabelAny(2)
    )
    val rule = new ExpandBoundRule
    val output = rule.rewrite(input)

    assertTrue(rule.happened)

    val outseq = output.asInstanceOf[PSeq]
    assertEquals(9, outseq.content.size)

    val expect = PSeq.collect(
      new PIntAny(1),
      new PToken(new TSymbol("/")),
      new PIntAny(1, -1),
      new PToken(new TSymbol("/")),
      new PWordAny(4),
      new PToken(new TSpace),
      new PIntAny(2),
      new PToken(new TSymbol(":")),
      new PLabelAny(2)
    )
    assertEquals(expect, output)


    // Stop after one application
    rule.reset
    rule.rewrite(output)
    assertFalse(rule.happened)
  }


  @Test
  def testNotHappen: Unit = {
    val input = PSeq.collect(
      new PIntAny(1),
      new PIntAny(1, 2),
      new PWordAny(4),
      new PIntAny(2),
      new PLabelAny(2)
    )
    val rule = new ExpandBoundRule
    rule.rewrite(input)
    assertFalse(rule.happened)
  }
}
