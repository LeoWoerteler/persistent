package de.woerteler.persistent.array.integer;

import static org.junit.Assert.*;

import org.junit.*;

import de.woerteler.persistent.array.*;

/**
 * Tests the {@link Array#subArray(long, long)} method.
 *
 * @author Leo Woerteler
 */
public class IntArraySliceTest {
  /**
   * Exhaustively tests creating sub-arrays of arrays of a range of lengths.
   */
  @Test
  public void testSlice() {
    IntArray arr = IntArray.empty();
    for(int len = 0; len < 300; len++) {
      assertEquals(len, arr.size());
      for(int pos = 0; pos < len; pos++) {
        for(int k = 0; k <= len - pos; k++) {
          final IntArray sub = arr.subArray(pos, k);
          assertEquals(k, sub.size());
          sub.checkInvariants();
          checkRange(sub, pos, k);
        }
      }
      arr = arr.snoc(len);
    }
  }

  /**
   * Checks that the given array contains the integers in the range {@code [pos .. pos + k - 1]}.
   * @param sub array
   * @param pos first element
   * @param k number of elements
   */
  private static void checkRange(final Array<Integer> sub, final int pos, final int k) {
    for(int i = 0; i < k; i++) {
      final int res = sub.get(i).intValue();
      if(res != pos + i) {
        fail("Wrong value: " + res + " vs. " + (pos + i));
      }
    }
  }
}
