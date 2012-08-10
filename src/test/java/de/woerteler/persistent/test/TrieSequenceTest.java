package de.woerteler.persistent.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.woerteler.persistent.ArraySequence;
import de.woerteler.persistent.PersistentSequence;
import de.woerteler.persistent.TrieSequence;

/**
 * Tests for {@link TrieSequence}.
 *
 * @author Leo Woerteler
 */
public class TrieSequenceTest {

  /**
   * Tests {@link Object#equals(Object)} and {@link Object#hashCode()}.
   * 
   * @param a first object to compare
   * @param b second object to compare
   * @return <code>true</code> iff <code>a</code> is equal to <code>b</code>,
   *         <code>b</code> is equal to <code>a</code> (commutativity) and the
   *         hash codes match
   */
  public static final boolean equalsWithHash(final Object a, final Object b) {
    return a.equals(b) && b.equals(a) && a.hashCode() == b.hashCode();
  }

  /**
   * Tests <code>not</code> {@link Object#equals(Object)}.
   * {@link Object#hashCode()} must not necessarily differ.
   * 
   * @param a first object to compare
   * @param b second object to compare
   * @return <code>true</code> iff <code>a</code> is not equal to <code>b</code>
   *         and <code>b</code> is not equal to <code>a</code> (commutativity)
   */
  public static final boolean notEqual(final Object a, final Object b) {
    return !a.equals(b) && !b.equals(a);
  }

