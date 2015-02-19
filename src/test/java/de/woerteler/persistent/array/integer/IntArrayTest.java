package de.woerteler.persistent.array.integer;

import de.woerteler.persistent.array.*;

/**
 * Tests for the {@link IntArray} data structure.
 *
 * @author Leo Woerteler
 */
public class IntArrayTest extends VariousArrayTest<IntArray> {
  @Override
  protected IntArray emptyArray() {
    return IntArray.empty();
  }

  @Override
  protected void checkInvariants(final IntArray arr) {
    arr.checkInvariants();
  }
}
