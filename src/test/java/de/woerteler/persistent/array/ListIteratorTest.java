package de.woerteler.persistent.array;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * Tests for {@link Array#listIterator(long)}.
 *
 * @author Leo Woerteler
 * @param <A> array type
 */
public abstract class ListIteratorTest<A extends Array<Integer>> extends ArrayTest<A> {
  /** Random movements inside the array. */
  @Test public void randomTest() {
    final Random rng = new Random(1337);
    for(int n = 0; n < 1_000; n++) {
      Array<Integer> arr = emptyArray();
      final ArrayList<Integer> list = new ArrayList<Integer>(n);
      for(int i = 0; i < n; i++) {
        final int insPos = rng.nextInt(i + 1);
        arr = arr.insertBefore(insPos, i);
        list.add(insPos, i);
      }

      final ListIterator<Integer> it1 = arr.iterator(), it2 = list.listIterator();
      int pos = 0;
      for(int i = 0; i < 100; i++) {
        final int k = rng.nextInt(n + 1);
        if(rng.nextBoolean()) {
          for(int j = 0; j < k; j++) {
            assertEquals(pos, it2.nextIndex());
            assertEquals(pos, it1.nextIndex());
            assertEquals(pos - 1, it2.previousIndex());
            assertEquals(pos - 1, it1.previousIndex());
            if(it2.hasNext()) {
              assertTrue(it1.hasNext());
              final int exp = it2.next();
              final int got = it1.next();
              assertEquals(exp, got);
              pos++;
            } else {
              assertFalse(it1.hasNext());
              continue;
            }
          }
        } else {
          for(int j = 0; j < k; j++) {
            assertEquals(pos, it2.nextIndex());
            assertEquals(pos, it1.nextIndex());
            assertEquals(pos - 1, it2.previousIndex());
            assertEquals(pos - 1, it1.previousIndex());
            if(it2.hasPrevious()) {
              assertTrue(it1.hasPrevious());
              pos--;
              final int exp = it2.previous();
              final int got = it1.previous();
              assertEquals(exp, got);
            } else {
              assertFalse(it1.hasPrevious());
              continue;
            }
          }
        }
      }
    }
  }
}
