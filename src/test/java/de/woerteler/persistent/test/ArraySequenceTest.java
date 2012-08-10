package de.woerteler.persistent.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.woerteler.persistent.ArraySequence;
import de.woerteler.persistent.Persistent;
import de.woerteler.persistent.PersistentSequence;
import de.woerteler.persistent.TrieSequence;

/**
 * Tests for {@link ArraySequence}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class ArraySequenceTest {

  /** Tests random access features. */
  @Test
  public void indexing() {
    final PersistentSequence<Integer> seq = ArraySequence.from(1, 2, 3);
    assertEquals(seq.get(0), (Integer) 1);
    assertEquals(seq.get(1), (Integer) 2);
    assertEquals(seq.get(2), (Integer) 3);
    assertEquals(seq.size(), 3);
    final Collection<Integer> c = new ArrayList<Integer>();
    c.add(4);
    c.add(5);
    c.add(6);
    final PersistentSequence<Integer> col = ArraySequence.from(c);
    assertEquals(col.get(0), (Integer) 4);
    assertEquals(col.get(1), (Integer) 5);
    assertEquals(col.get(2), (Integer) 6);
    assertEquals(col.size(), 3);
  }

  /**
   * All operations resulting in an empty sequence must return the same
   * sequence.
   */
  @Test
  public void emptySeqCache() {
    final PersistentSequence<Object> e = Persistent.empty();
    assertTrue(e == ArraySequence.from());
    assertTrue(e == ArraySequence.from(new ArrayList<Object>()));
    assertTrue(e == TrieSequence.from());
    assertTrue(e == TrieSequence.from(new ArrayList<Object>()));
  }

  /** Tests if consecutive integers can be inserted. */
  @Test
  public void genInsert() {
    PersistentSequence<Integer> seq = ArraySequence.from(0);
    for(int i = 1; i < 12; i++) {
      assertEquals("Sequence size", i, seq.size());
      seq = seq.add(i);
    }
    for(int i = 0; i < 12; i++) {
      assertEquals((Integer) i, seq.get(i));
    }
  }

  /** Tests if a drained iterator throws {@link NoSuchElementException}. */
  @Test(expected = NoSuchElementException.class)
  public void drainedError() {
    final Iterator<Integer> it = ArraySequence.from(1).iterator();
    try {
      it.next();
    } catch(final Exception e) {
      e.printStackTrace();
      assertTrue("must not throw an exception", false);
    }
    it.next();
  }

  /**
   * Tests if {@link Iterator#remove()} throws
   * {@link UnsupportedOperationException}.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void removeError() {
    ArraySequence.from(1).iterator().remove();
  }

  /** Tests arrays. */
  @Test
  public void arrayTests() {
    final Integer[] arr = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
    final PersistentSequence<Integer> seq = ArraySequence.from(arr);
    final Object[] arrObj = seq.toArray();
    final Integer[] arr0 = seq.toArray(new Integer[0]);
    final Integer[] arr1 = seq.toArray(new Integer[seq.size()]);
    assertArrayEquals(arr, arr0);
    assertArrayEquals(arr, arr1);
    assertArrayEquals(arr, arrObj);
  }

  /** Tests multiple adds. */
  @Test
  public void multipleAdds() {
    final int size = 10000000;
    final PersistentSequence<Integer> seq = ArraySequence.from(new Integer[size]);
    for(int x = 0; x < 5; ++x) {
      for(int i = 0; i < 10000; ++i) {
        final PersistentSequence<Integer> tmp = seq.add(i);
        assertEquals(tmp.get(tmp.size() - 1), (Integer) i);
        assertEquals(tmp.size(), size + 1);
      }
      for(int i = 0; i < 2; ++i) {
        System.gc();
        System.runFinalization();
      }
    }
  }

}
