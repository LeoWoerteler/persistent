package de.woerteler.persistent.array.integer;

import de.woerteler.persistent.array.*;

/**
 * Tests for {@link IntArray#insertBefore(long, Integer)}.
 *
 * @author Leo Woerteler
 */
public class IntArrayInsertTest extends ArrayInsertTest<IntArray> {
  @Override
  protected IntArray emptyArray() {
    return IntArray.empty();
  }

  @Override
  protected void checkInvariants(final IntArray arr) {
    arr.checkInvariants();
  }
}
