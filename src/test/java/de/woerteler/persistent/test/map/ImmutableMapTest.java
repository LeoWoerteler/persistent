package de.woerteler.persistent.test.map;

import static de.woerteler.persistent.test.TrieSequenceTest.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.woerteler.persistent.Persistent;
import de.woerteler.persistent.PersistentSequence;
import de.woerteler.persistent.map.ImmutableMap;
import de.woerteler.persistent.map.PersistentMap;
import de.woerteler.persistent.map.PersistentMap.PersistentEntry;

/**
 * Tests for the {@link ImmutableMap} data structure.
 * @author Leo Woerteler
 */
public class ImmutableMapTest {
  /**
   * Convenience method for getting the right generic type.
   * @param numbers numbers to add
   * @return map
   */
  private static ImmutableMap<Number, Number> mapFrom(final Number... numbers) {
    ImmutableMap<Number, Number> map = ImmutableMap.empty();
    for(final Number n : numbers) {
      map = map.put(n, n);
    }
    return map;
  }

  /**
   * Constructs a map from the given array, interpreting the elements as alternating
   * sequence of keys an values.
   * @param prs key/value pairs
   * @return map
   */
  private static ImmutableMap<Number, Number> mapFromPairs(final Number... prs) {
    ImmutableMap<Number, Number> map = ImmutableMap.empty();
    for(int i = 0; i < prs.length; i++) {
      map = map.put(prs[i], prs[++i]);
    }
    return map;
  }

  /**
   * Constructs a {@link Map} from the given array, interpreting the elements as
   * alternating sequence of keys an values.
   * @param prs key/value pairs
   * @return map
   */
  private static Map<Number, Number> utilMap(final Number... prs) {
    final Map<Number, Number> map = new HashMap<Number, Number>(prs.length / 2);
    for(int i = 0; i < prs.length; i++) {
      map.put(prs[i], prs[++i]);
    }
    return map;
  }

  /** Tests for the empty sequence. */
  @Test public void testEmpty() {
    final ImmutableMap<Integer, Integer> map = ImmutableMap.empty();
    assertEquals("size", 0, map.size());
    assertSame("delete", map, map.remove(123));
    assertNull("lookup", map.get(123));
    assertFalse(map.containsKey(42));
    assertTrue(map.keySequence() == (PersistentSequence<?>) Persistent.empty());
    assertTrue(map.valueSequence() == (PersistentSequence<?>) Persistent.empty());
    assertTrue(map.entrySequence() == (PersistentSequence<?>) Persistent.empty());
  }

  /** Test for sequentially inserting elements. */
  @Test public void testInsert() {
    ImmutableMap<Integer, Integer> map = ImmutableMap.empty();
    for(int i = 0; i < 1234; i++) {
      map = map.put(i, i);
      assertEquals("size", i + 1, map.size());
      for(int j = i + 1; --j >= 0;) {
        assertEquals("lookup", (Integer) j, map.get(j));
      }
    }
  }

  /** Tests for hash collisions during insert. */
  @Test public void collision() {
    final Number a = 1, b = 1L, c = (short) 1;
    assertEquals("hash codes", a.hashCode(), b.hashCode());
    assertFalse(a.equals(b));
    final ImmutableMap<Number, Number> map = mapFrom(a, b, c);
    assertEquals("size", 3, map.size());
  }

  /** Tests if non-colliding keys are correctly deleted. */
  @Test public void deleteLeaf() {
    ImmutableMap<Integer, Integer> map = ImmutableMap.empty();
    for(int i = 0; i < 1000; i++) {
      map = map.put(i, i);
    }
    for(int i = 0; i < 1000; i++) {
      map = map.remove(i);
      assertEquals("size", map.size(), 1000 - i - 1);
    }
  }

  /** Tests if the right element is deleted in case of collisions. */
  @Test public void deleteCollision() {
    PersistentMap<Number, Number> map = ImmutableMap.empty();
    for(int i = 0; i < 1000; i++) {
      map = map.putAll(mapFrom((short) i, i, (long) i));
      assertEquals("size", 3 * (i + 1), map.size());
    }

    for(int i = 0; i < 1000; i++) {
      map = map.remove((short) i);
      assertEquals("size", map.size(), 3000 - 3 * i - 1);
      map = map.remove(i);
      assertEquals("size", map.size(), 3000 - 3 * i - 2);
      map = map.remove((long) i);
      assertEquals("size", map.size(), 3000 - 3 * i - 3);
    }
  }

