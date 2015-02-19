package de.woerteler.persistent.array.integer;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * Tests for {@link IntArray#insertBefore(long, Integer)}.
 *
 * @author Leo Woerteler
 */
public class IntArrayInsertTest {

  /** Randomly insert elements and compare the result to an array list. */
  @Test
  public void fuzzyTest() {
    final int n = 200_000;
    final ArrayList<Integer> list = new ArrayList<Integer>(n);
    IntArray arr = IntArray.empty();

    final Random rng = new Random(42);
    for(int i = 0; i < n; i++) {
      final int insPos = rng.nextInt(i + 1);
      list.add(insPos, i);
      arr = arr.insertBefore(insPos, i);
      final int size = i + 1;
      assertEquals(size, arr.size());
      assertEquals(size, list.size());

      if(i % 1000 == 999) {
        final IntArray arr1 = arr;
        arr1.checkInvariants();
        for(int j = 0; j < size; j++) {
          assertEquals(list.get(j), arr.get(j));
        }
      }
    }
  }
}
