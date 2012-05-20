package de.woerteler.persistent.test;

import java.util.*;

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
        Sequence.singleton(123).toArray(new Integer[0]));
  }

  /**
   * Tests if the array given to {@link Sequence#toArray(Object[])} is used if it's
   * big enough.
   */
  @Test
  public void toArrayTBig() {
    final Integer[] in = new Integer[3];
    assertSame("same array", in, Sequence.from(1, 2, 3).toArray(in));
  }

  /** Tests if a drained iterator throws {@link NoSuchElementException}. */
  @Test(expected = NoSuchElementException.class)
  public void drainedError() {
    Sequence.empty().iterator().next();
  }

  /** Tests if {@link Iterator#remove()} throws {@link UnsupportedOperationException}. */
  @Test(expected = UnsupportedOperationException.class)
  public void removeError() {
    Sequence.empty().iterator().remove();
  }

  /** Tests if sequences can be created from arrays. */
  @Test public void fromArray() {
    assertSame(Sequence.empty(), Sequence.from(new Integer[0]));
    for(int len : new int[] { 1, Sequence.SIZE + Sequence.SIZE / 2, 2 * Sequence.SIZE }) {
      final Integer[] arr = new Integer[len];
      for(int j = 0; j < len; j++) arr[j] = j;
      final Sequence<Integer> seq = Sequence.from(arr);
      assertEquals("size", len, seq.length());
      for(int j = 0; j < len; j++) assertEquals("element", (Integer) j, seq.get(j));
    }
  }

  /** Tests if sequences can be created from {@link Iterable}s. */
  @Test public void fromIterable() {
    assertSame(Sequence.empty(), Sequence.from(Collections.emptyList()));
    final Sequence<Integer> single = Sequence.singleton(42);
    assertSame(single, Sequence.from(single));
    for(int len : new int[] { 1, Sequence.SIZE + Sequence.SIZE / 2, 2 * Sequence.SIZE }) {
      final List<Integer> list = new ArrayList<Integer>(len);
      for(int j = 0; j < len; j++) list.add(j);
      final Sequence<Integer> seq = Sequence.from(list);
      assertEquals("size", len, seq.length());
      for(int j = 0; j < len; j++) assertEquals("element", (Integer) j, seq.get(j));
    }
  }
}
