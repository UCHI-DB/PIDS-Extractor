package edu.uchicago.cs.db.subattr.extract.rule

import edu.uchicago.cs.db.subattr.extract.parser.{TSpace, TSymbol}
import edu.uchicago.cs.db.subattr.ir.{PEmpty, PIntAny, PSeq, PToken, PUnion, PWordAny}
import org.junit.Test
import org.junit.Assert._

class MergeWordRuleTest {

  @Test
  def testSimpleMerge: Unit = {
    val pattern = PSeq(Seq(
      new PWordAny(1, -1),
      new PToken(new TSpace()),
      new PWordAny(0, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(1, -1),
      PUnion(Seq(
        PEmpty,
        new PToken(new TSymbol("."))
      )),
      new PToken(new TSymbol("|")),
      new PWordAny(1, -1),
      new PToken(new TSymbol(",")),
      new PIntAny(1, -1)
    ))

    val rule = new MergeWordRule
    val rewritten = rule.rewrite(pattern)

    assertTrue(rule.happened)

    val expected = PSeq(Seq(
      new PWordAny(2, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(1, -1),
      new PToken(new TSymbol("|")),
      new PWordAny(1, -1),
      new PToken(new TSymbol(",")),
      new PIntAny(1, -1)
    ))

    assertEquals(expected, rewritten)
  }

  @Test
  def testMergeWithUnion:Unit = {
    val pattern = PSeq(Seq(
      new PWordAny(1, -1),
      PUnion(Seq(
        PEmpty,
        new PToken(new TSymbol("."))
      )),
      new PWordAny(0, -1),
    ))

    val rule = new MergeWordRule
    val rewritten = rule.rewrite(pattern)

    assertTrue(rule.happened)

    val expected = PSeq(Seq(
      new PWordAny(1, -1),
    ))

    assertEquals(expected, rewritten)
  }

  @Test
  def testNotHappen: Unit = {
    val alreadygood = PSeq(Seq(
      new PWordAny(1, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(0, -1),
      new PToken(new TSymbol("|")),
      new PWordAny(1, -1)
    ))
    val rule = new MergeWordRule
    val rewritten = rule.rewrite(alreadygood)

    assertFalse(rule.happened)
  }

  @Test
  def testMergeBackward: Unit = {
    val pattern = PSeq(Seq(
      new PIntAny(0, -1),
      new PToken(new TSpace()),
      new PWordAny(1, -1),
      new PToken(new TSpace()),
      new PWordAny(0, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(1, -1),
      PUnion(Seq(
        PEmpty,
        new PToken(new TSymbol("."))
      )),
      new PToken(new TSymbol("|")),
      new PWordAny(1, -1)
    ))

    val rule = new MergeWordRule
    val rewritten = rule.rewrite(pattern)

    assertTrue(rule.happened)

    val expected = PSeq(Seq(
      new PWordAny(3, -1),
      new PToken(new TSymbol(",")),
      new PWordAny(1, -1),
      new PToken(new TSymbol("|")),
      new PWordAny(1, -1)
    ))
    assertEquals(expected, rewritten)
  }

  @Test
  def testMergeLength: Unit = {
    val pattern = PSeq(Seq(
      new PWordAny(1, -1),
      PUnion(Seq(
        PEmpty,
        new PToken(new TSymbol("."))
      )),
      new PWordAny(0, -1)
    ))

    val rule = new MergeWordRule
    val rewritten = rule.rewrite(pattern)

    assertTrue(rule.happened)

    val expected = PSeq(Seq(new PWordAny(1, -1)))
    assertEquals(expected, rewritten)
  }
}
