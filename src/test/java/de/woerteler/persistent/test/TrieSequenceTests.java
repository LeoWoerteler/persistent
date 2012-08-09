package de.woerteler.persistent.test;

import java.util.*;

import org.junit.*;
import de.woerteler.persistent.TrieSequence;
import static org.junit.Assert.*;

/**
 * Tests for {@link TrieSequence}.
 *
 * @author Leo Woerteler
 */
public class TrieSequenceTests {

  /**
   * Tests {@link Object#equals(Object)} and {@link Object#hashCode()}.
   * @param a first object to compare
   * @param b second object to compare
   * @return <code>true</code> iff <code>a</code> is equal to <code>b</code>,
   *   <code>b</code> is equal to <code>a</code> (commutativity) an the hash codes match
   */
  public static boolean equalsWithHash(final Object a, final Object b) {
    return a.equals(b) && b.equals(a) && a.hashCode() == b.hashCode();
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
          @Override public void remove() { }
        };
      }
    };
  }

  /** Some big sequence. */
  private static final TrieSequence<Integer> TEST;
  static {
    TrieSequence<Integer> test = TrieSequence.empty();
    for(int i = 0; i < 12345; i++) test = test.add(i);
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
    for(int i = 0; i < 1234; i++) assertEquals((Integer) i, seq.get(i));
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
      for(@SuppressWarnings("unused") final Integer it : seq) j++;
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
    for(int len : new int[] { 1, TrieSequence.SIZE + TrieSequence.SIZE / 2,
        2 * TrieSequence.SIZE }) {
      final Integer[] arr = new Integer[len];
      for(int j = 0; j < len; j++) arr[j] = j;
      final TrieSequence<Integer> seq = TrieSequence.from(arr);
      assertEquals("size", len, seq.size());
      for(int j = 0; j < len; j++) assertEquals("element", (Integer) j, seq.get(j));
    }
  }

  /** Tests if sequences can be created from {@link Iterable}s. */
  @Test public void fromIterable() {
    assertSame(TrieSequence.empty(), TrieSequence.from(Collections.emptyList()));
    final TrieSequence<Integer> single = TrieSequence.singleton(42);
    assertSame(single, TrieSequence.from(single));
    for(int len : new int[] { 1, TrieSequence.SIZE + TrieSequence.SIZE / 2,
        2 * TrieSequence.SIZE }) {
      final List<Integer> list = new ArrayList<Integer>(len);
      for(int j = 0; j < len; j++) list.add(j);
      final TrieSequence<Integer> seq = TrieSequence.from(list);
      assertEquals("size", len, seq.size());
      for(int j = 0; j < len; j++) assertEquals("element", (Integer) j, seq.get(j));
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
}
