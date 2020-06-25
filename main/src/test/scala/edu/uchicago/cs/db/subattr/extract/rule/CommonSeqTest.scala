package edu.uchicago.cs.db.subattr.extract.rule

import edu.uchicago.cs.db.subattr.extract.parser._
import edu.uchicago.cs.db.subattr.ir._
import org.junit.Assert._
import org.junit.Test

import scala.io.Source

/**
  * Created by harper on 3/14/17.
  */
class CommonSeqTest {

  @Test
  def testBetween(): Unit = {
    val a = "778-9383 Suspendisse Av. Weirton IN 93479 (326) 677-3419"
    val b = "Ap #285-7193 Ullamcorper Avenue Amesbury HI 93373 (302) 259-2375"

    val atokens = Tokenizer.tokenize(a).toSeq
    val btokens = Tokenizer.tokenize(b).toSeq

    val cseq = new CommonSeq

    val commons = cseq.between(atokens, btokens, (a: Token, b: Token) => {
      (a.getClass == b.getClass) && (a match {
        case wd: TWord => a.value.equals(b.value)
        case _ => true
      })
    }, (a: Token) => 1)

    assertEquals(5, commons.size)

    //noinspection ZeroIndexToHead
    assertEquals((0, 3, 4), commons(0))
    assertEquals((5, 8, 1), commons(1))
    assertEquals((8, 10, 1), commons(2))
    assertEquals((10, 12, 1), commons(3))
    assertEquals((12, 14, 10), commons(4))
  }

  @Test
  def testBetween2(): Unit = {
    val a = Array(0, 2, 3, 0, 4, 5, 6)
    val b = Array(2, 3, 4, 1, 8, 7, 5, 6, 0)
    val cseq = new CommonSeq
    val commons = cseq.between(a, b, (a: Int, b: Int) => a == b, (a: Int) => 1)

    assertEquals(3, commons.size)
    assertEquals((1, 0, 2), commons(0))
    assertEquals((4, 2, 1), commons(1))
    assertEquals((5, 6, 2), commons(2))
  }

  @Test
  def testBetweenFirstOccurance(): Unit = {
    val a = Array(1)
    val b = Array(1, 0, 5, 1, 3)

    val cseq = new CommonSeq
    val btw = cseq.between(a, b, (a: Int, b: Int) => a == b, (a: Int) => 1)

    assertEquals(1, btw.size)
    assertEquals((0, 0, 1), btw(0))
  }

  @Test
  def testBetweenInOrder(): Unit = {
    val a = Array(0, 1, 8, 7, 2, 3, 4, 5, 6)
    val b = Array(2, 3, 4, 5, 6, 8, 7, 0, 1)
    val cseq = new CommonSeq
    val commons = cseq.between(a, b, (a: Int, b: Int) => a == b, (a: Int) => 1)

    assertEquals(1, commons.size)
    assertEquals((4, 0, 5), commons(0))
  }

  @Test
  def testBetweenLongest(): Unit = {
    val a = Array(0, 1, 0, 0, 1, 0, 0, 1, 0, 0)
    val b = Array(0, 1, 0, 2, 0, 1, 0, 0, 1, 0, 0)

    val cseq = new CommonSeq
    val commons = cseq.between(a, b, (a: Int, b: Int) => a == b, (a: Int) => 1)
    assertEquals(2, commons.size)
    assertEquals((0, 0, 3), commons(0))
    assertEquals((3, 4, 7), commons(1))
  }

  @Test
  def testFind: Unit = {
    val a = Array(Array(1, 2, 3, 4, 5, 6, 7, 8, 9),
      Array(1, 2, 3, 4, 9, 5, 6, 2),
      Array(2, 3, 4, 4, 2, 5, 6, 1),
      Array(3, 4, 5, 6, 7)).map(_.toSeq).toSeq

    val cseq = new CommonSeq
    val commons = cseq.find(a)

    assertEquals(4, commons.length)
    assertArrayEquals(Array(3, 4, 5, 6), commons.toArray)

    val pos = cseq.positions
    assertEquals(4, pos.length)

    assertArrayEquals(Array(2, 3, 4, 5), pos(0).toArray)
    assertArrayEquals(Array(2, 3, 5, 6), pos(1).toArray)
    assertArrayEquals(Array(1, 2, 5, 6), pos(2).toArray)
    assertArrayEquals(Array(0, 1, 2, 3), pos(3).toArray)
  }