  /** If a key is inserted into a single leaf node, it has to be split. */
  @Test public void splitLeaf() {
    final int size = 1 << ImmutableMap.BITS;
    assertEquals(2, ImmutableMap.singleton(0, 0).put(size, size).size());
  }

  /** If a key is inserted into a single leaf node, it has to be split. */
  @Test public void splitList() {
    final int size = 1 << (2 * ImmutableMap.BITS);
    final ImmutableMap<Number, Number> list =
        ImmutableMap.<Number, Number>empty().put(0, 0).put(0L, 0L),
        ins = list.put(size, size);
    assertEquals(3, ins.size());
    assertEquals(0, ins.get(0));
    assertEquals(size, ins.get(size));
    assertSame(Long.class, ins.get(0L).getClass());
  }

  /** If a branch has only one child after a deletion, it is replaced by its child. */
  @Test public void flattenBranch() {
    final int s = 1 << ImmutableMap.BITS;
    assertEquals(1, ImmutableMap.singleton(0, 0).put(s, s).remove(0).size());
  }

  /** If the single child of a branch is also a branch, it can't be flattened. */
  @Test public void dontFlattenNestedBranch() {
    final int s = 1 << (2 * ImmutableMap.BITS);
    assertEquals(2, ImmutableMap.singleton(0, 0).put(s, 0).put(1, 1
        ).remove(1).size());
  }

  /** Tests that colliding elements aren't found in leaves. */
  @Test public void getNonExLeaf() {
    final ImmutableMap<Number, Number> leaf =
        ImmutableMap.<Number, Number>singleton(42, 42);
    assertNull("collision", leaf.get(42L));
    assertNull("not contained", leaf.get(0));
  }

  /** Tries to delete a colliding non-existent element from a leaf. */
  @Test public void deleteNonExLeaf() {
    final ImmutableMap<Number, Number> leaf =
        ImmutableMap.<Number, Number>singleton(42, 42);
    assertSame("collision", leaf, leaf.remove(42L));
    assertSame("not contained", leaf, leaf.remove(0));
  }

  /** Tests that colliding elements aren't found in overflow lists. */
  @Test public void getNonExList() {
    final ImmutableMap<Number, Number> list =
        ImmutableMap.<Number, Number>singleton(42, 42).put(42L, 42L);
    assertNull("collision", list.get((short) 42));
    assertNull("not contained", list.get(0));
  }

  /** Tries to delete a colliding non-existent element from an overflow list. */
  @Test public void deleteNonExList() {
    final ImmutableMap<Number, Number> list =
        ImmutableMap.<Number, Number>singleton(42, 42).put(42L, 42L);
    assertSame("not contained", list, list.remove(0));
    assertSame("collision", list, list.remove((short) 42));
  }

  /** Tests that colliding elements aren't found under branches. */
  @Test public void getNonExBranch() {
    final ImmutableMap<Number, Number> leaf =
        ImmutableMap.<Number, Number>singleton(0, 0).put(0L, 0L).put(
            1, 1).put(1L, 1L);
    assertNull("collision", leaf.get((short) 1));
    assertNull("not contained", leaf.get((short) 2));
  }

  /** Tries to delete a colliding non-existent element from a branch. */
  @Test public void deleteNonExBranch() {
    final ImmutableMap<Number, Number> leaf =
        ImmutableMap.<Number, Number>singleton(0, 0).put(0L, 0L).put(
            1, 1).put(1L, 1L);
    assertSame("collision", leaf, leaf.remove((short) 1));
    assertSame("not contained", leaf, leaf.remove((short) 2));
  }

  /** Tests the @link {@link ImmutableMap#containsKey(Object)} method for leaf nodes. */
  @Test public void leafContains() {
    final ImmutableMap<Number, Number> leaf =
        ImmutableMap.<Number, Number>singleton(42, 42);
    assertTrue("contained", leaf.containsKey(42));
    assertFalse("not contained", leaf.containsKey(0));
    assertFalse("collision", leaf.containsKey(42L));
  }

