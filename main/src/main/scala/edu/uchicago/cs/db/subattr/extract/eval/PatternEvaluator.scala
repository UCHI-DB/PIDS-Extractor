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

package edu.uchicago.cs.db.subattr.extract.eval

import edu.uchicago.cs.db.subattr.ir.matcher.{ExactMatcher, RegexMatcher}
import edu.uchicago.cs.db.subattr.ir.{PAny, Pattern}
import org.apache.commons.lang3.StringUtils

/**
  * <code>PatternEvaluator</code> evaluates a given pattern on a dataset to
  * determine its efficiency based on the pattern size + encoded data size
  *
  */
object PatternEvaluator {

  var matcher: ExactMatcher = null

  def evaluate(ptn: Pattern, dataset: Traversable[String]): Double = {

    matcher = new RegexMatcher(ptn)

    if (StringUtils.isEmpty(ptn.getName))
      ptn.naming()

    // Pattern Size
    val sizeVisitor = new SizeVisitor
    ptn.visit(sizeVisitor)
    val ptnSize = sizeVisitor.size

    val anyIndex = ptn.flatten.zipWithIndex.
      filter(_._1.isInstanceOf[PAny]).map(_._2)

    // Encoded Data Size
    val matched = dataset.map(di => (matcher.`match`(di), di))

    //    val encodedSize = matched.map(item => {
    //      val record = item._1
    //      val origin = item._2
    //      record match {
    //        case Some(content) => {
    //          val unionSel = content.choices.values
    //            .map(x => intSize(x._2)).sum
    //          val anys = anyIndex.map(idx => {
    //            content(idx) match {
    //              case "" => 0
    //              case a => a.length
    //            }
    //          })
    //          val intRange = content.rangeDeltas.values.map(intSize).sum
    //          unionSel + (anys.isEmpty match {
    //            case true => 0
    //            case false => anys.sum + anys.size
    //          }) + intRange
    //        }
    //        case None => origin.length
    //      }
    //    })
    //
    //    ptnSize + encodedSize.sum
    throw new UnsupportedOperationException
  }

  def intSize(input: Int): Int = Math.ceil(Math.log(input) / (8 * Math.log(2))).toInt

  def intSize(input: BigInt): Int = Math.ceil(Math.log(input.doubleValue()) / (8 * Math.log(2))).toInt
}
