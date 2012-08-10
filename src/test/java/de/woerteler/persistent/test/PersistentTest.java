package de.woerteler.persistent.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import de.woerteler.persistent.ArraySequence;
import de.woerteler.persistent.Persistent;
import de.woerteler.persistent.TrieSequence;

/**
 * Tests the creation of persistent data structures.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class PersistentTest {

  /** Tests whether the correct types are created. */
  @Test
  public void testCorrectTypes() {
    assertEquals(Persistent.from(), Persistent.empty());
    assertTrue(Persistent.from(1, 2, 3) instanceof ArraySequence);
    final Collection<Integer> col = new ArrayList<Integer>();
    col.add(1);
    col.add(2);
    col.add(3);
    assertTrue(Persistent.from(col) instanceof ArraySequence);
    assertTrue(Persistent.from((Iterable<Integer>) col) instanceof ArraySequence);
    final Iterable<Integer> it = new Iterable<Integer>() {

      @Override
      public Iterator<Integer> iterator() {
        return col.iterator();
      }

    };
    assertTrue(Persistent.from(it) instanceof TrieSequence);
  }

}
