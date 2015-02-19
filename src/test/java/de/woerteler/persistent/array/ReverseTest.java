package de.woerteler.persistent.array;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * Tests for {@link Array#reverse()}.
 *
 * @author Leo Woerteler
 * @param <A> array type
 */
public abstract class ReverseTest<A extends Array<Integer>> extends ArrayTest<A> {
  /** Traverses an array and its reverse from both ends. */
  @Test public void randomTest() {
    final Random rng = new Random(42);
    for(int n = 0; n < 10_000; n++) {
      Array<Integer> arr = emptyArray();
      for(int i = 0; i < n; i++) {
        arr = arr.insertBefore(rng.nextInt(i + 1), i);
      }
      assertEquals(n, arr.size());
      final Array<Integer> rev = arr.reverse();
      final ListIterator<Integer> af = arr.listIterator(0), ab = arr.listIterator(n);
      final ListIterator<Integer> rf = rev.listIterator(0), rb = rev.listIterator(n);
      for(int i = 0; i < n; i++) {
        assertTrue(af.hasNext());
        assertTrue(ab.hasPrevious());
        assertTrue(rf.hasNext());
        assertTrue(rb.hasPrevious());
        assertEquals(af.next(), rb.previous());
        assertEquals(ab.previous(), rf.next());
      }
      assertFalse(af.hasNext());
      assertFalse(ab.hasPrevious());
      assertFalse(rf.hasNext());
      assertFalse(rb.hasPrevious());
    }
  }
}
