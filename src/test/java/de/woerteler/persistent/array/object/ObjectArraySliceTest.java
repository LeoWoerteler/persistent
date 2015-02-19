package de.woerteler.persistent.array.object;

import de.woerteler.persistent.array.*;

/**
 * Tests the {@link ObjectArray#subArray(long, long)} method.
 *
 * @author Leo Woerteler
 */
public class ObjectArraySliceTest extends ArraySliceTest<ObjectArray<Integer>> {
  @Override
  protected ObjectArray<Integer> emptyArray() {
    return ObjectArray.empty();
  }

  @Override
  protected void checkInvariants(final ObjectArray<Integer> arr) {
    arr.checkInvariants();
  }
}
