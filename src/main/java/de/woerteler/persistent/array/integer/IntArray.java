package de.woerteler.persistent.array.integer;

import java.util.*;

import de.woerteler.persistent.array.*;

/**
 * An array storing integer.
 *
 * @author Leo Woerteler
 */
public abstract class IntArray extends Array<Integer> {
  /** Minimum size of an int leaf. */
  static final int MIN_LEAF = 8;
  /** Maximum size of an int leaf. */
  static final int MAX_LEAF = 2 * MIN_LEAF - 1;
  /** Minimum number of elements in a digit. */
  static final int MIN_DIGIT = MIN_LEAF / 2;
  /** Maximum number of elements in a digit. */
  static final int MAX_DIGIT = MAX_LEAF + MIN_DIGIT;
  /** Maximum size of a small array. */
  static final int MAX_SMALL = 2 * MIN_DIGIT - 1;

  /**
   * The empty sequence.
   * Running time: <i>O(1)</i> and no allocation
   * @param <E> element type
   * @return (unique) instance of an empty sequence
   */
  public static <E> IntArray empty() {
    return EmptyIntArray.INSTANCE;
  }

  /**
   * Creates a singleton array containing the given element.
   * @param elem the contained element
   * @return the singleton array
   */
  public static IntArray singleton(final int elem) {
    return new SmallIntArray(new int[] { elem });
  }

  /**
   * Creates an array containing the given elements.
   * @param elems elements
   * @return the resulting array
   */
  @SafeVarargs
  public static IntArray from(final int... elems) {
    IntArray arr = IntArray.empty();
    for(final int i : elems) arr = arr.snoc(i);
    return arr;
  }

  /**
   * Creates an array containing the elements from the given {@link Iterable}.
   * @param iter the iterable
   * @return the resulting array
   */
  public static IntArray from(final Iterable<Integer> iter) {
    IntArray arr = IntArray.empty();
    for(final Integer i : iter) arr = arr.snoc(i);
    return arr;
  }

  /**
   * Creates an array containing the elements from the given {@link Iterator}.
   * @param iter the iterator
   * @return the resulting array
   */
  public static IntArray from(final Iterator<Integer> iter) {
    IntArray arr = IntArray.empty();
    while(iter.hasNext()) arr = arr.snoc(iter.next());
    return arr;
  }

  @Override
  public abstract IntArray cons(final Integer elem);

  @Override
  public abstract IntArray snoc(final Integer elem);

  @Override
  public abstract IntArray init();

  @Override
  public abstract IntArray tail();

  @Override
  public abstract IntArray subArray(final long pos, final long len);

  @Override
  public abstract IntArray reverse();

  @Override
  public abstract IntArray insertBefore(final long pos, final Integer val);

  @Override
  public abstract IntArray remove(final long pos);

  /**
   * Prepends the given elements to this array.
   * @param vals values, with length at most {@link SmallIntArray#MAX_SMALL}
   * @return resulting array
   */
  abstract IntArray consSmall(final int[] vals);

  /**
   * Returns an array containing the values at the indices {@code from} to {@code to - 1} in
   * the given array. Its length is always {@code to - from}. If {@code from} is smaller than zero,
   * the first {@code -from} entries in the resulting array are {@code null}.
   * If {@code to > arr.length} then the last {@code to - arr.length} entries are {@code null}.
   * If {@code from == 0 && to == arr.length}, the original array is returned.
   * @param arr input array
   * @param from first index, inclusive (may be negative)
   * @param to last index, exclusive (may be greater than {@code arr.length})
   * @return resulting array
   */
  static final int[] slice(final int[] arr, final int from, final int to) {
    if(from == 0 && to == arr.length) return arr;
    final int[] out = new int[to - from];
    final int in0 = Math.max(0, from), in1 = Math.min(to, arr.length);
    final int out0 = Math.max(-from, 0);
    System.arraycopy(arr, in0, out, out0, in1 - in0);
    return out;
  }

  /**
   * Concatenates the two int arrays.
   * @param as first array
   * @param bs second array
   * @return resulting array
   */
  static final int[] concat(final int[] as, final int[] bs) {
    final int l = as.length, r = bs.length, n = l + r;
    final int[] out = new int[n];
    System.arraycopy(as, 0, out, 0, l);
    System.arraycopy(bs, 0, out, l, r);
    return out;
  }

  /**
   * Checks that this array's implementation does not violate any invariants.
   * @throws AssertionError if an invariant was violated
   */
  abstract void checkInvariants();
}
