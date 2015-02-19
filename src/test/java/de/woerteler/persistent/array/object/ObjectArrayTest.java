package de.woerteler.persistent.array.object;

import de.woerteler.persistent.array.*;

/**
 * Tests for {@link ObjectArray}.
 *
 * @author Leo Woerteler
 */
public class ObjectArrayTest extends VariousArrayTest<ObjectArray<Integer>> {
  @Override
  protected ObjectArray<Integer> emptyArray() {
    return ObjectArray.empty();
  }

  @Override
  protected void checkInvariants(final ObjectArray<Integer> arr) {
    arr.checkInvariants();
  }
}
