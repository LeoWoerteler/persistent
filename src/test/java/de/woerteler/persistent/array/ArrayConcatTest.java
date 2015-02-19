package de.woerteler.persistent.array;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * Tests for  {@link Array#concat(Array)}.
 *
 * @author Leo Woerteler
 * @param <A> array type
 */
public abstract class ArrayConcatTest<A extends Array<Integer>> extends ArrayTest<A> {
  /** Generates and concatenates random arrays of a given size. */
  @SuppressWarnings("unchecked")
  @Test public void fuzzyTest() {
    final Random rng = new Random(42);
    for(int n = 0; n < 1000; n++) {
      for(int k = 0; k < 100; k++) {
        final int l = rng.nextInt(n + 1), r = n - l;

        A a1 = emptyArray(), b1 = emptyArray();
        final ArrayList<Integer> a2 = new ArrayList<>(l), b2 = new ArrayList<>(r);
        for(int i = 0; i < l; i++) {
          final int pos = rng.nextInt(i + 1);
          a1 = (A) a1.insertBefore(pos, i);
          a2.add(pos, i);
        }

        for(int i = 0; i < r; i++) {
          final int pos = rng.nextInt(i + 1);
          b1 = (A) b1.insertBefore(pos, l + i);
          b2.add(pos, l + i);
        }

        a1 = (A) a1.concat(b1);
        checkInvariants(a1);
        a2.addAll(b2);
        assertEquals(n, a1.size());
        assertEquals(n, a2.size());

        final Iterator<Integer> it1 = a1.iterator(), it2 = a2.iterator();
        while(it1.hasNext()) {
          assertTrue(it2.hasNext());
          final int i1 = it1.next(), i2 = it2.next();
          assertEquals(i2, i1);
        }
        assertFalse(it2.hasNext());
      }
    }
  }
}
