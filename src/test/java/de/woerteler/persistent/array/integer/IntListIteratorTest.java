package de.woerteler.persistent.array.integer;

import de.woerteler.persistent.array.*;

/**
 * Tests for {@link IntArray#listIterator(long)}.
 *
 * @author Leo Woerteler
 */
public class IntListIteratorTest extends ListIteratorTest<IntArray> {
  @Override
  protected IntArray emptyArray() {
    return IntArray.empty();
  }

  @Override
  protected void checkInvariants(final IntArray arr) {
    arr.checkInvariants();
  }
}