  @Test
  def testCommonSeqInSequence: Unit = {
    // The common sequences should appear in order
    val a = Array(Array(1, 2, 3, 2), Array(1, 0, 1, 0, 2, 3, 2), Array(2, 3, 2, 1)).map(_.toSeq).toSeq
    val cseq = new CommonSeq
    val commons = cseq.find(a)

    // should only contain (2,3,2), (1) is not good
    assertEquals(3, commons.size)
    assertArrayEquals(Array(2, 3, 2), commons.toArray)
  }

  @Test
  def testFirstOccurance: Unit = {
    val input = Array(Array(3, 1, 5, 1, 7), Array(4, 1, 8), Array(9, 1, 6, 1, 2)).map(_.toSeq)
    val cseq = new CommonSeq
    val commons = cseq.find(input)
    assertEquals(1, commons.size)
    assertEquals(1, commons(0))
  }

  @Test
  def testSingleRecordPosition: Unit = {
    // Position should not be empty when a single line is provided
    val a = Array(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).map(_.toSeq).toSeq

    val cseq = new CommonSeq
    val commons = cseq.find(a)

    assertEquals(9, commons.length)
    assertArrayEquals(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), commons.toArray)

    val pos = cseq.positions
    assertEquals(1, pos.length)

    assertArrayEquals((0 until 9).toArray, pos(0).toArray)
  }

  @Test
  def testCommonSeqWithToken: Unit = {
    val a = Array("J01", "P05", "L37", "D53", "M21")
      .map(s => Tokenizer.tokenize(s).map(t => new PToken(t)).toSeq).toSeq

    val csq = new CommonSeq
    val tokeneq: (Pattern, Pattern) => Boolean = (a, b) => {
      (a, b) match {
        case (atk: PToken, btk: PToken) => atk.token.getClass == btk.token.getClass
        case _ => a.equals(b)
      }
    }
    val res = csq.find(a, tokeneq)
    assertEquals(2, res.size)
    assertTrue(res(0).token.isInstanceOf[TWord])
    assertTrue(res(1).token.isInstanceOf[TInt])
  }

  @Test
  def testAddress: Unit = {
    val lines = Source.fromInputStream(Thread.currentThread().getContextClassLoader.getResourceAsStream("pattern/sample_address"))
      .getLines().map(Tokenizer.tokenize(_).toSeq).toSeq
    val result = new CommonSeq().find(lines, (a: Token, b: Token) => a == b)
    assertEquals(11, result.length)

    assertEquals(",", result(0).toString)
    assertEquals(" ", result(1).toString)
    assertEquals(",", result(2).toString)
    assertEquals(",", result(3).toString)
    assertEquals("Suite", result(4).toString)
    assertEquals(" ", result(5).toString)
    assertEquals(",", result(6).toString)
    assertEquals(",", result(7).toString)
    assertEquals(" ", result(8).toString)
    assertEquals(",", result(9).toString)
    assertEquals(",", result(10).toString)
  }

  @Test
  def testPriority: Unit = {
    val lines = Seq(
      Seq(
        new PToken(new TSpace),
        new PToken(new TSymbol(",")),
        new PToken(new TSpace),
        new PToken(new TSymbol(",")),
        new PToken(new TSymbol(",")),
      ), Seq(
        new PToken(new TSymbol(",")),
        new PToken(new TSpace),
        new PToken(new TSpace),
        new PToken(new TSymbol(",")),
        new PToken(new TSymbol(",")),
      )
    )
    val result = new CommonSeq().find(lines,
      (a: PToken, b: PToken) => a == b,
      (a: PToken) => {
        a.token match {
          case space: TSpace => 1
          case symbol: TSymbol => 100
          case _ => 1
        }
      })

    assertEquals(4, result.length)
    assertEquals(new PToken(new TSymbol(",")), result(0))
    assertEquals(new PToken(new TSpace), result(1))
    assertEquals(new PToken(new TSymbol(",")), result(2))
    assertEquals(new PToken(new TSymbol(",")), result(3))
  }
}

