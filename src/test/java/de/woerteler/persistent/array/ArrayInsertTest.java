package de.woerteler.persistent.array;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * Tests for {@link Array#insertBefore(long, Object)}.
 *
 * @author Leo Woerteler
 * @param <A> array type
 */
public abstract class ArrayInsertTest<A extends Array<Integer>> extends ArrayTest<A> {
  /** Randomly insert elements and compare the result to an array list. */
  @Test
  @SuppressWarnings("unchecked")
  public void fuzzyTest() {
    final int n = 200_000;
    final ArrayList<Integer> list = new ArrayList<Integer>(n);
    A arr = emptyArray();

    final Random rng = new Random(42);
    for(int i = 0; i < n; i++) {
      final int insPos = rng.nextInt(i + 1);
      list.add(insPos, i);
      arr = (A) arr.insertBefore(insPos, i);
      final int size = i + 1;
      assertEquals(size, arr.size());
      assertEquals(size, list.size());

      if(i % 1000 == 999) {
        checkInvariants(arr);
        for(int j = 0; j < size; j++) {
          assertEquals(list.get(j), arr.get(j));
        }
      }
    }
  }
}
