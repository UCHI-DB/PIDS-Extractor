package edu.uchicago.cs.db.subattr.extract.rule

import edu.uchicago.cs.db.subattr.extract.parser._
import edu.uchicago.cs.db.subattr.ir._
import org.junit.Assert._
import org.junit.Test

/**
  * Created by harper on 4/25/17.
  */
class MergeGroupRuleTest {

  @Test
  def testRewriteSeq1: Unit = {

    val pattern = PSeq.collect(
      PSeq.collect(
        new PToken(new TInt("323")),
        new PToken(new TWord("sdds"))
      ),
      PSeq.collect(
        new PToken(new TWord("Aad")),
        new PToken(new TInt("23423"))
      )
    )

    val rule = new MergeGroupRule

    val rewritten = rule.rewrite(pattern)

    assertTrue(rule.happened)

    assertTrue(rewritten.isInstanceOf[PSeq])
    val seq = rewritten.asInstanceOf[PSeq]
    assertEquals(4, seq.content.length)


    assertTrue(seq.content.contains(new PToken(new TInt("323"))))
    assertTrue(seq.content.contains(new PToken(new TWord("sdds"))))
    assertTrue(seq.content.contains(new PToken(new TWord("Aad"))))
    assertTrue(seq.content.contains(new PToken(new TInt("23423"))))
  }

  @Test
  def testRewriteUnion1: Unit = {

    val pattern = PUnion.collect(
      PUnion.collect(
        new PToken(new TInt("323")),
        new PToken(new TWord("sdds"))
      ),
      PUnion.collect(
        new PToken(new TWord("Aad")),
        new PToken(new TInt("23423"))
      )
    )

    val rule = new MergeGroupRule

    val rewritten = rule.rewrite(pattern)

    assertTrue(rule.happened)

    assertTrue(rewritten.isInstanceOf[PUnion])
    val seq = rewritten.asInstanceOf[PUnion]
    assertEquals(4, seq.content.length)

    assertTrue(seq.content.contains(new PToken(new TInt("323"))))
    assertTrue(seq.content.contains(new PToken(new TWord("sdds"))))
    assertTrue(seq.content.contains(new PToken(new TWord("Aad"))))
    assertTrue(seq.content.contains(new PToken(new TInt("23423"))))
  }

  @Test
  def testRewriteSeq2: Unit = {

    val pattern = PSeq.collect(
      PSeq.collect(
        new PToken(new TInt("323")),
        new PToken(new TWord("sdds"))
      ),
      new PToken(new TWord("Aad")),
      new PToken(new TInt("23423"))
    )

    val rule = new MergeGroupRule

    val rewritten = rule.rewrite(pattern)

    assertTrue(rule.happened)

    assertTrue(rewritten.isInstanceOf[PSeq])
    val seq = rewritten.asInstanceOf[PSeq]
    assertEquals(4, seq.content.length)

    assertTrue(seq.content.contains(new PToken(new TInt("323"))))
    assertTrue(seq.content.contains(new PToken(new TWord("sdds"))))
    assertTrue(seq.content.contains(new PToken(new TWord("Aad"))))
    assertTrue(seq.content.contains(new PToken(new TInt("23423"))))
  }
  @Test
  def testRewriteUnion2: Unit = {

    val pattern = PUnion.collect(
      PUnion.collect(
        new PToken(new TInt("323")),
        new PToken(new TWord("sdds"))
      ),
      new PToken(new TWord("Aad")),
      new PToken(new TInt("23423"))
    )

    val rule = new MergeGroupRule

    val rewritten = rule.rewrite(pattern)

    assertTrue(rule.happened)

    assertTrue(rewritten.isInstanceOf[PUnion])
    val seq = rewritten.asInstanceOf[PUnion]
    assertEquals(4, seq.content.length)

    assertTrue(seq.content.contains(new PToken(new TInt("323"))))
    assertTrue(seq.content.contains(new PToken(new TWord("sdds"))))
    assertTrue(seq.content.contains(new PToken(new TWord("Aad"))))
    assertTrue(seq.content.contains(new PToken(new TInt("23423"))))
  }
  @Test
  def testRewriteNotHappen: Unit = {

    val pattern = PSeq.collect(
      new PToken(new TInt("323")),
      new PToken(new TWord("sdds")),
      new PToken(new TWord("Aad")),
      new PToken(new TInt("23423"))
    )

    val rule = new MergeGroupRule

    val rewritten = rule.rewrite(pattern)

    assertFalse(rule.happened)
  }
}
