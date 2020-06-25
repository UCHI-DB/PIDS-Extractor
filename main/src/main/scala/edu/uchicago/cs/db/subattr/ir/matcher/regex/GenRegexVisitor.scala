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

package edu.uchicago.cs.db.subattr.ir.matcher.regex

import edu.uchicago.cs.db.subattr.extract.parser.TSpace
import edu.uchicago.cs.db.subattr.ir._

import scala.collection.mutable.{ArrayBuffer, HashMap}

/**
  * Generate a regular expression string that contains the given pattern.
  * Some patterns, e.g., <code>PIntRange</code> cannot be represented
  * exactly by regular expressions, in that case, we will use the superset
  * to represent it
  *
  */

object RegexHelper {
  def escape(input: String): String = {
    input match {
      // [\^$.|?*+(){}
      case "[" | "\\" | "^" | "$" | "." | "|" | "?" | "*" | "+" | "(" | ")" | "{" | "}" => "\\%s".format(input)
      case _ => input
    }
  }

  def isEscape(input: Char): Boolean = {
    "[\\^$.|?*+(){}".contains(input)
  }
}

class GenRegexVisitor extends AbstractVisitor {

  private val history = new HashMap[String, String]

  // The pattern names by the order of appearance
  // Note: this only covers those Patterns having variable content
  val list = new ArrayBuffer[String]

  def reset: Unit = {
    history.clear
    list.clear
  }

  // No partial matching
  def get: String = {
    history.size match {
      case 0 => "^.*$" // not
      case _ => "^%s$".format(history.head._2)
    }
  }

  override def on(ptn: Pattern): Unit = {
    ptn match {
      case union: PUnion => list += union.name
      case token: PToken => {
        history.put(token.name,
          if (token.token.isInstanceOf[TSpace]) {
            "\\s+"
          } else {
            RegexHelper.escape(token.token.value)
          })
        //history.put(token.name, RegexHelper.escape(token.token.value))
      }
      case lany: PLetterAny => {
        list += lany.name
        history.put(lany.name, boundedRegex("[a-zA-Z]", lany.minLength, lany.maxLength))
      }
      case iany: PIntAny => {
        list += iany.name
        val digit = iany.hasHex match {
          case false => "\\d"
          case true => "[0-9a-fA-F]"
        }
        history.put(iany.name, boundedRegex(digit, iany.minLength, iany.maxLength))
      }
      case lany: PLabelAny => {
        list += lany.name
        history.put(lany.name, boundedRegex("\\w", lany.minLength, lany.maxLength))
      }
      case wdany: PWordAny => {
        list += wdany.name
        history.put(wdany.name, boundedRegex("[a-zA-Z0-9\\.\\- ]", wdany.minLength, wdany.maxLength))
      }
      case irng: PIntRange => {
        list += irng.name
        // Use PIntAny instead
        history.put(irng.name, "(\\d+)")
      }
      case _ => {}
    }
  }

  override def exit(ptn: Pattern): Unit = {
    super.exit(ptn)
    ptn match {
      case union: PUnion => {
        var result = union.content.filter(_ != PEmpty).map(n => history.getOrElse(n.name, "<err>"))
          .reduce((a, b) => "%s|%s".format(a, b))
        if (union.content.contains(PEmpty)) {
          result = "(%s)?".format(result)
        } else {
          result = "(%s)".format(result)
        }

        union.content.foreach(n => history.remove(n.name))
        history.put(union.name, result)
      }
      case seq: PSeq => {
        val res = seq.content.filter(_ != PEmpty)
          .map(n => history.getOrElse(n.name, "<err>")).mkString
        seq.content.foreach(n => history.remove(n.name))
        history.put(seq.name, res)
      }
      case _ => {}
    }
  }

  private def boundedRegex(digit: String, min: Int, max: Int): String = {
    "(%s)".format((min, max) match {
      case (0, -1) => "%s*".format(digit)
      case (1, -1) => "%s+".format(digit)
      case (i, -1) => "%s{%d,}".format(digit, i)
      case (i, j) if i == j => "%s{%d}".format(digit, i)
      case (i, j) => "%s{%d,%d}".format(digit, i, j)
    })
  }
}
