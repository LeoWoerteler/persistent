package de.woerteler.persistent.array.object;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * Tests for {@link ObjectArrayBuilder}.
 *
 * @author Leo Woerteler
 */
public class ArrayBuilderTest {

  /** Tests building arrays only with {@link ObjectArrayBuilder#append(Object)}. */
  @Test
  public void builderTestAscending() {
    for(int len = 0; len < 10000; len++) {
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
    for(int len = 0; len < 10000; len++) {
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
    for(int len = 0; len < 10000; len++) {
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
}
