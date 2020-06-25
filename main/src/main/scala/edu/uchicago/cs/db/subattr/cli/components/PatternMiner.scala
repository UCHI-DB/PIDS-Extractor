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
 *
 */

package edu.uchicago.cs.db.subattr.cli.components

import java.net.URI

import com.google.common.base.CharMatcher
import edu.uchicago.cs.db.subattr.extract.parser.{Token, Tokenizer}
import edu.uchicago.cs.db.subattr.extract.rule._
import edu.uchicago.cs.db.subattr.ir.{PSeq, PToken, PUnion, Pattern}
import edu.uchicago.cs.db.subattr.util.FileUtils

import scala.io.Source
import scala.util.Random

/**
  * Created by harper on 3/25/17.
  */
class PatternMiner {

  val rules = Array(new CommonSymbolRule,
    new DictionaryWordRule,
    new SameLenMergeRule,
    new CommonSeqRule(CommonSeqEqualFunc.exactEquals _),
    new UseAnyRule, new SuccinctRule, new MergeGroupRule,
    new GeneralizeTokenRule, new ExpandBoundRule,
    new MergeWordRule)

  var sampleSize: Int = 500

  var seed: Long = System.currentTimeMillis()

  def mine(inputFile: URI): Pattern = {
    val nLine = FileUtils.numNonEmptyLine(inputFile)
    val rate = sampleSize.toDouble / nLine
    val source = Source.fromFile(inputFile)
    val rand = new Random(seed)
    try {
      val lines = source.getLines().map(_.trim).filter(l => l.nonEmpty && CharMatcher.ascii.matchesAllOf(l))
        .filter(p => rand.nextDouble() <= rate)
      if (lines.isEmpty) {
        return null
      }
      //    val tail = lines.takeRight(100)
      //    val both = head ++ tail
      val pattern = mine(lines.map(Tokenizer.tokenize(_).toSeq).toSeq)
      pattern.naming()
      pattern
    } finally {
      source.close
    }
  }


  def mine(in: Seq[Seq[Token]]): Pattern = {
    // Generate a direct pattern by translating tokens

    val translated = new PUnion(in.filter(_.nonEmpty).map(l => new PSeq(l.map(new PToken(_)))))

    rules.foreach(rule => {
      rule match {
        case data: DataRewriteRule => data.generateOn(in)
        case _ => Unit
      }
    })
    // Repeatedly refine the pattern using supplied rules
    var toRefine: Pattern = translated
    var needRefine = true
    var refineResult: Pattern = toRefine
    while (needRefine) {
      val refined = refine(toRefine)
      if (refined._2.isDefined) {
        toRefine = refined._1
      } else {
        needRefine = false
        refineResult = refined._1
      }
    }

    val validated = validate(refineResult)
    validated
  }

  protected def refine(root: Pattern): (Pattern, Option[RewriteRule]) = {
    var current = root

    rules.foreach(rule => {
      rule.reset
      current = rule.rewrite(current)
      if (rule.happened) {
        // Apply the first valid rule
        return (current, Some(rule))
      }
    })
    (root, None)
  }

  def validate(ptn: Pattern): Pattern = {
    ptn
  }

}

