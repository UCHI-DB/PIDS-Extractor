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
import edu.uchicago.cs.db.subattr.ir.matcher.regex.GenRegexVisitor
import org.junit.Assert._
import org.junit.Test

class GenRegexVisitorTest {

  @Test
  def testGen: Unit = {

    val pattern = PSeq(Seq(
      new PIntAny(5, 5),
      new PToken(new TSymbol("-")),
      new PIntAny(4, 4)))

    pattern.visit(new NamingVisitor)

    val visitor = new GenRegexVisitor

    pattern.visit(visitor)

    assertEquals("^(\\d{5})-(\\d{4})$", visitor.get)
  }

  @Test
  def testManyAny: Unit = {
    val pattern = PSeq(Seq(
      new PIntAny(5, 7),
      new PToken(new TSymbol("-")),
      new PLetterAny(1, -1),
      new PToken(new TSymbol("-")),
      new PWordAny(0, -1),
      new PToken(new TSymbol("-")),
      new PLabelAny(2, -1),
    ))

    pattern.visit(new NamingVisitor)

    val visitor = new GenRegexVisitor

    pattern.visit(visitor)

    assertEquals("^(\\d{5,7})-([a-zA-Z]+)-([a-zA-Z0-9\\.\\- ]*)-(\\w{2,})$", visitor.get)
  }
}
