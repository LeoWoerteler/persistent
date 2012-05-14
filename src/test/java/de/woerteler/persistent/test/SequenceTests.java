package de.woerteler.persistent.test;

import java.util.Iterator;

import org.junit.*;
import de.woerteler.persistent.Sequence;
import static org.junit.Assert.*;

/**
 * Tests for the immutable sequences.
 *
 * @author Leo Woerteler
 */
public class SequenceTests {

  /** Some big sequence. */
  private static final Sequence<Integer> TEST;
  static {
    Sequence<Integer> test = Sequence.empty();
    for(int i = 0; i < 12345; i++) test = test.cons(i);
    TEST = test;
  }

  /** Tests if consecutive integers can be inserted. */
  @Test
  public void genInsert() {
    Sequence<Integer> seq = Sequence.empty();
    for(int i = 0; i < 1000; i++) {
      assertEquals("Sequence size", i, seq.length());
      seq = seq.cons(Integer.valueOf(i));
      for(int j = 0; j <= i; j++) assertEquals(seq.toString(), j, (int) seq.get(j));
    }
  }

  /** Tests if the iterator yields the items in the right order. */
  @Test
  public void genIter() {
    final Iterator<Integer> it = TEST.iterator();
    for(int i = 0; i < 12345; i++) {
      assertEquals(i, (int) it.next());
    }
  }

  /** Checks if the iterator works for different sizes of sequences. */
  @Test
  public void iter() {
    Sequence<Integer> seq = Sequence.empty();
    for(int i = 0; i < 1000; i++) {
      int j = 0;
      for(@SuppressWarnings("unused") final Integer it : seq) j++;
      assertEquals(i, j);
      seq = seq.cons(i);
    }
  }

  /** Tests if the empty sequence is correctly written to an array. */
  @Test
  public void toArray() {
    assertArrayEquals(new Object[0], Sequence.empty().toArray());
  }

  /** Tests if a new array is created if the sequence doesn't fit into the given one. */
  @Test
  public void toArrayTSmall() {
    assertArrayEquals(new Integer[] { 123 },
        Sequence.empty().cons(123).toArray(new Integer[0]));
  }

  /**
   * Tests if the array given to {@link Sequence#toArray(Object[])} is used if it's
   * big enough.
   */
  @Test
  public void toArrayTBig() {
    final Integer[] in = new Integer[3];
    assertSame("same array", in, Sequence.empty().cons(1).cons(2).cons(3).toArray(in));
  }
}
