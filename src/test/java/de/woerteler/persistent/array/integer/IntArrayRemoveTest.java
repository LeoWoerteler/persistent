package de.woerteler.persistent.array.integer;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * Tests the {@link IntArray#remove(long)} method.
 *
 * @author Leo Woerteler
 */
public class IntArrayRemoveTest {
  /** Negative index on empty array. */
  @Test(expected = IndexOutOfBoundsException.class)
  public void emptyRemoveNegative() {
    IntArray.empty().remove(-1);
  }

  /** Zero index on empty array. */
  @Test(expected = IndexOutOfBoundsException.class)
  public void emptyRemoveZero() {
    IntArray.empty().remove(0);
  }

  /** Negative index on singleton array. */
  @Test(expected = IndexOutOfBoundsException.class)
  public void singletonRemoveNegative() {
    IntArray.singleton(42).remove(-1);
  }

  /** Too big index on singleton array. */
  @Test(expected = IndexOutOfBoundsException.class)
  public void singletonRemoveOne() {
    IntArray.singleton(42).remove(1);
  }

  /** Negative index on deep array. */
  @Test(expected = IndexOutOfBoundsException.class)
  public void deepRemoveNegative() {
    IntArray.singleton(0).snoc(1).remove(-1);
  }

  /** too big index on deep array. */
  @Test(expected = IndexOutOfBoundsException.class)
  public void deepRemoveTwo() {
    IntArray.singleton(0).snoc(1).remove(2);
  }

  /** Remove one element from singleton array. */
  @Test
  public void singletonTest() {
    final IntArray singleton = IntArray.singleton(42);
    assertSame(IntArray.empty(), singleton.remove(0));
  }

  /** Delete each element once from arrays of varying length. */
  @Test
  public void deleteOneTest() {
    final int n = 1_000;
    IntArray arr = IntArray.empty();
    for(int k = 0; k < n; k++) {
      for(int i = 0; i < k; i++) {
        final IntArray arr2 = arr.remove(i);
        final Iterator<Integer> iter = arr2.iterator();
        for(int j = 0; j < k - 1; j++) {
          assertTrue(iter.hasNext());
          assertEquals(j < i ? j : j + 1, iter.next().intValue());
        }
        assertFalse(iter.hasNext());
      }
      arr = arr.snoc(k);
      assertEquals(k + 1, arr.size());
      assertEquals(k, arr.last().intValue());
    }
  }

  /** Delete elements so that the middle tree collapses. */
  @Test
  public void collapseMiddleTest() {
    final IntArray arr = IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 8);

    IntArray arr2 = arr.tail();
    arr2 = arr2.remove(4);
    arr2 = arr2.remove(2);
    assertEquals(IntArray.from(1, 2, 4, 6, 7, 8), arr2);

    IntArray arr3 = arr.cons(-1).snoc(9);
    arr3 = arr3.remove(5);
    arr3 = arr3.remove(5);
    assertEquals(IntArray.from(-1, 0, 1, 2, 3, 6, 7, 8, 9), arr3);

    IntArray arr4 = arr.cons(-1);
    arr4 = arr4.remove(5);
    arr4 = arr4.remove(5);
    assertEquals(IntArray.from(-1, 0, 1, 2, 3, 6, 7, 8), arr4);
  }

  /** Delete elements so that the left digit is emptied. */
  @Test
  public void emptyLeftDigitTest() {
    IntArray arr = IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 8);
    arr = arr.remove(0);
    arr = arr.remove(0);
    arr = arr.remove(0);
    arr = arr.remove(0);
    assertEquals(IntArray.from(4, 5, 6, 7, 8), arr);
  }

  /** Delete elements so that the right digit is emptied. */
  @Test
  public void emptyRightDigitTest() {
    IntArray arr = IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 8);
    arr = arr.remove(8);
    arr = arr.remove(7);
    arr = arr.remove(6);
    arr = arr.remove(5);
    assertEquals(IntArray.from(0, 1, 2, 3, 4), arr);

    IntArray arr2 = IntArray.from(1, 2, 3, 4, 5, 6, 7, 8, 9).cons(0);
    for(int i = 9; i >= 4; i--) {
      arr2 = arr2.remove(i);
    }
    assertEquals(IntArray.from(0, 1, 2, 3), arr2);
  }

  /** Delete in the left digit of a deep node. */
  @Test
  public void deepLeftTest() {
    IntArray arr = IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    arr = arr.remove(3);
    assertEquals(IntArray.from(0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), arr);

    IntArray arr2 = IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    arr2 = arr2.remove(6);
    assertEquals(IntArray.from(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15), arr2);

    IntArray arr3 = IntArray.from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24);
    arr3 = arr3.remove(9);
    arr3 = arr3.remove(6);
    arr3 = arr3.remove(5);
    arr3 = arr3.remove(4);
    arr3 = arr3.remove(3);
    arr3 = arr3.remove(3);
    assertEquals(
        IntArray.from(0, 1, 2, 8, 10, 11, 12, 13, 14,
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24),
        arr3);

    IntArray arr4 = IntArray.from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24);
    arr4 = arr4.remove(6);
    arr4 = arr4.remove(5);
    arr4 = arr4.remove(4);
    arr4 = arr4.remove(3);
    arr4 = arr4.remove(3);
    assertEquals(IntArray.from(
        0, 1, 2, 8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24),
        arr4);

    IntArray arr5 = IntArray.from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24);
    arr5 = arr5.remove(17);
    arr5 = arr5.remove(16);
    arr5 = arr5.remove(15);
    arr5 = arr5.remove(6);
    arr5 = arr5.remove(5);
    arr5 = arr5.remove(4);
    arr5 = arr5.remove(3);
    arr5 = arr5.remove(3);
    assertEquals(IntArray.from(0, 1, 2, 8, 9, 10, 11, 12, 13, 14, 18, 19, 20, 21, 22, 23, 24),
        arr5);

    IntArray arr6 = IntArray.from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21
    );
    for(int i = 12; i >= 4; i--) {
      arr6 = arr6.remove(i);
    }
    assertEquals(IntArray.from(0, 1, 2, 3, 13, 14, 15, 16, 17, 18, 19, 20, 21), arr6);
  }

  /** Delete in the middle tree of a deep node. */
  @Test
  public void deepMiddleTest() {
    IntArray arr = IntArray.from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26);

    for(int i = 8; i >= 6; i--) {
      arr = arr.remove(i);
    }

    for(int i = 15; i >= 9; i--) {
      arr = arr.remove(i - 3);
    }

    assertEquals(
        IntArray.from(0, 1, 2, 3, 4, 5, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26), arr);

    IntArray arr2 = IntArray.from(
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30);
    for(int i = 4; i >= 0; i--) arr2 = arr2.cons(i);
    for(int i = 31; i <= 35; i++) arr2 = arr2.snoc(i);
    for(int i = 22; i >= 16; i--) arr2 = arr2.remove(i);
    assertEquals(
        IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            14, 15, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35),
        arr2);
  }

  /** Delete in the right digit of a deep node. */
  @Test
  public void deepRightTest() {
    IntArray arr =
        IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
    for(int i = 12; i >= 8; i--) arr = arr.remove(i);
    arr = arr.remove(8);
    assertEquals(IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 14, 15, 16, 17), arr);

    IntArray arr2 =
        IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
    for(int i = 12; i >= 9; i--) arr2 = arr2.remove(i);
    arr2 = arr2.remove(9);
    assertEquals(IntArray.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 14, 15, 16, 17), arr2);
  }

  /**
   * Randomly delete elements until an array is empty.
   */
  @Test
  public void fuzzyTest() {
    final int n = 200_000;
    final ArrayList<Integer> list = new ArrayList<Integer>(n);
    for(int i = 0; i < n; i++) list.add(i);

    IntArray arr = IntArray.from(list);

    final Random rng = new Random(42);
    for(int i = 0; i < n; i++) {
      final int delPos = rng.nextInt(n - i);
      list.remove(delPos);
      arr = arr.remove(delPos);
      final int size = n - i - 1;
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
