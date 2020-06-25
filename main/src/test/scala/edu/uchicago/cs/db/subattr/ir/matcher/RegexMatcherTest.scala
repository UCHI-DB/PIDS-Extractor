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
import edu.uchicago.cs.db.subattr.ir.{PIntAny, PSeq, PToken, Pattern}
import org.junit.Assert._
import org.junit.Test

class RegexMatcherTest {
  @Test
  def testMatchSimple: Unit = {

    val pattern: Pattern =
      PSeq(Seq(new PIntAny(3, 4), new PToken(new TSymbol("-")), new PIntAny(4, 4)))

    val matcher = new RegexMatcher(pattern)

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

}
