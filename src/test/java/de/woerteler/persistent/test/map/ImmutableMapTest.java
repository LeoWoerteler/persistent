package de.woerteler.persistent.test.map;

import static org.junit.Assert.*;
import static de.woerteler.persistent.test.TrieSequenceTest.equalsWithHash;

import java.util.*;

import org.junit.*;

import de.woerteler.persistent.map.*;

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
    for(final Number n : numbers) map = map.insert(n, n);
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
    for(int i = 0; i < prs.length; i++) map = map.insert(prs[i], prs[++i]);
    return map;
  }

  /**
   * Constructs a {@link Map} from the given array, interpreting the elements as
   * alternating sequence of keys an values.
   * @param prs key/value pairs
   * @return map
   */
  private static Map<Number, Number> utilMap(final Number... prs) {
    Map<Number, Number> map = new HashMap<Number, Number>(prs.length / 2);
    for(int i = 0; i < prs.length; i++) map.put(prs[i], prs[++i]);
    return map;
  }

  /** Tests for the empty sequence. */
  @Test public void testEmpty() {
    final ImmutableMap<Integer, Integer> map = ImmutableMap.empty();
    assertEquals("size", 0, map.size());
    assertSame("delete", map, map.delete(123));
    assertNull("lookup", map.get(123));
    assertFalse(map.contains(42));
  }

  /** Test for sequentially inserting elements. */
  @Test public void testInsert() {
    ImmutableMap<Integer, Integer> map = ImmutableMap.empty();
    for(int i = 0; i < 1234; i++) {
      map = map.insert(i, i);
      assertEquals("size", i + 1, map.size());
      for(int j = i + 1; --j >= 0;) assertEquals("lookup", (Integer) j, map.get(j));
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
    for(int i = 0; i < 1000; i++) map = map.insert(i, i);
    for(int i = 0; i < 1000; i++) {
      map = map.delete(i);
      assertEquals("size", map.size(), 1000 - i - 1);
    }
  }

  /** Tests if the right element is deleted in case of collisions. */
  @Test public void deleteCollision() {
    ImmutableMap<Number, Number> map = ImmutableMap.empty();
    for(int i = 0; i < 1000; i++) {
      map = map.addAll(mapFrom((short) i, i, (long) i));
      assertEquals("size", 3 * (i + 1), map.size());
    }

    for(int i = 0; i < 1000; i++) {
      map = map.delete((short) i);
      assertEquals("size", map.size(), 3000 - 3 * i - 1);
      map = map.delete(i);
      assertEquals("size", map.size(), 3000 - 3 * i - 2);
      map = map.delete((long) i);
      assertEquals("size", map.size(), 3000 - 3 * i - 3);
    }
  }

  /** If a key is inserted into a single leaf node, it has to be split. */
  @Test public void splitLeaf() {
    final int size = 1 << ImmutableMap.BITS;
    assertEquals(2, ImmutableMap.singleton(0, 0).insert(size, size).size());
  }

  /** If a key is inserted into a single leaf node, it has to be split. */
  @Test public void splitList() {
    final int size = 1 << (2 * ImmutableMap.BITS);
    final ImmutableMap<Number, Number> list =
        ImmutableMap.<Number, Number>empty().insert(0, 0).insert(0L, 0L),
        ins = list.insert(size, size);
    assertEquals(3, ins.size());
    assertEquals(0, ins.get(0));
    assertEquals(size, ins.get(size));
    assertSame(Long.class, ins.get(0L).getClass());
  }

  /** If a branch has only one child after a deletion, it is replaced by its child. */
  @Test public void flattenBranch() {
    final int s = 1 << ImmutableMap.BITS;
    assertEquals(1, ImmutableMap.singleton(0, 0).insert(s, s).delete(0).size());
  }

  /** If the single child of a branch is also a branch, it can't be flattened. */
  @Test public void dontFlattenNestedBranch() {
    final int s = 1 << (2 * ImmutableMap.BITS);
    assertEquals(2, ImmutableMap.singleton(0, 0).insert(s, 0).insert(1, 1
        ).delete(1).size());
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
    assertSame("collision", leaf, leaf.delete(42L));
    assertSame("not contained", leaf, leaf.delete(0));
  }

  /** Tests that colliding elements aren't found in overflow lists. */
  @Test public void getNonExList() {
    final ImmutableMap<Number, Number> list =
        ImmutableMap.<Number, Number>singleton(42, 42).insert(42L, 42L);
    assertNull("collision", list.get((short) 42));
    assertNull("not contained", list.get(0));
  }

  /** Tries to delete a colliding non-existent element from an overflow list. */
  @Test public void deleteNonExList() {
    final ImmutableMap<Number, Number> list =
        ImmutableMap.<Number, Number>singleton(42, 42).insert(42L, 42L);
    assertSame("not contained", list, list.delete(0));
    assertSame("collision", list, list.delete((short) 42));
  }

  /** Tests that colliding elements aren't found under branches. */
  @Test public void getNonExBranch() {
    final ImmutableMap<Number, Number> leaf =
        ImmutableMap.<Number, Number>singleton(0, 0).insert(0L, 0L).insert(
            1, 1).insert(1L, 1L);
    assertNull("collision", leaf.get((short) 1));
    assertNull("not contained", leaf.get((short) 2));
  }

  /** Tries to delete a colliding non-existent element from a branch. */
  @Test public void deleteNonExBranch() {
    final ImmutableMap<Number, Number> leaf =
        ImmutableMap.<Number, Number>singleton(0, 0).insert(0L, 0L).insert(
            1, 1).insert(1L, 1L);
    assertSame("collision", leaf, leaf.delete((short) 1));
    assertSame("not contained", leaf, leaf.delete((short) 2));
  }

  /** Tests the @link {@link ImmutableMap#contains(Object)} method for leaf nodes. */
  @Test public void leafContains() {
    final ImmutableMap<Number, Number> leaf =
        ImmutableMap.<Number, Number>singleton(42, 42);
    assertTrue("contained", leaf.contains(42));
    assertFalse("not contained", leaf.contains(0));
    assertFalse("collision", leaf.contains(42L));
  }

  /** Tests the @link {@link ImmutableMap#contains(Object)} method for overflow lists. */
  @Test public void listContains() {
    final ImmutableMap<Number, Number> list =
        ImmutableMap.<Number, Number>singleton(42, 0).insert((short) 42, 0);
    assertTrue("contained", list.contains(42));
    assertTrue("contained2", list.contains((short) 42));
    assertFalse("not contained", list.contains(0));
    assertFalse("collision", list.contains(42L));
  }

  /** Tests the @link {@link ImmutableMap#contains(Object)} method for branch nodes. */
  @Test public void branchContains() {
    final ImmutableMap<Number, Number> branch =
        ImmutableMap.<Number, Number>singleton(0, 0).insert(1, 0);
    assertTrue("contained", branch.contains(0));
    assertTrue("contained2", branch.contains(1));
    assertFalse("not contained", branch.contains(42));
    assertFalse("collision", branch.contains(1L));
  }

  /** Tests if the empty map is unique. */
  @Test public void uniqueEmpty() {
    assertSame("not same", mapFrom(1).delete(1), ImmutableMap.empty());
  }

  /** Replacing a value in a leaf. */
  @Test public void replaceLeaf() {
    final ImmutableMap<Integer, Integer> map1 = ImmutableMap.singleton(0, 0),
        map2 = map1.insert(0, 42);
    assertEquals("size1", 1, map1.size());
    assertEquals("size2", 1, map2.size());
    assertEquals("get", 0, (int) map1.get(0));
    assertEquals("get", 42, (int) map2.get(0));
  }

  /** Replacing a value in an overflow list. */
  @Test public void replaceList() {
    final ImmutableMap<Number, Number> list1 = ImmutableMap.<Number, Number>singleton(42,
        0).insert((short) 42, 0).insert(42L, 0), list2 = list1.insert(42, 42);
    assertEquals("size1", 3, list1.size());
    assertEquals("size2", 3, list2.size());
    assertEquals("get", 0, list1.get(42));
    assertEquals("get", 42, list2.get(42));
  }

  /** Tests {@link ImmutableMap#addAll(ImmutableMap)} of a leaf into a leaf. */
  @Test public void addLeafLeaf() {
    final ImmutableMap<Number, Number> leaf = mapFrom(0),
        replace = mapFromPairs(0, 1), conflict = mapFromPairs(0L, 2),
        other = mapFromPairs(42, 3), split = mapFromPairs(1 << ImmutableMap.BITS, 4);
    assertEquals(leaf.addAll(replace), replace);
    assertEquals(leaf.addAll(conflict), mapFromPairs(0, 0, 0L, 2));
    assertEquals(leaf.addAll(other), mapFromPairs(0, 0, 42, 3));
    assertEquals(leaf.addAll(split), mapFromPairs(0, 0, 1 << ImmutableMap.BITS, 4));
  }

  /** Tests {@link ImmutableMap#addAll(ImmutableMap)} of an overflow list into a leaf. */
  @Test public void addLeafList() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> leaf = mapFrom(0),
        replace = mapFromPairs(0, 1, 0L, 1), conflict = mapFromPairs(0L, 2, (short) 0, 2),
        other = mapFromPairs(42, 3, 42L, 3), split = mapFromPairs(next, 4, 1L * next, 4);
    assertEquals(leaf.addAll(replace), replace);
    assertEquals(leaf.addAll(conflict), mapFromPairs(0, 0, 0L, 2, (short) 0, 2));
    assertEquals(leaf.addAll(other), mapFromPairs(0, 0, 42, 3, 42L, 3));
    assertEquals(leaf.addAll(split), mapFromPairs(0, 0, next, 4, 1L * next, 4));
  }

  /** Tests {@link ImmutableMap#addAll(ImmutableMap)} of a branch into a leaf. */
  @Test public void addLeafBranch() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> leaf = mapFrom(0),
        replace = mapFromPairs(0, 1, 1, 1), conflict = mapFromPairs(0L, 2, 1, 2),
        other = mapFromPairs(1, 3, 2, 3), split = mapFromPairs(next, 4, next + 1, 4);
    assertEquals(leaf.addAll(replace), replace);
    assertEquals(leaf.addAll(conflict), mapFromPairs(0, 0, 0L, 2, 1, 2));
    assertEquals(leaf.addAll(other), mapFromPairs(0, 0, 1, 3, 2, 3));
    assertEquals(leaf.addAll(split), mapFromPairs(0, 0, next, 4, next + 1, 4));
  }

  /** Tests {@link ImmutableMap#addAll(ImmutableMap)} of a leaf into an overflow list. */
  @Test public void addListLeaf() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> list = mapFrom(0, 0L),
        replace = mapFromPairs(0, 1), conflict = mapFromPairs((short) 0, 2),
        other = mapFromPairs(1, 3), split = mapFromPairs(next, 4);
    assertEquals(list.addAll(replace), mapFromPairs(0, 1, 0L, 0L));
    assertEquals(list.addAll(conflict), mapFromPairs(0, 0, 0L, 0L, (short) 0, 2));
    assertEquals(list.addAll(other), mapFromPairs(0, 0, 0L, 0L, 1, 3));
    assertEquals(list.addAll(split), mapFromPairs(0, 0, 0L, 0L, next, 4));
  }

  /**
   * Tests {@link ImmutableMap#addAll(ImmutableMap)} of an overflow list into an overflow
   * list.
   */
  @Test public void addListList() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> list = mapFrom(0, 0L),
        replace = mapFromPairs(0, 1, 0L, 1),
        conflict = mapFromPairs((short) 0, 2, (byte) 0, 2),
        other = mapFromPairs(1, 3, 1L, 3),
        split = mapFromPairs(next, 4, 1L * next, 4);
    assertEquals(list.addAll(replace), replace);
    assertEquals(list.addAll(conflict),
        mapFromPairs(0, 0, 0L, 0L, (short) 0, 2, (byte) 0, 2));
    assertEquals(list.addAll(other), mapFromPairs(0, 0, 0L, 0L, 1, 3, 1L, 3));
    assertEquals(list.addAll(split), mapFromPairs(0, 0, 0L, 0L, next, 4, 1L * next, 4));
  }

  /**
   * Tests {@link ImmutableMap#addAll(ImmutableMap)} of a branch into an overflow list.
   */
  @Test public void addListBranch() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> list = mapFrom(0, 0L),
        replace = mapFromPairs(0, 1, 0L, 1, 1, 1),
        conflict = mapFromPairs((short) 0, 2, 1, 2),
        other = mapFromPairs(1, 3, 2, 3),
        split = mapFromPairs(next, 4, next + 1, 4);
    assertEquals(list.addAll(replace), replace);
    assertEquals(list.addAll(conflict), mapFromPairs(0, 0, 0L, 0L, (short) 0, 2, 1, 2));
    assertEquals(list.addAll(other), mapFromPairs(0, 0, 0L, 0L, 1, 3, 2, 3));
    assertEquals(list.addAll(split), mapFromPairs(0, 0, 0L, 0L, next, 4, next + 1, 4));
  }

  /**
   * Tests {@link ImmutableMap#addAll(ImmutableMap)} of a branch into an overflow list.
   */
  @Test public void addBranchLeaf() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> branch = mapFromPairs(0, 0, 1, 0),
        replace = mapFromPairs(0, 1),
        conflict = mapFromPairs((short) 0, 2),
        other = mapFromPairs(2, 3),
        split = mapFromPairs(next, 4);
    assertEquals(branch.addAll(replace), mapFromPairs(0, 1, 1, 0));
    assertEquals(branch.addAll(conflict), mapFromPairs(0, 0, 1, 0, (short) 0, 2));
    assertEquals(branch.addAll(other), mapFromPairs(0, 0, 1, 0, 2, 3));
    assertEquals(branch.addAll(split), mapFromPairs(0, 0, 1, 0, next, 4));
  }

  /**
   * Tests {@link ImmutableMap#addAll(ImmutableMap)} of a branch into an overflow list.
   */
  @Test public void addBranchList() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> branch = mapFromPairs(0, 0, 1, 0),
        replace = mapFromPairs(0, 1, 0L, 1),
        conflict = mapFromPairs((short) 0, 2, (byte) 0, 2),
        other = mapFromPairs(2, 3, 2L, 3),
        split = mapFromPairs(next, 4, 1L * next, 4);
    assertEquals(branch.addAll(replace), mapFromPairs(0, 1, 0L, 1, 1, 0));
    assertEquals(branch.addAll(conflict),
        mapFromPairs(0, 0, 1, 0, (short) 0, 2, (byte) 0, 2));
    assertEquals(branch.addAll(other), mapFromPairs(0, 0, 1, 0, 2, 3, 2L, 3));
    assertEquals(branch.addAll(split), mapFromPairs(0, 0, 1, 0, next, 4, 1L * next, 4));
  }

  /**
   * Tests {@link ImmutableMap#addAll(ImmutableMap)} of a branch into an overflow list.
   */
  @Test public void addBranchBranch() {
    final int next = 1 << ImmutableMap.BITS;
    final ImmutableMap<Number, Number> branch = mapFromPairs(0, 0, 1, 0),
        replace = mapFromPairs(0, 1, 1, 1),
        conflict = mapFromPairs(0L, 2, 1L, 2),
        other = mapFromPairs(2, 3, 3, 3),
        split = mapFromPairs(next, 4, next + 1, 4);
    assertEquals(branch.addAll(replace), replace);
    assertEquals(branch.addAll(conflict), mapFromPairs(0, 0, 1, 0, 0L, 2, 1L, 2));
    assertEquals(branch.addAll(other), mapFromPairs(0, 0, 1, 0, 2, 3, 3, 3));
    assertEquals(branch.addAll(split), mapFromPairs(0, 0, 1, 0, next, 4, next + 1, 4));
  }

  /** Tests {@link ImmutableMap#addAll(ImmutableMap)} of nodes into an empty node. */
  @SuppressWarnings("unchecked")
  @Test public void addEmpty() {
    final ImmutableMap<Number, Number> empty = mapFrom(), leaf = mapFromPairs(0, 1),
        list = mapFromPairs(0, 2, 0L, 2), branch = mapFromPairs(0, 3, 1, 3);
    for(final ImmutableMap<Number, Number> map
        : Arrays.asList(empty, leaf, list, branch)) {
      assertSame(map, empty.addAll(map));
      assertSame(map, map.addAll(empty));
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
}
