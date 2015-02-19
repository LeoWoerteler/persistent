package de.woerteler.persistent.array;

/**
 * Base class for tests targeting different implementation of {@link Array}.
 *
 * @author Leo Woerteler
 * @param <A> array type
 */
public abstract class ArrayTest<A extends Array<Integer>> {
  /**
   * Returns an empty array of the desired type.
   * @return empty array
   */
  protected abstract A emptyArray();

  /**
   * Checks the invariants of the given array.
   * @param arr array to check
   */
  protected abstract void checkInvariants(final A arr);
}
