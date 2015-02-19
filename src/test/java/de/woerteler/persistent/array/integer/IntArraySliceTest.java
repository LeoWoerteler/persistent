package de.woerteler.persistent.array.integer;

import de.woerteler.persistent.array.*;

/**
 * Tests the {@link IntArray#subArray(long, long)} method.
 *
 * @author Leo Woerteler
 */
public class IntArraySliceTest extends ArraySliceTest<IntArray> {
  @Override
  protected IntArray emptyArray() {
    return IntArray.empty();
  }

  @Override
  protected void checkInvariants(final IntArray arr) {
    arr.checkInvariants();
  }
}
