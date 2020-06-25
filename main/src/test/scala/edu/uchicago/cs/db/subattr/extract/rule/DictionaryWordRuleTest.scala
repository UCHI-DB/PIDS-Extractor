package edu.uchicago.cs.db.subattr.extract.rule

import edu.uchicago.cs.db.subattr.extract.parser.{TInt, TSpace, TSymbol, TWord}
import edu.uchicago.cs.db.subattr.ir.{PSeq, PToken, PUnion, PWordAny}
import org.junit.Test
import org.junit.Assert.{assertEquals, _}

class DictionaryWordRuleTest {

  @Test
  def testEvaluate: Unit = {
    val rule = new DictionaryWordRule
    val p1 = PSeq(Seq(new PToken(new TWord("Dogwood")), new PToken(new TSpace()), new PToken(new TWord("Oakwood"))))
    assertEquals(1, rule.evaluate(p1), 0.0001)
    val p2 = PSeq(Seq(new PToken(new TWord("14th")), new PToken(new TSpace()), new PToken(new TWord("Monday"))))
    assertEquals(1, rule.evaluate(p2), 0.0001)
    val p3 = PSeq(Seq(new PToken(new TWord("greenwood")), new PToken(new TSpace()), new PToken(new TWord("rd."))))
    assertEquals(1, rule.evaluate(p3), 0.0001)
    val p4 = new PToken(new TInt("4242"))
    assertEquals(0, rule.evaluate(p4), 0.0001)
    val p5 = PUnion(Seq(p1, p2, p3, p4))
    assertEquals(0.75, rule.evaluate(p5), 0.0001)
    val p6 = new PToken(new TWord("William"))
    assertEquals(1, rule.evaluate(p6), 0.0001)
    val p7 = PSeq(Seq(new PToken(new TInt("3234")), new PToken(new TSymbol(".")), new PToken(new TInt("32432"))))
    assertEquals(0, rule.evaluate(p7), 0.0001)
  }

  @Test
  def testRewrite: Unit = {
    val rule = new DictionaryWordRule
    val pattern = PSeq(Seq(new PToken(new TWord("Dogwood")), new PToken(new TSpace()), new PToken(new TWord("Oakwood"))))
    val rewritten = rule.rewrite(pattern)
    assertTrue(rewritten.isInstanceOf[PWordAny])
  }

  @Test
  def testRewriteUnionOfWords: Unit = {
    val rule = new DictionaryWordRule
    val pattern = PUnion(Seq(
      new PToken(new TWord("Dogwood")),
      new PToken(new TWord("William")),
      PSeq(Seq(new PToken(new TInt("14")), new PToken(new TWord("th")))),
      new PToken(new TWord("Oakwood")),
      new PToken(new TWord("Second"))
    ))
    val rewritten = rule.rewrite(pattern)
    assertTrue(rewritten.isInstanceOf[PWordAny])
  }

  @Test
  def testShouldRewrite: Unit = {
    val rule = new DictionaryWordRule
    val pattern = PUnion(Seq(
      new PToken(new TWord("Park")),
      PSeq(Seq(new PToken(new TInt("13")), new PToken(new TWord("th")))),
      new PToken(new TWord("Washington")),
      PSeq(Seq(new PToken(new TInt("1")), new PToken(new TWord("st")))),
      new PToken(new TWord("Jefferson")),
      new PToken(new TWord("Dogwood")),
      new PToken(new TWord("Adams"))
    ))

    val rewritten = rule.rewrite(pattern)
    assertTrue(rewritten.isInstanceOf[PWordAny])
  }

  @Test
  def testNotHappen: Unit = {
    val rule = new DictionaryWordRule
    val pattern = PSeq(Seq(new PToken(new TInt("3423")), new PToken(new TSymbol(".")), new PToken(new TInt("4224"))))
    val rewritten = rule.rewrite(pattern)
    assertFalse(rule.happened)
  }

}
