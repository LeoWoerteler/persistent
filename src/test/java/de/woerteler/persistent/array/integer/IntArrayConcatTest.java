package de.woerteler.persistent.array.integer;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import de.woerteler.persistent.array.*;

/**
 * Tests for  {@link Array#concat(Array)}.
 *
 * @author Leo Woerteler
 */
public class IntArrayConcatTest {

  /** Generates and concatenates random arrays of a given size. */
  @Test public void fuzzyTest() {
    final Random rng = new Random(42);
    for(int n = 0; n < 1000; n++) {
      for(int k = 0; k < 100; k++) {
        final int l = rng.nextInt(n + 1), r = n - l;

        IntArray a1 = IntArray.empty(), b1 = IntArray.empty();
        final ArrayList<Integer> a2 = new ArrayList<>(l), b2 = new ArrayList<>(r);
        for(int i = 0; i < l; i++) {
          final int pos = rng.nextInt(i + 1);
          a1 = a1.insertBefore(pos, i);
          a2.add(pos, i);
        }

        for(int i = 0; i < r; i++) {
          final int pos = rng.nextInt(i + 1);
          b1 = b1.insertBefore(pos, l + i);
          b2.add(pos, l + i);
        }

        a1 = (IntArray) a1.concat(b1);
        a1.checkInvariants();
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