  /** Tests the @link {@link ImmutableMap#containsKey(Object)} method for overflow lists. */
  @Test public void listContains() {
    final ImmutableMap<Number, Number> list =
        ImmutableMap.<Number, Number>singleton(42, 0).put((short) 42, 0);
    assertTrue("contained", list.containsKey(42));
    assertTrue("contained2", list.containsKey((short) 42));
    assertFalse("not contained", list.containsKey(0));
    assertFalse("collision", list.containsKey(42L));
  }

  /** Tests the @link {@link ImmutableMap#containsKey(Object)} method for branch nodes. */
  @Test public void branchContains() {
    final ImmutableMap<Number, Number> branch =
        ImmutableMap.<Number, Number>singleton(0, 0).put(1, 0);
    assertTrue("contained", branch.containsKey(0));
    assertTrue("contained2", branch.containsKey(1));
    assertFalse("not contained", branch.containsKey(42));
    assertFalse("collision", branch.containsKey(1L));
  }

  /** Tests if the empty map is unique. */
  @Test public void uniqueEmpty() {
    assertSame("not same", mapFrom(1).remove(1), ImmutableMap.empty());
  }

  /** Replacing a value in a leaf. */
  @Test public void replaceLeaf() {
    final ImmutableMap<Integer, Integer> map1 = ImmutableMap.singleton(0, 0),
        map2 = map1.put(0, 42);
    assertEquals("size1", 1, map1.size());
    assertEquals("size2", 1, map2.size());
    assertEquals("get", 0, (int) map1.get(0));
    assertEquals("get", 42, (int) map2.get(0));
  }

  /** Replacing a value in an overflow list. */
  @Test public void replaceList() {
    final ImmutableMap<Number, Number> list1 = ImmutableMap.<Number, Number>singleton(42,
        0).put((short) 42, 0).put(42L, 0), list2 = list1.put(42, 42);
    assertEquals("size1", 3, list1.size());
    assertEquals("size2", 3, list2.size());
    assertEquals("get", 0, list1.get(42));
    assertEquals("get", 42, list2.get(42));
  }

  /** Tests {@link ImmutableMap#putAll(PersistentMap)} of a leaf into a leaf. */
  @Test public void addLeafLeaf() {
    final ImmutableMap<Number, Number> leaf = mapFrom(0),
        replace = mapFromPairs(0, 1), conflict = mapFromPairs(0L, 2),
        other = mapFromPairs(42, 3), split = mapFromPairs(1 << ImmutableMap.BITS, 4);
    assertEquals(leaf.putAll(replace), replace);
    assertEquals(leaf.putAll(conflict), mapFromPairs(0, 0, 0L, 2));
    assertEquals(leaf.putAll(other), mapFromPairs(0, 0, 42, 3));
    assertEquals(leaf.putAll(split), mapFromPairs(0, 0, 1 << ImmutableMap.BITS, 4));
  }

