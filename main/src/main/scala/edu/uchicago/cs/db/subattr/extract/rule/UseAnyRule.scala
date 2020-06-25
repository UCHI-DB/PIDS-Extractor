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

package edu.uchicago.cs.db.subattr.extract.rule

import edu.uchicago.cs.db.subattr.extract.parser.{TDouble, TInt, TWord}
import edu.uchicago.cs.db.subattr.ir._

import scala.collection.mutable.ArrayBuffer

/**
  * Use <code>PAny</code> to replace big Union of tokens
  *
  * TInt -> PIntAny
  * TWord -> PLetterAny
  * Mixture -> PLabelAny
  */
object UseAnyRule {
  // Execute the rule if union size is greater than threshold * data size
  val threshold = 0.00000
  val hardThreshold = 2
}

class UseAnyRule extends DataRewriteRule {

  override def condition(ptn: Pattern): Boolean = {
    val qualifiedUnion = ptn.isInstanceOf[PUnion] && {
      val union = ptn.asInstanceOf[PUnion]
      union.content.length >= Math.max(UseAnyRule.hardThreshold, UseAnyRule.threshold * originData.length) &&
        union.content.view.forall(p => p.isInstanceOf[PToken] || p == PEmpty)
    }
    qualifiedUnion
  }


  override protected def update(ptn: Pattern): Pattern = {
    val union = ptn.asInstanceOf[PUnion]
    val hasEmpty = union.content.contains(PEmpty)
    val anyed = union.content.view.filter(_ != PEmpty).map(
      _ match {
        case token: PToken => {
          token.token match {
            case word: TWord => new PLetterAny(word.numChar)
            case int: TInt => new PIntAny(int.numChar, int.isHex)
            case other => token
          }
        }
        case other => other
      }
    ).groupBy(_.getClass)

    val shrinked = anyed.filterKeys(classOf[PAny].isAssignableFrom(_)).map(kv => {
      kv._1 match {
        case wa if wa == classOf[PLetterAny] => {
          kv._2.reduce((a, b) => {
            val aw = a.asInstanceOf[PLetterAny]
            val bw = b.asInstanceOf[PLetterAny]
            new PLetterAny(Math.min(aw.minLength, bw.minLength),
              Math.max(aw.maxLength, bw.maxLength))
          })
        }
        case ia if ia == classOf[PIntAny] => {
          kv._2.reduce((a, b) => {
            val ai = a.asInstanceOf[PIntAny]
            val bi = b.asInstanceOf[PIntAny]
            new PIntAny(Math.min(ai.minLength, bi.minLength),
              Math.max(ai.maxLength, bi.maxLength),
              ai.hasHex || bi.hasHex)
          })
        }
        case _ => throw new IllegalArgumentException
      }
    }).toList
    val remain = anyed.filterKeys(!classOf[PAny].isAssignableFrom(_)).values.flatten.toList
    if (!shrinked.isEmpty) {
      val any = shrinked.size match {
        case 1 => shrinked.head
        case 2 => {
          // word merge with int to become label
          val first = shrinked(0).asInstanceOf[PAny]
          val second = shrinked(1).asInstanceOf[PAny]
          new PLabelAny(Math.min(first.minLength, second.minLength), Math.max(first.maxLength, second.maxLength))
        }
      }
      if (any != null) {
        happen()
        if (remain.isEmpty) {
          hasEmpty match {
            case true => new PUnion(Seq(any, PEmpty))
            case false => any
          }
        } else {
          val content = new ArrayBuffer[Pattern]
          content += any
          content ++= remain
          if (hasEmpty)
            content += PEmpty
          new PUnion(content)
        }
      } else
        union
    } else {
      union
    }
  }
}
