package de.woerteler.persistent.array.integer;

import de.woerteler.persistent.array.*;

/**
 * Tests for  {@link Array#concat(Array)}.
 *
 * @author Leo Woerteler
 */
public class IntArrayConcatTest extends ArrayConcatTest<IntArray> {
  @Override
  protected IntArray emptyArray() {
    return IntArray.empty();
  }

  @Override
  protected void checkInvariants(final IntArray arr) {
    arr.checkInvariants();
  }
}
