package edu.uchicago.cs.db.subattr.extract.rule

import edu.uchicago.cs.db.subattr.extract.parser.{TInt, TSpace, TSymbol, TWord}
import edu.uchicago.cs.db.subattr.ir.{PAny, PEmpty, PSeq, PToken, PUnion, PWordAny, Pattern}
import edu.uchicago.cs.db.subattr.nlp.NaturalLangDict
import DictionaryWordRule._

/**
  * This rule looks at unions of token / sequence and determine if they contain a significant amount of
  * dictionary words. If so, the entire union will be rewritten as a PWordAny
  *
  * This rule greedy treats all Any as valid word, so it should be applied before any is generated for accuracy
  */


object DictionaryWordRule {

  val coverageThreshold = 0.9

  implicit def boolean2Double(b: Boolean): Double = if (b) 1 else 0

}

/**
  *
  */
class DictionaryWordRule extends RewriteRule {

  val dictionary = NaturalLangDict.GLOVE

  /**
    *
    * Check if a pattern is suitable for becoming a word
    *
    * Note that we only validate the symbol here, instead of using evaluate.
    * In practice, we notice that when a Union has most of its members not word, we do not want to rewrite the remaining.
    * E.g., U(12342, 'GOOD3', 'KMBPM') should be left unchanged although GOOD3 can be rewritten as wordany and 3
    *
    * To achieve this, we will move the evaluate to the update phase, where if fails, no children will be evaluated.
    *
    * @param ptn
    * @return
    */
  override protected def condition(ptn: Pattern): Boolean = {
    ptn match {
      case union: PUnion => union.content.filter(_ != PEmpty).forall(validSymbol)
      case seq: PSeq => seq.content.filter(_ != PEmpty).forall(validSymbol)
      case any: PAny => false
      // Note that we only allow container level rewrite, no single token will be rewritten
      case _ => false
    }
  }

  // Test if a pattern contains only valid symbols
  def validSymbol(ptn: Pattern): Boolean = {
    ptn match {
      case token: PToken => dictionary.validSymbols(token.token.value)
      case seq: PSeq => seq.content.forall(validSymbol)
      case union: PUnion => union.content.forall(validSymbol)
      case _: PAny => true
      case _ => false
    }
  }

  override protected def update(ptn: Pattern): Pattern = {
    if (evaluate(ptn) < coverageThreshold) {
      return ptn
    }
    happen()
    var minLength = 1
    var maxLength = -1
    // Has empty
    if (ptn.isInstanceOf[PUnion]) {
      val union = ptn.asInstanceOf[PUnion]
      if (union.content.contains(PEmpty)) {
        minLength = 0
      } else {
        val chars = union.numChar
        if (chars._1 == chars._2) {
          minLength = chars._1
          maxLength = chars._1
        }
      }
    }

    new PWordAny(minLength, maxLength)
  }

  def evaluate(ptn: Pattern): Double = {
    ptn match {
      case token: PToken => {
        token.token match {
          case word: TWord => if (dictionary.test(word.value)) 1 else 0
          case _ => 0
        }
      }
      case union: PUnion => union.content.filter(_ != PEmpty).map(evaluate)
        .foldLeft((0.0, 1))((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1
      case seq: PSeq =>
        // convert the tokens back to string, split by space and match with the dictionary
        // then compute the average
        seq.flatten.map(_.toString).mkString("").split("\\s+")
          .map(dictionary.test)
          .foldLeft((0.0, 1))((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1
      case _ => 0
    }
  }
}
