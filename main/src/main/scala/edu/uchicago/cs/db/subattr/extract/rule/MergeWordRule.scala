package edu.uchicago.cs.db.subattr.extract.rule

import edu.uchicago.cs.db.subattr.extract.parser.{TSpace, TSymbol}
import edu.uchicago.cs.db.subattr.ir.{PAny, PEmpty, PSeq, PToken, PUnion, PWordAny, Pattern}

import scala.collection.mutable.ListBuffer

/**
  * Merge adjacent PWordAny in a PSeq together
  * PWordAny will eagerly merge with the following patterns:
  * PWordAny, PIntAny, PLabelAny, Symbol('.'), Symbol('-'), Space, and Unions that contains only these
  */
class MergeWordRule extends RewriteRule {

  override protected def condition(ptn: Pattern): Boolean = {
    ptn.isInstanceOf[PSeq] && ptn.asInstanceOf[PSeq].content.exists(_.isInstanceOf[PWordAny])
  }

  override protected def update(ptn: Pattern): Pattern = {
    val seq = ptn.asInstanceOf[PSeq]

    val list = new ListBuffer[Pattern]
    val buffer = new ListBuffer[Pattern]
    var mergeTo: PWordAny = null
    seq.content.foreach(child => {
      // Non mergeable items serve as separator
      if (!isMergeCandidate(child)) {
        list ++= buffer
        buffer.clear()
        if (mergeTo != null)
          list += mergeTo
        mergeTo = null
        list += child
      } else {
        if (mergeTo != null) {
          // Merge new token
          mergeTo = merge(mergeTo, child)
        } else if (child.isInstanceOf[PWordAny]) {
          mergeTo = child.asInstanceOf[PWordAny]
          // Merge things in buffer
          buffer.foreach(p => mergeTo = merge(mergeTo, p))
          buffer.clear
        } else {
          buffer += child
        }
      }
    })
    list ++= buffer
    buffer.clear()
    if (mergeTo != null)
      list += mergeTo

    if (happened) {
      PSeq(list)
    } else {
      seq
    }
  }

  def isMergeCandidate(ptn: Pattern): Boolean = {
    ptn match {
      case token: PToken => {
        val t = token.token
        t.isInstanceOf[TSpace] || t.value == "." || t.value == "-"
      }
      case any: PAny => true
      case union: PUnion => union.content.filter(_ != PEmpty).forall(isMergeCandidate)
      case _ => false
    }
  }

  /**
    * Check if we need to expand border of PWordAny after merging the token
    *
    * @param wany
    * @param ptn
    * @return
    */
  def merge(wany: PWordAny, ptn: Pattern): PWordAny = {
    happen()
    if (wany.minLength == 0 && wany.maxLength == -1) {
      // Already includes everything
      return wany
    }

    var numChar = ptn.numChar
    val newMin = wany.minLength + numChar._1
    val newMax = if (wany.maxLength == -1 || numChar._2 == -1) -1 else wany.maxLength + numChar._2
    new PWordAny(newMin, newMax)
  }
}
