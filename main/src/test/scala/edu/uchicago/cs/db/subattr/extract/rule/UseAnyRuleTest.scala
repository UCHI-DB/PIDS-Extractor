package edu.uchicago.cs.db.subattr.extract.rule

import edu.uchicago.cs.db.subattr.extract.parser._
import edu.uchicago.cs.db.subattr.ir._
import org.junit.Assert._
import org.junit.Test

/**
  * Created by harper on 4/5/17.
  */
class UseAnyRuleTest {

  @Test
  def testRewriteInt: Unit = {
    val ptn = PSeq.collect(
      new PToken(new TInt("23432")),
      PUnion.collect(
        new PToken(new TInt("323")),
        new PToken(new TInt("32322")),
        new PToken(new TInt("333")),
        new PToken(new TInt("1231"))
      ),
      new PToken(new TWord("dasfdfa")))

    val rule = new UseAnyRule
    rule.generateOn((0 to 10).map(i => String.valueOf(i)).map(Tokenizer.tokenize(_).toSeq))
    val result = rule.rewrite(ptn)
    assertTrue(rule.happened)

    assertTrue(result.isInstanceOf[PSeq])
    val resq = result.asInstanceOf[PSeq]
    assertEquals(3, resq.content.size)

    assertEquals(new PToken(new TInt("23432")), resq.content(0))
    assertEquals(new PIntAny(3, 5, false), resq.content(1))
    assertEquals(new PToken(new TWord("dasfdfa")), resq.content(2))

  }

  @Test
  def testRewriteLabel: Unit = {
    val ptn = PSeq.collect(
      new PToken(new TInt("23432")),
      PUnion.collect(
        new PToken(new TWord("apa")),
        new PToken(new TInt("323")),
        new PToken(new TWord("twdqa")),
        new PToken(new TInt("322"))
      ),
      new PToken(new TWord("dasfdfa")))

    val rule = new UseAnyRule
    rule.generateOn((0 to 10).map(i => String.valueOf(i)).map(Tokenizer.tokenize(_).toSeq))
    val result = rule.rewrite(ptn)
    assertTrue(rule.happened)

    assertTrue(result.isInstanceOf[PSeq])
    val resq = result.asInstanceOf[PSeq]
    assertEquals(3, resq.content.size)

    assertEquals(new PToken(new TInt("23432")), resq.content(0))
    assertEquals(new PLabelAny(3, 5), resq.content(1))
    assertEquals(new PToken(new TWord("dasfdfa")), resq.content(2))
  }

  @Test
  def testRewriteLetter: Unit = {
    val ptn = PSeq.collect(
      new PToken(new TInt("23432")),
      PUnion.collect(
        new PToken(new TWord("apa")),
        new PToken(new TWord("dmp")),
        new PToken(new TWord("twdqa")),
        new PToken(new TWord("wmpa"))
      ),
      new PToken(new TWord("dasfdfa")))

    val rule = new UseAnyRule
    rule.generateOn((0 to 10).map(i => String.valueOf(i)).map(Tokenizer.tokenize(_).toSeq))
    val result = rule.rewrite(ptn)
    assertTrue(rule.happened)

    assertTrue(result.isInstanceOf[PSeq])
    val resq = result.asInstanceOf[PSeq]
    assertEquals(3, resq.content.size)

    assertEquals(new PToken(new TInt("23432")), resq.content(0))
    assertEquals(new PLetterAny(3, 5), resq.content(1))
    assertEquals(new PToken(new TWord("dasfdfa")), resq.content(2))

  }

  @Test
  def testWithEmpty: Unit = {
    val ptn = PSeq.collect(
      new PToken(new TInt("23432")),
      PUnion.collect(
        new PToken(new TInt("32")),
        new PToken(new TInt("324")),
        new PToken(new TInt("5537")),
        new PToken(new TInt("53281")),
        PEmpty
      ),
      new PToken(new TWord("dasfdfa")))

    val rule = new UseAnyRule
    rule.generateOn((0 to 10).map(i => String.valueOf(i)).map(Tokenizer.tokenize(_).toSeq))
    val result = rule.rewrite(ptn)
    assertTrue(rule.happened)

    val resq = result.asInstanceOf[PSeq]
    assertEquals(new PToken(new TInt("23432")), resq.content(0))
    assertEquals(PUnion.collect(new PIntAny(2, 5, false), PEmpty), resq.content(1))
    assertEquals(new PToken(new TWord("dasfdfa")), resq.content(2))
  }

  @Test
  def testOther: Unit = {
    val ptn = PSeq.collect(
      new PToken(new TInt("23432")),
      PUnion.collect(
        new PToken(new TSymbol("-")),
        new PToken(new TSymbol(".")),
        new PToken(new TSymbol("/")),
        new PToken(new TSymbol("\\"))
      ),
      new PToken(new TWord("dasfdfa")))

    val rule = new UseAnyRule
    rule.generateOn((0 to 10).map(i => String.valueOf(i)).map(Tokenizer.tokenize(_).toSeq))
    val result = rule.rewrite(ptn)
    assertFalse(rule.happened)
  }

  @Test
  def testNotHappen: Unit = {
    val ptn = PSeq.collect(
      new PToken(new TInt("23432")),
      PUnion.collect(
        new PToken(new TInt("323")),
        new PToken(new TInt("32322")),
        PSeq(Seq(new PToken(new TWord("abbd")), new PToken(new TInt("323")))),
        new PToken(new TInt("1231"))
      ),
      new PToken(new TWord("dasfdfa")))

    val rule = new UseAnyRule
    rule.generateOn((0 to 10).map(i => String.valueOf(i)).map(Tokenizer.tokenize(_).toSeq))
    val result = rule.rewrite(ptn)
    assertFalse(rule.happened)
  }
}
