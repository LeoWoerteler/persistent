package de.woerteler.persistent.array.object;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * Tests for {@link ObjectArrayBuilder}.
 *
 * @author Leo Woerteler
 */
public class ObjectArrayBuilderTest {

  /** Tests building arrays only with {@link ObjectArrayBuilder#append(Object)}. */
  @Test
  public void builderTestAscending() {
    for(int len = 0; len < 10_000; len++) {
      final ObjectArrayBuilder<Integer> builder = new ObjectArrayBuilder<>();
      for(int i = 0; i < len; i++) builder.append(i);
      final ObjectArray<Integer> arr = builder.freeze();
      arr.checkInvariants();
      assertEquals(len, arr.size());
      final Iterator<Integer> iter = arr.iterator();
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, iter.next().intValue());
      }
      assertFalse(iter.hasNext());
    }
  }

  /** Tests building arrays only with {@link ObjectArrayBuilder#prepend(Object)}. */
  @Test
  public void builderTestDescending() {
    for(int len = 0; len < 10_000; len++) {
      final ObjectArrayBuilder<Integer> builder = new ObjectArrayBuilder<>();
      for(int i = 0; i < len; i++) builder.prepend(len - 1 - i);
      final ObjectArray<Integer> arr = builder.freeze();
      arr.checkInvariants();
      assertEquals(len, arr.size());
      final Iterator<Integer> iter = arr.iterator();
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, iter.next().intValue());
      }
      assertFalse(iter.hasNext());
    }
  }

  /**
   * Tests building arrays only with {@link ObjectArrayBuilder#prepend(Object)} and
   * {@link ObjectArrayBuilder#append(Object)} in alternating order.
   */
  @Test
  public void builderTestAlternating() {
    for(int len = 0; len < 10_000; len++) {
      final ObjectArrayBuilder<Integer> builder = new ObjectArrayBuilder<>();

      final int mid = len / 2;
      if(len % 2 == 0) {
        for(int i = 0; i < len; i++) {
          if(i % 2 == 0) builder.prepend(mid - 1 - i / 2);
          else builder.append(mid + i / 2);
        }
      } else {
        for(int i = 0; i < len; i++) {
          if(i % 2 == 0) builder.prepend(mid - i / 2);
          else builder.append(mid + 1 + i / 2);
        }
      }

      final ObjectArray<Integer> arr = builder.freeze();
      arr.checkInvariants();
      assertEquals(len, arr.size());
      final Iterator<Integer> iter = arr.iterator();
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, iter.next().intValue());
      }
      assertFalse(iter.hasNext());
    }
  }

  /**
   * Tests building arrays only with {@link ObjectArrayBuilder#prepend(Object)} and
   * {@link ObjectArrayBuilder#append(Object)} in random order.
   */
  @Test
  public void builderTestRandom() {
    final Random rng = new Random(42);
    final ArrayDeque<Integer> deque = new ArrayDeque<>();
    for(int len = 0; len < 10_000; len++) {
      deque.clear();
      final ObjectArrayBuilder<Integer> builder = new ObjectArrayBuilder<>();

      for(int i = 0; i < len; i++) {
        final Integer val = i;
        if(rng.nextBoolean()) {
          builder.prepend(val);
          deque.addFirst(val);
        } else {
          builder.append(val);
          deque.addLast(val);
        }
      }

      final ObjectArray<Integer> arr = builder.freeze();
      arr.checkInvariants();
      assertEquals(len, arr.size());
      final Iterator<Integer> iter1 = deque.iterator(), iter2 = arr.iterator();
      while(iter1.hasNext()) {
        assertTrue(iter2.hasNext());
        assertEquals(iter1.next(), iter2.next());
      }
      assertFalse(iter2.hasNext());
    }
  }

  /** Tests {@link ObjectArray#from(Object...)}. */
  @Test
  public void fromArrayTest() {
    final int n = 10_000;
    for(int k = 0; k < n; k++) {
      final Integer[] vals = new Integer[k];
      ObjectArray<Integer> arr1 = ObjectArray.empty();
      for(int i = 0; i < k; i++) {
        vals[i] = i;
        arr1 = arr1.snoc(i);
      }

      assertEquals(k, arr1.size());
      final ObjectArray<Integer> arr2 = ObjectArray.from(vals);
      assertEquals(k, arr2.size());
      assertEquals(arr1, arr2);
    }
  }

  /** Creates a sequence by randomly invoking builder methods. */
  @Test
  public void fuzzyTest() {
    for(int test = 0; test < 100; test++) {
      final Random rng = new Random(42 + test);
      final ObjectArrayBuilder<Integer> builder = new ObjectArrayBuilder<>();
      int size = 100_000, l = 0, n = 0;
      do {
        final int action = rng.nextInt(4);
        if(action == 0) {
          // check iterator
          final Iterator<Integer> iter = builder.iterator();
          for(int i = 0; i < n; i++) {
            assertTrue(iter.hasNext());
            assertEquals(l + i, iter.next().intValue());
          }
          assertFalse(iter.hasNext());
        } else if(action == 1) {
          // prepend element
          builder.prepend(--l);
          n++;
        } else if(action == 2) {
          // append element
          builder.append(l + n++);
        } else {
          // append array
          final int k = rng.nextInt(Math.min(size - n, 1_000));
          builder.append(array(l + n, k));
          n += k;
        }
      } while(n < size);

      final ObjectArray<Integer> arr = builder.freeze();
      assertEquals(size, arr.size());
      final Iterator<Integer> iter = builder.iterator();
      for(int i = 0; i < size; i++) {
        assertTrue(iter.hasNext());
        assertEquals(l + i, iter.next().intValue());
      }
      assertFalse(iter.hasNext());
    }
  }

  /**
   * Creates an {@link ObjectArray} containing the {@code n} consecutive integers
   * starting with {@code start}.
   * @param start first integer
   * @param n number of consecutive integers
   * @return the object array
   */
  private static ObjectArray<Integer> array(final int start, final int n) {
    final Integer[] arr = new Integer[n];
    for(int i = 0; i < n; i++) arr[i] = start + i;
    return ObjectArray.from(arr);
  }
}
