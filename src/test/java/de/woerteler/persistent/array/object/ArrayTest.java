package de.woerteler.persistent.array.object;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import de.woerteler.persistent.array.*;

/**
 * Tests for the {@link Array} data structure.
 *
 * @author Leo Woerteler
 */
public class ArrayTest {
  /**
   * Test for {@link Array#cons(Object)} and {@link Array#snoc(Object)}.
   */
  @Test
  public void consSnocTest() {
    final long n = 2_000_000;
    Array<Long> seq = ObjectArray.empty();
    for(long i = 0; i < n; i++) {
      seq = seq.cons(i).snoc(i);
    }

    assertEquals(2 * n, seq.size());
    for(long i = 0; i < 2 * n; i++) {
      final long diff = i - n, j = diff < 0 ? -(diff + 1) : diff;
      assertEquals(Long.valueOf(j), seq.get(i));
    }
  }

  /**
   * Test for {@link Array#concat(Array)}.
   */
  @Test
  public void concatTest() {
    Array<Long> seq1 = ObjectArray.empty();
    Array<Long> seq2 = ObjectArray.empty();
    final long n = 2_000_000;
    for(long i = 0; i < n; i++) {
      seq1 = seq1.cons(i);
      seq2 = seq2.snoc(i);
    }

    assertEquals(n, seq1.size());
    assertEquals(n, seq2.size());
    final Array<Long> seq = seq1.concat(seq2);
    assertEquals(2 * n, seq.size());

    for(long i = 0; i < 2 * n; i++) {
      final long diff = i - n, j = diff < 0 ? -(diff + 1) : diff;
      assertEquals(Long.valueOf(j), seq.get(i));
    }
  }

  /**
   * Test an {@link Array} used as a FIFO queue.
   */
  @Test
  public void queueTest() {
    final int n = 20_000_000, k = n / 100;
    Array<Integer> seq = ObjectArray.empty();
    for(int i = 0; i < k; i++) {
      seq = seq.cons(i);
    }

    for(int i = k; i < n; i++) {
      assertEquals(k, seq.size());
      assertEquals(Integer.valueOf(i - k), seq.last());
      seq = seq.init();
      seq = seq.cons(i);
    }

    assertEquals(k, seq.size());
    for(int i = 0; i < k; i++) {
      assertEquals(Integer.valueOf(n - k + i), seq.last());
      seq = seq.init();
      assertEquals(k - i - 1, seq.size());
    }

    assertTrue(seq.isEmpty());
  }

  /**
   * Test an {@link Array} used as a LIFO stack.
   */
  @Test
  public void stackTest() {
    final int n = 20_000_000;
    Array<Integer> seq = ObjectArray.empty();

    for(int i = 0; i < n; i++) {
      assertEquals(i, seq.size());
      seq = seq.cons(i).cons(i);
      assertEquals(Integer.valueOf(i), seq.head());
      seq = seq.tail();
    }

    assertEquals(n, seq.size());

    for(int i = n; --i >= 0;) {
      assertEquals(Integer.valueOf(i), seq.head());
      seq = seq.tail();
      assertEquals(i, seq.size());
    }

    assertTrue(seq.isEmpty());
  }

  /**
   * Test for {@link Array#insertBefore(long, Object)}.
   */
  @Test
  public void insertTest() {
    final int n = 10_000;
    Array<Integer> seq = ObjectArray.empty();

    for(int i = 0; i < n; i++) seq = seq.snoc(i);
    assertEquals(n, seq.size());

    for(int i = 0; i <= n; i++) {
      final Array<Integer> seq2 = seq.insertBefore(i, n);
      assertEquals(Integer.valueOf(n), seq2.get(i));
      assertEquals(n + 1L, seq2.size());
      for(int j = 0; j < n; j++) {
        assertEquals(Integer.valueOf(j), seq2.get(j < i ? j : j + 1));
      }
    }
  }

  /**
   * Test for {@link Array#remove(long)}.
   */
  @Test
  public void removeTest() {
    final int n = 1000;
    Array<Integer> seq = ObjectArray.empty();

    for(int k = 0; k < n; k++) {
      assertEquals(k, seq.size());
      for(int i = 0; i < k; i++) {
        final Array<Integer> seq2 = seq.remove(i);
        assertEquals(k - 1, seq2.size());

        final Iterator<Integer> iter = seq2.iterator();
        for(int j = 0; j < k - 1; j++) {
          assertTrue(iter.hasNext());
          assertEquals(j < i ? j : j + 1, iter.next().intValue());
        }
        assertFalse(iter.hasNext());
      }
      seq = seq.snoc(k);
    }
  }

  /**
   * Test for {@link Array#iterator()}.
   */
  @Test
  public void iteratorTest() {
    final int n = 10_000;
    Array<Integer> seq = ObjectArray.empty();
    assertFalse(seq.iterator().hasNext());

    for(int i = 0; i < n; i++) {
      seq = seq.cons(i).snoc(i);
      final int k = 2 * (i + 1);
      final Iterator<Integer> iter = seq.iterator();
      for(int j = 0; j < k; j++) {
        assertTrue(iter.hasNext());
        final Integer val = iter.next();
        final int expected = j <= i ? i - j : j - (i + 1);
        assertEquals(expected, val.intValue());
      }
      assertFalse(iter.hasNext());
    }
  }

  /** Tests {@link Array#tail()}. */
  @Test
  public void tailTest() {
    Array<Integer> seq = ObjectArray.empty();
    for(int i = 0; i < 15; i++) {
      seq = seq.snoc(i);
    }

    assertEquals(0, seq.head().intValue());
    assertEquals(15, seq.size());
    seq = seq.tail();
    assertEquals(1, seq.head().intValue());
    assertEquals(14, seq.size());
  }

  /** Tests {@link ObjectArray#from(Object...)}. */
  @Test
  public void fromArrayTest() {
    final int n = 10_000;
    for(int k = 0; k < n; k++) {
      final Integer[] vals = new Integer[k];
      Array<Integer> arr1 = ObjectArray.empty();
      for(int i = 0; i < k; i++) {
        vals[i] = i;
        arr1 = arr1.snoc(i);
      }

      assertEquals(k, arr1.size());
      final Array<Integer> arr2 = ObjectArray.from(vals);
      assertEquals(k, arr2.size());
      assertEquals(arr1, arr2);
    }
  }
}
