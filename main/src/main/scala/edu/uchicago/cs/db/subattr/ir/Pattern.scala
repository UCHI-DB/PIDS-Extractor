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

package edu.uchicago.cs.db.subattr.ir

import edu.uchicago.cs.db.subattr.extract.parser._
import edu.uchicago.cs.db.subattr.extract.rule._
import edu.uchicago.cs.db.subattr.ir.matcher.NamingVisitor

import scala.collection.mutable

/**
  * Created by harper on 3/16/17.
  */

object Pattern {


}

trait PatternVisitor {

  def on(ptn: Pattern): Unit

  def enter(container: Pattern): Unit

  def exit(container: Pattern): Unit
}

abstract class AbstractVisitor extends PatternVisitor {

  protected val path = new mutable.Stack[Pattern]

  def on(ptn: Pattern): Unit

  override def enter(container: Pattern): Unit = path.push(container)

  override def exit(container: Pattern): Unit = path.pop()
}


trait Pattern extends Serializable {
  var name = ""

  def getName = name

  /**
    * @return all leaf patterns
    */
  def flatten: Seq[Pattern] = Seq(this)

  /**
    * Recursively visit the pattern elements starting from the root
    *
    * @param visitor
    */
  def visit(visitor: AbstractVisitor): Unit = visitor.on(this)

  def naming() = visit(new NamingVisitor)

  def numChar: (Int, Int)
}

class PToken(t: Token) extends Pattern {
  val token = t

  override def numChar: (Int, Int) = (token.numChar, token.numChar)

  def canEqual(other: Any): Boolean = other.isInstanceOf[PToken]

  override def equals(other: Any): Boolean = other match {
    case that: PToken =>
      (that canEqual this) &&
        token == that.token
    case _ => false
  }

  override def hashCode(): Int = token.hashCode()

  override def toString = token.toString
}

object PSeq {

  def apply(content: Seq[Pattern]): Pattern = {
    val filtered = content.filter(_ != PEmpty)
    filtered.length match {
      case 0 => PEmpty
      case 1 => filtered.head
      case _ => new PSeq(filtered)
    }
  }

  def apply(content: Array[Pattern]): Pattern = {
    apply(content.toSeq)
  }

  def collect(content: Pattern*): Pattern = apply(content)
}

class PSeq(cnt: Seq[Pattern]) extends Pattern {
  val content = cnt.toList

  override def flatten: Seq[Pattern] = content.flatMap(_.flatten)

  override def visit(visitor: AbstractVisitor): Unit = {
    visitor.on(this)
    visitor.enter(this)
    content.foreach(_.visit(visitor))
    visitor.exit(this)
  }

