package de.woerteler.persistent.array.integer;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import de.woerteler.persistent.array.Array;

/**
 * Tests for the {@link IntArray} data structure.
 *
 * @author Leo Woerteler
 */
public class IntArrayTest {
  /**
   * Test for {@link IntArray#cons(Integer)} and {@link IntArray#snoc(Integer)}.
   */
  @Test
  public void consSnocTest() {
    final int n = 2_000_000;
    IntArray seq = IntArray.empty();
    for(int i = 0; i < n; i++) {
      seq = seq.cons(i).snoc(i);
    }

    assertEquals(2 * n, seq.size());
    for(int i = 0; i < 2 * n; i++) {
      final int diff = i - n, j = diff < 0 ? -(diff + 1) : diff;
      assertEquals(Integer.valueOf(j), seq.get(i));
    }
  }

  /**
   * Test for {@link IntArray#concat(Array)}.
   */
  @Test
  public void concatTest() {
    IntArray seq1 = IntArray.empty();
    IntArray seq2 = IntArray.empty();
    final int n = 2_000;
    for(int k = 0; k < n; k++) {
      seq1 = seq1.cons(k);
      seq2 = seq2.snoc(k);

      final int s = k + 1;
      assertEquals(s, seq1.size());
      assertEquals(s, seq2.size());

      final Array<Integer> seq11 = seq1.concat(seq1);
      assertEquals(2 * s, seq11.size());

      for(int i = 0; i < 2 * s; i++) {
        final int j = i < s ? s - i - 1 : 2 * s - i - 1;
        assertEquals(Integer.valueOf(j), seq11.get(i));
      }

      final Array<Integer> seq12 = seq1.concat(seq2);
      assertEquals(2 * s, seq12.size());

      for(int i = 0; i < 2 * s; i++) {
        final int j = i < s ? s - i - 1 : i - s;
        assertEquals(Integer.valueOf(j), seq12.get(i));
      }

      final Array<Integer> seq21 = seq2.concat(seq1);
      assertEquals(2 * s, seq21.size());

      for(int i = 0; i < 2 * s; i++) {
        final int j = i < s ? i : 2 * s - i - 1;
        assertEquals(Integer.valueOf(j), seq21.get(i));
      }

      final Array<Integer> seq22 = seq2.concat(seq2);
      assertEquals(2 * s, seq22.size());

      for(int i = 0; i < 2 * s; i++) {
        final int j = i < s ? i : i - s;
        assertEquals(Integer.valueOf(j), seq22.get(i));
      }
    }
  }

  /**
   * Test an {@link IntArray} used as a FIFO queue.
   */
  @Test
  public void queueLRTest() {
    final int n = 20_000_000, k = n / 100;
    IntArray seq = IntArray.empty();
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
   * Test an {@link IntArray} used as a FIFO queue.
   */
  @Test
  public void queueRLTest() {
    final int n = 20_000_000, k = n / 100;
    IntArray seq = IntArray.empty();
    for(int i = 0; i < k; i++) {
      seq = seq.snoc(i);
    }

    for(int i = k; i < n; i++) {
      assertEquals(k, seq.size());
      assertEquals(Integer.valueOf(i - k), seq.head());
      seq = seq.tail();
      seq = seq.snoc(i);
    }

    assertEquals(k, seq.size());
    for(int i = 0; i < k; i++) {
      assertEquals(Integer.valueOf(n - k + i), seq.head());
      seq = seq.tail();
      assertEquals(k - i - 1, seq.size());
    }

    assertTrue(seq.isEmpty());
  }

  /**
   * Test an {@link IntArray} used as a LIFO stack.
   */
  @Test
  public void stackLeftTest() {
    final int n = 20_000_000;
    IntArray seq = IntArray.empty();

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
   * Test an {@link IntArray} used as a LIFO stack.
   */
  @Test
  public void stackRightTest() {
    final int n = 20_000_000;
    IntArray seq = IntArray.empty();

    for(int i = 0; i < n; i++) {
      assertEquals(i, seq.size());
      seq = seq.snoc(i).snoc(i);
      assertEquals(Integer.valueOf(i), seq.last());
      seq = seq.init();
    }

    assertEquals(n, seq.size());

    for(int i = n; --i >= 0;) {
      assertEquals(Integer.valueOf(i), seq.last());
      seq = seq.init();
      assertEquals(i, seq.size());
    }

    assertTrue(seq.isEmpty());
  }

  /**
   * Test for {@link IntArray#insertBefore(long, Integer)}.
   */
  @Test
  public void insertTest() {
    final int n = 10_000;
    IntArray seq = IntArray.empty();

    for(int i = 0; i < n; i++) seq = seq.snoc(i);
    assertEquals(n, seq.size());

    for(int i = 0; i <= n; i++) {
      final IntArray seq2 = seq.insertBefore(i, n);
      assertEquals(Integer.valueOf(n), seq2.get(i));
      assertEquals(n + 1L, seq2.size());
      for(int j = 0; j < n; j++) {
        assertEquals(Integer.valueOf(j), seq2.get(j < i ? j : j + 1));
      }
    }
  }

  /**
   * Test for {@link IntArray#remove(long)}.
   */
  @Test
  public void removeTest() {
    final int n = 1000;
    IntArray seq = IntArray.empty();

    for(int k = 0; k < n; k++) {
      assertEquals(k, seq.size());
      for(int i = 0; i < k; i++) {
        final IntArray seq2 = seq.remove(i);
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
   * Test for {@link IntArray#iterator()}.
   */
  @Test
  public void iteratorTest() {
    final int n = 10_000;
    IntArray seq = IntArray.empty();
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

  /** Tests {@link IntArray#tail()}. */
  @Test
  public void tailTest() {
    IntArray seq = IntArray.empty();
    for(int i = 0; i < 15; i++) {
      seq = seq.snoc(i);
    }

    assertEquals(0, seq.head().intValue());
    assertEquals(15, seq.size());
    seq = seq.tail();
    assertEquals(1, seq.head().intValue());
    assertEquals(14, seq.size());
  }

  /** Tests {@link IntArray#from(int...)}. */
  @Test
  public void fromArrayTest() {
    final int n = 10_000;
    for(int k = 0; k < n; k++) {
      final int[] vals = new int[k];
      IntArray arr1 = IntArray.empty();
      for(int i = 0; i < k; i++) {
        vals[i] = i;
        arr1 = arr1.snoc(i);
      }

      assertEquals(k, arr1.size());
      final IntArray arr2 = IntArray.from(vals);
      assertEquals(k, arr2.size());
      assertEquals(arr1, arr2);
    }
  }
}