  /**
   * Creates {@link Iterable}s for integer ranges.
   * @param start start value (inclusive)
   * @param end end value (exclusive)
   * @return the iterable
   */
  private static Iterable<Integer> range(final int start, final int end) {
    return new Iterable<Integer>() {
      int current = start;
      @Override
      public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
          @Override public boolean hasNext() { return current < end; }
          @Override public Integer next() { return current++; }

          @Override
          public void remove() { /* nothing to do */}
        };
      }
    };
  }

  /** Some big sequence. */
  private static final TrieSequence<Integer> TEST;
  static {
    TrieSequence<Integer> test = TrieSequence.empty();
    for(int i = 0; i < 12345; i++) {
      test = test.add(i);
    }
    TEST = test;
  }

  /** Tests if consecutive integers can be inserted. */
  @Test
  public void genInsert() {
    TrieSequence<Integer> seq = TrieSequence.empty();
    for(int i = 0; i < 1234; i++) {
      assertEquals("Sequence size", i, seq.size());
      seq = seq.add(i);
    }
    for(int i = 0; i < 1234; i++) {
      assertEquals((Integer) i, seq.get(i));
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
    TrieSequence<Integer> seq = TrieSequence.empty();
    for(int i = 0; i < 1000; i++) {
      int j = 0;
      for(@SuppressWarnings("unused") final Integer it : seq) {
        j++;
      }
      assertEquals(i, j);
      seq = seq.add(i);
    }
  }

  /** Tests if the empty sequence is correctly written to an array. */
  @Test
  public void toArray() {
    assertArrayEquals(new Object[0], TrieSequence.empty().toArray());
  }

  /** Tests if a new array is created if the sequence doesn't fit into the given one. */
  @Test
  public void toArrayTSmall() {
    assertArrayEquals(new Integer[] { 123 },
        TrieSequence.singleton(123).toArray(new Integer[0]));
  }

  /**
   * Tests if the array given to {@link TrieSequence#toArray(Object[])} is used if it's
   * big enough.
   */
  @Test
  public void toArrayTBig() {
    final Integer[] in = new Integer[3];
    assertSame("same array", in, TrieSequence.from(1, 2, 3).toArray(in));
  }

  /** Tests if a drained iterator throws {@link NoSuchElementException}. */
  @Test(expected = NoSuchElementException.class)
  public void drainedError() {
    TrieSequence.empty().iterator().next();
  }

  /** Tests if {@link Iterator#remove()} throws {@link UnsupportedOperationException}. */
  @Test(expected = UnsupportedOperationException.class)
  public void removeError() {
    TrieSequence.empty().iterator().remove();
  }

  /** Tests if sequences can be created from arrays. */
  @Test public void fromArray() {
    assertSame(TrieSequence.empty(), TrieSequence.from(new Integer[0]));
    for(final int len : new int[] { 1, TrieSequence.SIZE + TrieSequence.SIZE / 2,
        2 * TrieSequence.SIZE }) {
      final Integer[] arr = new Integer[len];
      for(int j = 0; j < len; j++) {
        arr[j] = j;
      }
      final TrieSequence<Integer> seq = TrieSequence.from(arr);
      assertEquals("size", len, seq.size());
      for(int j = 0; j < len; j++) {
        assertEquals("element", (Integer) j, seq.get(j));
      }
    }
  }

  /** Tests if sequences can be created from {@link Iterable}s. */
  @Test public void fromIterable() {
    assertSame(TrieSequence.empty(), TrieSequence.from(Collections.emptyList()));
    final TrieSequence<Integer> single = TrieSequence.singleton(42);
    assertSame(single, TrieSequence.from(single));
    for(final int len : new int[] { 1, TrieSequence.SIZE + TrieSequence.SIZE / 2,
        2 * TrieSequence.SIZE }) {
      final List<Integer> list = new ArrayList<Integer>(len);
      for(int j = 0; j < len; j++) {
        list.add(j);
      }
      final TrieSequence<Integer> seq = TrieSequence.from(list);
      assertEquals("size", len, seq.size());
      for(int j = 0; j < len; j++) {
        assertEquals("element", (Integer) j, seq.get(j));
      }
    }
  }

  /** Tests {@link TrieSequence#equals(Object)} and {@link TrieSequence#hashCode()}. */
  @Test public void testEquals() {
    assertTrue(equalsWithHash(TrieSequence.EMPTY, TrieSequence.from()));
    assertTrue(equalsWithHash(TrieSequence.from(1, 2, 3), TrieSequence.from(1, 2, 3)));
    assertTrue(equalsWithHash(TrieSequence.from(range(0, TrieSequence.SIZE)),
        TrieSequence.from(range(0, TrieSequence.SIZE))));
    assertTrue(equalsWithHash(TrieSequence.from(range(0, 3 * TrieSequence.SIZE / 2)),
        TrieSequence.from(range(0, 3 * TrieSequence.SIZE / 2))));
    assertTrue("null", equalsWithHash(TrieSequence.from(0, null),
        TrieSequence.from(0, null)));

    assertFalse("length", equalsWithHash(TrieSequence.from(), TrieSequence.from(1)));
    assertFalse("no sequence", equalsWithHash(TrieSequence.from(1), 1));
    assertFalse("elements", equalsWithHash(TrieSequence.from(0), TrieSequence.from(1)));
    assertFalse("null", equalsWithHash(TrieSequence.from(0, null),
        TrieSequence.from(0, 1)));
    assertFalse("null2", equalsWithHash(TrieSequence.from(0, 1),
        TrieSequence.from(0, null)));
  }

  /** Tests {@link TrieSequence#equals(Object)} and {@link TrieSequence#hashCode()}. */
  @Test public void testAppend() {
    final int size = TrieSequence.SIZE, q32 = 3 * size / 2;
    final TrieSequence<Integer> empty = TrieSequence.empty(),
        s123 = TrieSequence.from(1, 2, 3),
        s456 = TrieSequence.from(4, 5, 6);
    assertSame(s123, empty.append(s123));
    assertSame(s123, s123.append(empty));

    assertEquals(TrieSequence.from(1, 2, 3, 4, 5, 6), s123.append(s456));
    assertEquals(TrieSequence.from(range(0, q32)),
        TrieSequence.from(range(0, q32 / 2)).append(
            TrieSequence.from(range(q32 / 2, q32))));
    assertEquals(TrieSequence.from(range(0, 3 * size + q32)),
        TrieSequence.from(range(0, 2 * size)).append(
            TrieSequence.from(range(2 * size, 3 * size + q32))));
    assertEquals(TrieSequence.from(range(0, 2 * size)),
        TrieSequence.from(range(0, size)).append(
            TrieSequence.from(range(size, 2 * size))));
    assertEquals(TrieSequence.from(range(0, 3 * size)),
        TrieSequence.from(range(0, q32)).append(TrieSequence.from(range(q32, 3 * size))));
    assertEquals(TrieSequence.from(range(0, 2 * size + q32)),
        TrieSequence.from(range(0, q32)).append(
            TrieSequence.from(range(q32, 2 * size + q32))));
    assertEquals(TrieSequence.from(range(0, size + 3)),
        TrieSequence.from(range(0, 3)).append(TrieSequence.from(range(3, size + 3))));
  }

  /** Tests appends on arbitrary sequences. */
  @Test
  public void appendArbitrary() {
    final PersistentSequence<Integer> s123 = TrieSequence.from(1, 2, 3),

        s456 = TrieSequence.from(4, 5, 6),

        a123 = ArraySequence.from(1, 2, 3), a456 = ArraySequence.from(4, 5, 6),

        s123456 = TrieSequence.from(1, 2, 3, 4, 5, 6),

        a123456 = ArraySequence.from(1, 2, 3, 4, 5, 6),

        css = s123.append(s456), csa = s123.append(a456),

        cas = a123.append(s456), caa = a123.append(a456);
    assertTrue(equalsWithHash(a123, s123));
    assertTrue(equalsWithHash(a456, s456));
    assertTrue(equalsWithHash(s123456, a123456));
    assertTrue(notEqual(a123, a456));
    assertTrue(notEqual(s123, a456));
    assertTrue(notEqual(a123, s456));
    assertTrue(notEqual(s123, s456));
    assertTrue(notEqual(s123, s123456));
    assertTrue(notEqual(a123, s123456));
    assertTrue(notEqual(s123, a123456));
    assertTrue(notEqual(a123, a123456));
    assertTrue(notEqual(s456, s123456));
    assertTrue(notEqual(s456, a123456));
    assertTrue(notEqual(a456, s123456));
    assertTrue(notEqual(a456, a123456));
    assertTrue(equalsWithHash(css, a123456));
    assertTrue(equalsWithHash(csa, a123456));
    assertTrue(equalsWithHash(cas, a123456));
    assertTrue(equalsWithHash(caa, a123456));
    assertTrue(equalsWithHash(css, s123456));
    assertTrue(equalsWithHash(csa, s123456));
    assertTrue(equalsWithHash(cas, s123456));
    assertTrue(equalsWithHash(caa, s123456));
  }

}