  override def numChar: (Int, Int) = {
    content.map(_.numChar).reduce((a, b) => {
      (a._1 + b._1, a._2 + b._2)
    })
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[PSeq]

  override def equals(other: Any): Boolean = other match {
    case that: PSeq =>
      (that canEqual this) &&
        content == that.content
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(content)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = "<S>(%s)".format(content.map(_.toString).mkString(","))

}

object PUnion {

  def apply(content: Seq[Pattern]): Pattern = {
    content.toSet.size match {
      case 0 => PEmpty
      case 1 => content.head
      case _ => new PUnion(content)
    }
  }

  def apply(content: Array[Pattern]): Pattern = {
    apply(content.toSeq)
  }

  def collect(content: Pattern*): Pattern = apply(content)
}

class PUnion(cnt: Seq[Pattern]) extends Pattern {
  val content = cnt.toSet.toList

  override def flatten: Seq[Pattern] = content.flatMap(_.flatten)

  override def visit(visitor: AbstractVisitor): Unit = {
    visitor.on(this)
    visitor.enter(this)
    content.foreach(_.visit(visitor))
    visitor.exit(this)
  }

  override def numChar: (Int, Int) = {
    content.map(_.numChar).reduce((a, b) => {
      (Math.min(a._1, b._1), Math.max(a._2, b._2))
    })
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[PUnion]

  override def equals(other: Any): Boolean = other match {
    case that: PUnion =>
      (that canEqual this) &&
        content == that.content
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(content)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String = "<U>(%s)".format(content.map(_.toString).mkString(","))
}

object PEmpty extends Pattern {
  override def numChar: (Int, Int) = (0, 0)

  override def toString = "<empty>"
}

abstract class PAny(var minLength: Int, var maxLength: Int) extends Pattern {

  override def numChar: (Int, Int) = (minLength, maxLength)

  def canEqual(other: Any): Boolean = other.getClass.eq(this.getClass)

  override def equals(other: Any): Boolean = other match {
    case that: PAny =>
      (that canEqual this) &&
        minLength == that.minLength &&
        maxLength == that.maxLength
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(getClass.getSimpleName.hashCode, minLength, maxLength)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

class PIntAny(minl: Int = 1, maxl: Int = -1,
              var hasHex: Boolean = false) extends PAny(minl, maxl) {

  def this(limit: Int, hasHex: Boolean) = this(limit, limit, hasHex)

  def this(limit: Int) = this(limit, false)


  override def equals(other: Any): Boolean = other match {
    case that: PIntAny =>
      (that.canEqual(this)) &&
        minLength == that.minLength &&
        maxLength == that.maxLength &&
        hasHex == that.hasHex
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(super.hashCode(), minLength, maxLength, hasHex)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String = "<intany %d:%d, %s>".format(minLength, maxLength, hasHex)
}

class PLetterAny(minl: Int = 1, maxl: Int = -1)
  extends PAny(minl, maxl) {
  def this(ml: Int) = this(ml, ml)

  override def toString: String = "<letterany %d:%d>".format(minLength, maxLength)
}

/**
  * Mix of letter and digits
  *
  * @param minl
  * @param maxl
  */
class PLabelAny(minl: Int = 1, maxl: Int = -1)
  extends PAny(minl, maxl) {
  def this(ml: Int) = this(ml, ml)

  override def toString: String = "<labelany %d:%d>".format(minLength, maxLength)
}

/**
  * English words, including letter, digit, '.', '-', and space
  *
  * Note: we did not see a reason to separate phrase and word for now. So if a token
  * is mutated from label to words for any reason, e.g., label + '.' or label + '-',
  * it will automatically includes space, '.' and '-', even if the original sample
  * does not contain some of the symbol. If there is a reason to separate phrase
  * and word, we can move space out here and use PPhraseAny
  *
  * @param minl
  * @param maxl
  */
class PWordAny(minl: Int = 1, maxl: Int = -1)
  extends PAny(minl, maxl) {
  def this(limit: Int) = this(limit, limit)

  override def toString: String = "<wordany %d:%d>".format(minLength, maxLength)
}

/**
  * Not used for now. See <code>PWordAny</code>
  *
  * @param minl
  * @param maxl
  */
class PPhraseAny(minl: Int = 1, maxl: Int = -1)
  extends PAny(minl, maxl) {
  def this(limit: Int) = this(limit, limit)

  override def toString: String = "<phraseany %d:%d>".format(minLength, maxLength)
}

/**
  * As we are extracting patterns from a small subset, determining range from
  * that is prone to error
  *
  * @deprecated
  */
@deprecated
class PIntRange extends Pattern {
  var min: BigInt = BigInt(0)
  var max: BigInt = BigInt(0)

  def this(min: BigInt, max: BigInt) {
    this()
    this.min = min
    this.max = max
  }

  def this(min: Int, max: Int) {
    this(BigInt(min), BigInt(max))
  }

  override def hashCode(): Int = this.min.hashCode() * 13 + this.max.hashCode()

  override def equals(obj: scala.Any): Boolean = {
    if (eq(obj.asInstanceOf[AnyRef]))
      return true
    obj match {
      case range: PIntRange => {
        this.min == range.min && this.max == range.max
      }
      case _ => super.equals(obj)
    }
  }

  override def numChar: (Int, Int) = {
    val factor = Math.log(2) / Math.log(10)
    val digitCount = (factor * max.bitLength + 1).toInt
    if (BigInt(10).pow(digitCount - 1).compareTo(max) > 0)
      (digitCount - 1, digitCount - 1)
    else
      (digitCount, digitCount)
  }
}