  /**
   * Tests {@link ImmutableMap#putAll(PersistentMap)} of an overflow list into a
   * leaf.
   */
  @Test public void addLeafList() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> leaf = mapFrom(0),
        replace = mapFromPairs(0, 1, 0L, 1), conflict = mapFromPairs(0L, 2, (short) 0, 2),
        other = mapFromPairs(42, 3, 42L, 3), split = mapFromPairs(next, 4, 1L * next, 4);
    assertEquals(leaf.putAll(replace), replace);
    assertEquals(leaf.putAll(conflict), mapFromPairs(0, 0, 0L, 2, (short) 0, 2));
    assertEquals(leaf.putAll(other), mapFromPairs(0, 0, 42, 3, 42L, 3));
    assertEquals(leaf.putAll(split), mapFromPairs(0, 0, next, 4, 1L * next, 4));
  }

  /** Tests {@link ImmutableMap#putAll(PersistentMap)} of a branch into a leaf. */
  @Test public void addLeafBranch() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> leaf = mapFrom(0),
        replace = mapFromPairs(0, 1, 1, 1), conflict = mapFromPairs(0L, 2, 1, 2),
        other = mapFromPairs(1, 3, 2, 3), split = mapFromPairs(next, 4, next + 1, 4);
    assertEquals(leaf.putAll(replace), replace);
    assertEquals(leaf.putAll(conflict), mapFromPairs(0, 0, 0L, 2, 1, 2));
    assertEquals(leaf.putAll(other), mapFromPairs(0, 0, 1, 3, 2, 3));
    assertEquals(leaf.putAll(split), mapFromPairs(0, 0, next, 4, next + 1, 4));
  }

  /**
   * Tests {@link ImmutableMap#putAll(PersistentMap)} of a leaf into an overflow
   * list.
   */
  @Test public void addListLeaf() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> list = mapFrom(0, 0L),
        replace = mapFromPairs(0, 1), conflict = mapFromPairs((short) 0, 2),
        other = mapFromPairs(1, 3), split = mapFromPairs(next, 4);
    assertEquals(list.putAll(replace), mapFromPairs(0, 1, 0L, 0L));
    assertEquals(list.putAll(conflict), mapFromPairs(0, 0, 0L, 0L, (short) 0, 2));
    assertEquals(list.putAll(other), mapFromPairs(0, 0, 0L, 0L, 1, 3));
    assertEquals(list.putAll(split), mapFromPairs(0, 0, 0L, 0L, next, 4));
  }

  /**
   * Tests {@link ImmutableMap#putAll(PersistentMap)} of an overflow list into
   * an overflow list.
   */
  @Test public void addListList() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> list = mapFrom(0, 0L),
        replace = mapFromPairs(0, 1, 0L, 1),
        conflict = mapFromPairs((short) 0, 2, (byte) 0, 2),
        other = mapFromPairs(1, 3, 1L, 3),
        split = mapFromPairs(next, 4, 1L * next, 4);
    assertEquals(list.putAll(replace), replace);
    assertEquals(list.putAll(conflict),
        mapFromPairs(0, 0, 0L, 0L, (short) 0, 2, (byte) 0, 2));
    assertEquals(list.putAll(other), mapFromPairs(0, 0, 0L, 0L, 1, 3, 1L, 3));
    assertEquals(list.putAll(split), mapFromPairs(0, 0, 0L, 0L, next, 4, 1L * next, 4));
  }

  /**
   * Tests {@link ImmutableMap#putAll(PersistentMap)} of a branch into an
   * overflow list.
   */
  @Test public void addListBranch() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> list = mapFrom(0, 0L),
        replace = mapFromPairs(0, 1, 0L, 1, 1, 1),
        conflict = mapFromPairs((short) 0, 2, 1, 2),
        other = mapFromPairs(1, 3, 2, 3),
        split = mapFromPairs(next, 4, next + 1, 4);
    assertEquals(list.putAll(replace), replace);
    assertEquals(list.putAll(conflict), mapFromPairs(0, 0, 0L, 0L, (short) 0, 2, 1, 2));
    assertEquals(list.putAll(other), mapFromPairs(0, 0, 0L, 0L, 1, 3, 2, 3));
    assertEquals(list.putAll(split), mapFromPairs(0, 0, 0L, 0L, next, 4, next + 1, 4));
  }

  /**
   * Tests {@link ImmutableMap#putAll(PersistentMap)} of a branch into an
   * overflow list.
   */
  @Test public void addBranchLeaf() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> branch = mapFromPairs(0, 0, 1, 0),
        replace = mapFromPairs(0, 1),
        conflict = mapFromPairs((short) 0, 2),
        other = mapFromPairs(2, 3),
        split = mapFromPairs(next, 4);
    assertEquals(branch.putAll(replace), mapFromPairs(0, 1, 1, 0));
    assertEquals(branch.putAll(conflict), mapFromPairs(0, 0, 1, 0, (short) 0, 2));
    assertEquals(branch.putAll(other), mapFromPairs(0, 0, 1, 0, 2, 3));
    assertEquals(branch.putAll(split), mapFromPairs(0, 0, 1, 0, next, 4));
  }

  /**
   * Tests {@link ImmutableMap#putAll(PersistentMap)} of a branch into an
   * overflow list.
   */
  @Test public void addBranchList() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> branch = mapFromPairs(0, 0, 1, 0),
        replace = mapFromPairs(0, 1, 0L, 1),
        conflict = mapFromPairs((short) 0, 2, (byte) 0, 2),
        other = mapFromPairs(2, 3, 2L, 3),
        split = mapFromPairs(next, 4, 1L * next, 4);
    assertEquals(branch.putAll(replace), mapFromPairs(0, 1, 0L, 1, 1, 0));
    assertEquals(branch.putAll(conflict),
        mapFromPairs(0, 0, 1, 0, (short) 0, 2, (byte) 0, 2));
    assertEquals(branch.putAll(other), mapFromPairs(0, 0, 1, 0, 2, 3, 2L, 3));
    assertEquals(branch.putAll(split), mapFromPairs(0, 0, 1, 0, next, 4, 1L * next, 4));
  }

  /**
   * Tests {@link ImmutableMap#putAll(PersistentMap)} of a branch into an
   * overflow list.
   */
  @Test public void addBranchBranch() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> branch = mapFromPairs(0, 0, 1, 0),
        replace = mapFromPairs(0, 1, 1, 1),
        conflict = mapFromPairs(0L, 2, 1L, 2),
        other = mapFromPairs(2, 3, 3, 3),
        split = mapFromPairs(next, 4, next + 1, 4);
    assertEquals(branch.putAll(replace), replace);
    assertEquals(branch.putAll(conflict), mapFromPairs(0, 0, 1, 0, 0L, 2, 1L, 2));
    assertEquals(branch.putAll(other), mapFromPairs(0, 0, 1, 0, 2, 3, 3, 3));
    assertEquals(branch.putAll(split), mapFromPairs(0, 0, 1, 0, next, 4, next + 1, 4));
  }

  /**
   * Tests {@link ImmutableMap#putAll(PersistentMap)} of nodes into an empty
   * node.
   */
  @SuppressWarnings("unchecked")
  @Test public void addEmpty() {
    final ImmutableMap<Number, Number> empty = mapFrom(), leaf = mapFromPairs(0, 1),
        list = mapFromPairs(0, 2, 0L, 2), branch = mapFromPairs(0, 3, 1, 3);
    for(final ImmutableMap<Number, Number> map
        : Arrays.asList(empty, leaf, list, branch)) {
      assertSame(map, empty.putAll(map));
      assertSame(map, map.putAll(empty));
      assertEquals("argument: " + map.toString(), empty == map, empty.equals(map));
      assertEquals("reveiver: " + map.toString(), empty == map, map.equals(empty));
    }
  }

  /** Tests {@link ImmutableMap#equals(Object)} between different node types. */
  @SuppressWarnings("unchecked")
  @Test public void crossEquals() {
    final ImmutableMap<Number, Number> empty = mapFrom(), leaf = mapFromPairs(0, 1),
        list = mapFromPairs(0, 2, 0L, 2), branch = mapFromPairs(0, 3, 1, 3);
    for(final ImmutableMap<Number, Number> map
        : Arrays.asList(empty, leaf, list, branch)) {
      assertFalse(map.equals(null));
      for(final ImmutableMap<Number, Number> map2
          : Arrays.asList(empty, leaf, list, branch)) {
        assertEquals(map == map2, map.equals(map2));
      }
    }
  }

  /** Tests {@link ImmutableMap#equals(Object)} between different node types. */
  @Test public void leafEquals() {
    final ImmutableMap<Number, Number> leaf = mapFrom(0), wNull = mapFromPairs(0, null);
    assertTrue(equalsWithHash(leaf, mapFrom(0)));
    assertTrue(equalsWithHash(wNull, mapFromPairs(0, null)));

    assertFalse("collision", leaf.equals(mapFrom(0L)));
    assertFalse("not contained", leaf.equals(mapFrom(1)));
    assertFalse("different value", leaf.equals(mapFromPairs(0, 1)));
    assertFalse("value null", leaf.equals(wNull));
    assertFalse("own value null", wNull.equals(leaf));
    assertFalse("not a leaf", mapFrom(0, 1, 1L).equals(mapFrom(0, 0L, 1)));
  }

  /** Tests {@link ImmutableMap#equals(Object)} between different node types. */
  @Test public void listEquals() {
    final ImmutableMap<Number, Number> list = mapFrom(0, 0L),
        withNull = mapFromPairs(0, null, 0L, 0L);
    assertTrue(equalsWithHash(list, mapFrom(0, 0L)));
    assertTrue(equalsWithHash(list, mapFrom(0L, 0)));
    assertTrue(equalsWithHash(withNull, mapFromPairs(0, null, 0L, 0L)));

    assertFalse("collision", list.equals(mapFromPairs(0, 0, (short) 0, 0L)));
    assertFalse("not contained", list.equals(mapFrom(1, 0L)));
    assertFalse("different value", list.equals(mapFromPairs(0, 0, 0L, 1)));
    assertFalse("value null", list.equals(withNull));
    assertFalse("own value null", withNull.equals(list));
    assertFalse("different lengths",
        mapFrom(0, 0L, 1, 1L).equals(mapFrom(0, 0L, (short) 0, 1)));
    assertFalse("different hash", list.equals(mapFrom(1, 1L)));
  }

  /** Tests {@link ImmutableMap#equals(Object)} between different node types. */
  @Test public void branchEquals() {
    final ImmutableMap<Number, Number> branch = mapFrom(0, 1);
    assertTrue(equalsWithHash(branch, mapFrom(0, 1)));

    assertFalse("different nodes", branch.equals(mapFrom(0, 0L)));
    assertFalse("different usage", branch.equals(mapFrom(0, 2)));
    assertFalse("different children", branch.equals(mapFrom(0, 1L)));
  }

  /** Tests the {@link ImmutableMap#from(Map)} method. */
  @Test public void fromMap() {
    assertSame(ImmutableMap.EMPTY, ImmutableMap.from(Collections.emptyMap()));
    assertEquals("leaf", mapFrom(0), ImmutableMap.from(utilMap(0, 0)));
    assertEquals("leaf null", mapFromPairs(0, null), ImmutableMap.from(utilMap(0, null)));
    assertEquals("list", mapFrom(0, 0L), ImmutableMap.from(utilMap(0, 0, 0L, 0L)));
    assertEquals("list null", mapFromPairs(0, 0, 0L, null),
        ImmutableMap.from(utilMap(0, 0, 0L, null)));
    assertEquals("branch", mapFrom(0, 1), ImmutableMap.from(utilMap(0, 0, 1, 1)));
  }

  /** Tests the sequence keys. */
  @Test
  public void toSequenceKeys() {
    final int size = 1000;
    PersistentMap<Long, Integer> map = ImmutableMap.empty();
    for(int i = 0; i < size; ++i) {
      map = map.put((long) i, i);
      map = map.put((long) (i + 1) * size, i);
    }
    // hash collisions
    map = map.put(1L << 32, 1);
    map = map.put(2L << 32, 2);
    assertEquals(map.size(), 2 * size + 2);
    final PersistentSequence<Long> keys = map.keySequence();
    final PersistentSequence<Integer> values = map.valueSequence();
    final PersistentSequence<PersistentEntry<Long, Integer>> entries = map.entrySequence();
    assertEquals(keys.size(), 2 * size + 2);
    for(int i = 0; i < size; ++i) {
      assertTrue(keys.contains((long) i));
      assertTrue(keys.contains((long) (i + 1) * size));
    }
    assertTrue(keys.contains(1L << 32));
    assertTrue(keys.contains(2L << 32));
    // check corresponding sequences -- this relies heavily on
    // all sequences having the same arbitrary order
    final Iterator<Long> itl = keys.iterator();
    final Iterator<Integer> iti = values.iterator();
    final Iterator<PersistentEntry<Long, Integer>> ite = entries.iterator();
    final Object[] ea = entries.toArray();
    @SuppressWarnings("unchecked")
    final PersistentEntry<Long, Integer>[] es = entries.toArray(
        (PersistentEntry<Long, Integer>[]) new PersistentEntry<?, ?>[entries.size()]);
    @SuppressWarnings("unchecked")
    final PersistentEntry<Long, Integer>[] es2 = entries.toArray(
        (PersistentEntry<Long, Integer>[]) new PersistentEntry<?, ?>[0]);
    assertEquals(keys.size(), values.size());
    assertEquals(values.size(), entries.size());
    PersistentEntry<Long, Integer> prev = null;
    for(int pos = 0; pos < keys.size(); ++pos) {
      assertTrue(itl.hasNext());
      final long li = itl.next();
      assertTrue(iti.hasNext());
      final int ii = iti.next();
      assertTrue(ite.hasNext());
      final PersistentEntry<Long, Integer> ei = ite.next();
      final long ld = keys.get(pos);
      final int id = values.get(pos);
      final PersistentEntry<Long, Integer> ed = entries.get(pos);
      assertEquals(ed, es[pos]);
      assertEquals(ed, es2[pos]);
      assertEquals(ed, ea[pos]);
      assertEquals(li, ld);
      assertEquals((long) ei.getKey(), li);
      assertEquals((long) ed.getKey(), (long) ei.getKey());
      assertEquals(ld, (long) ed.getKey());
      assertEquals(ii, id);
      assertEquals((int) ei.getValue(), ii);
      assertEquals((int) ed.getValue(), (int) ei.getValue());
      assertEquals(id, (int) ed.getValue());
      assertEquals(ei, ed);
      assertEquals(ei.hashCode(), ed.hashCode());
      assertEquals(ei, ei);
      assertFalse(ei.equals(prev));
      prev = ei;
    }
    assertFalse(itl.hasNext());
    assertFalse(iti.hasNext());
    assertFalse(ite.hasNext());
    try {
      itl.next();
      fail("should throw a no such element exception");
    } catch(final NoSuchElementException e) {
      // ok
    }
    try {
      itl.remove();
      fail("should throw a unsupported operation exception");
    } catch(final UnsupportedOperationException e) {
      // ok
    }
    try {
      keys.get(-1);
      fail("should throw a index out of bounds exception");
    } catch(final IndexOutOfBoundsException e) {
      // ok
    }
    try {
      keys.get(keys.size());
      fail("should throw a index out of bounds exception");
    } catch(final IndexOutOfBoundsException e) {
      // ok
    }
    // sanity -- toString() must not throw an exception
    map.toString();
    keys.toString();
    keys.add(500L).toString();
  }

  /**
   * Does a sequence sanity check under the assumption that no two elements are
   * the same.
   * 
   * @param <T> The type of sequence.
   * @param seq The sequence.
   */
  private static <T> void fullSequenceSanity(final PersistentSequence<T> seq) {
    for(int i = 0; i < seq.size(); ++i) {
      // commutative
      for(int j = 0; j < seq.size(); ++j) {
        if(i == j) {
          assertTrue(seq.get(i).equals(seq.get(j)));
        } else {
          assertFalse(seq.get(i).equals(seq.get(j)));
        }
      }
    }
  }

  /**
   * Ensures that <code>null</code> values can be inserted and deleted from a
   * map.
   */
  @Test
  public void nullEntries() {
    PersistentMap<Integer, Integer> map = ImmutableMap.empty();
    map = map.put(null, 5);
    map = map.put(3, null);
    map = map.put(10, 20);
    assertTrue(map.containsKey(null));
    assertTrue(map.containsKey(3));
    assertFalse(map.containsKey(5));
    assertEquals((int) map.get(null), 5);
    assertEquals(map.remove(0), map);
    assertNull(map.get(3));
    final PersistentSequence<Integer> keys = map.keySequence();
    assertTrue(keys.contains(null));
    assertTrue(keys.contains(3));
    assertFalse(keys.contains(5));
    final PersistentSequence<Integer> values = map.valueSequence();
    assertTrue(values.contains(null));
    assertTrue(values.contains(5));
    assertFalse(values.contains(3));
    fullSequenceSanity(map.entrySequence());
    map = map.put(null, null);
    assertNull(map.get(null));
    assertTrue(map.containsKey(null));
    fullSequenceSanity(map.entrySequence());
    map = map.remove(null);
    assertFalse(map.containsKey(null));
    assertFalse(map.keySequence().contains(null));
    fullSequenceSanity(map.entrySequence());
  }

  /**
   * Ensures that <code>null</code> values can be inserted and deleted from a
   * map with collisions.
   */
  @Test
  public void nullCollision() {
    PersistentMap<Integer, Integer> map = ImmutableMap.empty();
    map = map.put(0, 4);
    map = map.put(null, 5);
    map = map.put(3, null);
    assertTrue(map.containsKey(null));
    assertTrue(map.containsKey(3));
    assertTrue(map.containsKey(0));
    assertFalse(map.containsKey(5));
    assertEquals((int) map.get(null), 5);
    assertEquals((int) map.get(0), 4);
    assertNull(map.get(3));
    fullSequenceSanity(map.entrySequence());
    final PersistentSequence<Integer> keys = map.keySequence();
    assertTrue(keys.contains(null));
    assertTrue(keys.contains(0));
    assertTrue(keys.contains(3));
    assertFalse(keys.contains(5));
    final PersistentSequence<Integer> values = map.valueSequence();
    assertTrue(values.contains(null));
    assertTrue(values.contains(4));
    assertTrue(values.contains(5));
    assertFalse(values.contains(3));
    map = map.put(null, null);
    map = map.put(null, 20);
    map = map.put(null, null);
    assertNull(map.get(null));
    assertEquals((int) map.get(0), 4);
    assertTrue(map.containsKey(0));
    assertTrue(map.containsKey(null));
    fullSequenceSanity(map.entrySequence());
    map = map.remove(null);
    assertFalse(map.containsKey(null));
    assertTrue(map.containsKey(0));
    assertEquals((int) map.get(0), 4);
    assertFalse(map.keySequence().contains(null));
    map = map.remove(0);
    map = map.put(null, 5);
    map = map.put(0, 4);
    assertTrue(map.containsKey(null));
    assertTrue(map.containsKey(0));
    fullSequenceSanity(map.entrySequence());
    map = map.remove(0);
    assertTrue(map.containsKey(null));
    assertFalse(map.containsKey(0));
    assertEquals((int) map.get(null), 5);
    assertNull(map.get(0));
    fullSequenceSanity(map.entrySequence());
    map = map.remove(null);
    assertFalse(map.containsKey(null));
    assertFalse(map.containsKey(0));
    assertNull(map.get(0));
    assertNull(map.get(null));
    fullSequenceSanity(map.entrySequence());
  }

  /**
   * Tests whether maps behave correctly when not containing <code>null</code>
   * values but asked if they do.
   */
  @Test
  public void nonNull() {
    final ImmutableMap<Integer, Integer> map0 = ImmutableMap.singleton(1, 5);
    assertNull(map0.get(null));
    assertFalse(map0.containsKey(null));
    assertEquals(map0.remove(null), map0);
    fullSequenceSanity(map0.entrySequence());
    ImmutableMap<Object, Integer> map1 = ImmutableMap.empty();
    map1 = map1.put(new Object() {
      @Override
      public int hashCode() {
        return 0;
      }
    }, 3);
    assertNull(map1.get(null));
    assertFalse(map1.containsKey(null));
    assertEquals(map1.remove(null), map1);
    map1 = map1.put(new Object() {
      @Override
      public int hashCode() {
        return 0;
      }
    }, 5);
    assertNull(map1.get(null));
    assertFalse(map1.containsKey(null));
    assertEquals(map1.remove(null), map1);
    assertEquals(map1.remove(new Object() {
      @Override
      public int hashCode() {
        return 0;
      }
    }), map1);
    map1 = map1.put(null, 20);
    assertEquals(map1.remove(new Object() {
      @Override
      public int hashCode() {
        return 0;
      }
    }), map1);
    fullSequenceSanity(map1.entrySequence());
  }

  /** Finds equal entries from different maps. */
  @Test
  public void equalEntries() {
    ImmutableMap<Integer, Integer> map0 = ImmutableMap.singleton(1, 5);
    map0 = map0.put(null, 4);
    map0 = map0.put(3, null);
    map0 = map0.put(6, null);
    map0 = map0.put(2, 6);
    ImmutableMap<Integer, Integer> map1 = ImmutableMap.singleton(2, 6);
    map1 = map1.put(6, null);
    map1 = map1.put(3, null);
    map1 = map1.put(null, 4);
    map1 = map1.put(1, 5);
    for(final PersistentEntry<Integer, Integer> e : map0.entrySequence()) {
      int matches = 0;
      for(final PersistentEntry<Integer, Integer> a : map1.entrySequence()) {
        if(e.equals(a)) {
          assertEquals(e.hashCode(), a.hashCode());
          ++matches;
        }
      }
      assertEquals(1, matches);
    }
  }

  /** No equal entries here. */
  @Test
  public void noEqualEntries() {
    ImmutableMap<Integer, Integer> map0 = ImmutableMap.singleton(1, 5);
    map0 = map0.put(null, 4);
    map0 = map0.put(3, null);
    map0 = map0.put(6, null);
    map0 = map0.put(2, 6);
    ImmutableMap<Integer, Integer> map1 = ImmutableMap.singleton(1, 6);
    map1 = map1.put(null, 3);
    map1 = map1.put(3, 5);
    map1 = map1.put(6, 4);
    map1 = map1.put(2, null);
    for(final PersistentEntry<Integer, Integer> e : map0.entrySequence()) {
      for(final PersistentEntry<Integer, Integer> a : map1.entrySequence()) {
        assertFalse(e.equals(a));
      }
    }
  }

}
