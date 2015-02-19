package de.woerteler.persistent.array.integer;

import de.woerteler.persistent.array.*;

/**
 * Tests for {@link IntArray#reverse()}.
 *
 * @author Leo Woerteler
 */
public class IntReverseTest extends ReverseTest<IntArray> {
  @Override
  protected IntArray emptyArray() {
    return IntArray.empty();
  }

  @Override
  protected void checkInvariants(final IntArray arr) {
    arr.checkInvariants();
  }
}
