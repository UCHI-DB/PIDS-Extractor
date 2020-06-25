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

import scala.collection.mutable.ArrayBuffer


/**
  * Look for common sequences from a list of tokens
  *
  */
object CommonSeq {
  val DEFAULT_SEQ_LENGTH = 1
  // Percentage that a common sequence is not in some sentence
  // TODO: the tolerance is not supported now
  val DEFAULT_TOLERANCE = 0.1
}

class CommonSeq(val seqLength: Int = CommonSeq.DEFAULT_SEQ_LENGTH,
                val tolerance: Double = CommonSeq.DEFAULT_TOLERANCE) {

  implicit def bool2int(b: Boolean) = if (b) 1 else 0

  // positions marks the corresponding location of the returned common sequence in each input
  var positions = new ArrayBuffer[Seq[Int]]

  /**
    * Look for common sequence in a list of lines. For implementation
    * simplicity, only the longest common seq is returned
    *
    * @param lines
    * @return common sequences
    */
  def find[T](lines: Seq[Seq[T]],
              equal: (T, T) => Boolean = (a: T, b: T) => a.equals(b),
              scoring: (T) => Int = (_: T) => 1): Seq[T] = {
    positions.clear

    var commons = lines.head

    if (lines.length == 1) {
      positions += (0 until commons.length)
      return commons
    }
    var firstLine = true
    lines.drop(1).foreach(line => {
      if (commons.nonEmpty) {

        // val commonsBetween = commons.map(between(_, line, equal))
        val commonsBetween = between(commons, line, equal, scoring)

        // Update previous positions
        // Split the positions
        if (firstLine) {
          // For the first and second lines
          positions += commonsBetween.flatMap(pair => (pair._1 until pair._1 + pair._3))
          firstLine = false
        } else {
          positions = positions.map(pos => {
            commonsBetween.flatMap(pair => pos.slice(pair._1, pair._1 + pair._3))
          })
        }
        positions += commonsBetween.flatMap(pair => (pair._2 until pair._2 + pair._3))
        commons = commonsBetween.flatMap(pair => commons.slice(pair._1, pair._1 + pair._3))
      }
    })
    commons
  }

  /**
    * Find Common sub-sequence in two sequences
    *
    * This method will find the longest subsequence with minimal separations.
    * E.g., if there are two candidates (0,1,2,3) and ((-1,4) (8,5)).
    * Although both has size 4, the first one will be chosen as it has no separation.
    *
    * @param a     the first sequence
    * @param b     the second sequence
    * @param equal equality test function
    * @return sequence of common symbols with length >= <code>sequence_length</code>.
    *         (a_start, b_start, length)
    */
  def between[T](a: Seq[T], b: Seq[T],
                 equal: (T, T) => Boolean,
                 scoring: (T) => Int): Seq[(Int, Int, Int)] = {
    if (a.isEmpty || b.isEmpty)
      return Seq[(Int, Int, Int)]()
    // data: (length of common seq end here, total score)
    val data = Array.fill(a.length + 1)(Array.fill(b.length + 1)(0, 0))
    val longest = Array.fill(a.length + 1)(Array.fill(b.length + 1)((0, 0)))
    val path = Array.fill(a.length + 1)(new Array[(Int, Int, Int)](b.length + 1))

    (0 to a.length).foreach(i => {
      data(i)(0) = (0, 0)
      longest(i)(0) = (0, 0)
      path(i)(0) = (i, 0, 0)
    })
    (0 to b.length).foreach(i => {
      data(0)(i) = (0, 0)
      longest(0)(i) = (0, 0)
      path(0)(i) = (0, i, 0)
    })

    // Find the longest common in order subsequences

    // Ways to construct a longest subsequence
    // 1. Use the current sequence end at (i,j) of length l, together with L(i-l,j-l)
    // 2. Do not use the current sequence at (i,j), use max(L(i,j-1), L(i-1,j))
    for (i <- 1 to a.length; j <- 1 to b.length) {
      data(i)(j) = equal(a(i - 1), b(j - 1)) match {
        case true => {
          val old = data(i - 1)(j - 1)
          (old._1 + 1, old._2 + scoring(a(i - 1)))
        }
        case false => (0, 0)
      }

      val currentdata = data(i)(j)

      val l1 = currentdata._1 match {
        case 0 => (-1, 0)
        case l => {
          val prev = longest(i - l)(j - l)
          (prev._1 + currentdata._2, prev._2 + 1)
        }
      }
      val l2 = longest(i - 1)(j)
      val l3 = longest(i)(j - 1)
      longest(i)(j) = Array(l1, l2, l3).maxBy(a => (a._1, -a._2))
      // Record path
      longest(i)(j) match {
        case c2 if c2 == l2 => path(i)(j) = (i - 1, j, 0)
        case c3 if c3 == l3 => path(i)(j) = (i, j - 1, 0)
        case c1 if c1 == l1 => path(i)(j) = (i - currentdata._1, j - currentdata._1, currentdata._1)
      }
    }

    val result = new ArrayBuffer[(Int, Int, Int)]()

    // Construct the longest sequence from path
    var i = a.length
    var j = b.length
    while (i != 0 && j != 0) {
      val step = path(i)(j)
      if (step._3 != 0)
        result += step
      i = step._1
      j = step._2
    }
    if (path(i)(j)._3 != 0)
      result += path(i)(j)
    result.reverse
  }
}
