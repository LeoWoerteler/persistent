package de.woerteler.persistent.array.integer;

import de.woerteler.persistent.fingertree.*;

/**
 * A builder for creating an {@link IntArray} by prepending and appending elements.
 *
 * @author Leo Woerteler
 */
public final class IntArrayBuilder {
  /** Capacity of the root. */
  private static final int CAP = 2 * IntArray.MAX_DIGIT;
  /** Size of inner nodes. */
  private static final int NODE_SIZE = (IntArray.MIN_LEAF + IntArray.MAX_LEAF + 1) / 2;

  /** Ring buffer containing the root-level elements. */
  private final int[] vals = new int[CAP];

  /** Number of elements in left digit. */
  private int inLeft;
  /** Middle between left and right digit in the buffer. */
  private int mid = CAP / 2;
  /** Number of elements in right digit. */
  private int inRight;
  /** Builder for the middle tree. */
  private final FingerTreeBuilder<Integer> tree = new FingerTreeBuilder<>();

  /**
   * Adds an element to the start of the array.
   * @param elem element to add
   */
  public void prepend(final int elem) {
    if(inLeft < IntArray.MAX_DIGIT) {
      // just insert the element
      vals[(mid - inLeft + CAP - 1) % CAP] = elem;
      inLeft++;
    } else if(tree.isEmpty() && inRight < IntArray.MAX_DIGIT) {
      // move the middle to the left
      mid = (mid + CAP - 1) % CAP;
      vals[(mid - inLeft + CAP) % CAP] = elem;
      inRight++;
    } else {
      // push leaf node into the tree
      final int[] leaf = new int[NODE_SIZE];
      final int start = (mid - NODE_SIZE + CAP) % CAP;
      for(int i = 0; i < NODE_SIZE; i++) leaf[i] = vals[(start + i) % CAP];
      tree.prepend(new IntLeaf(leaf));

      // move rest of the nodes to the right
      final int rest = inLeft - NODE_SIZE;
      final int p0 = (mid - inLeft + CAP) % CAP;
      for(int i = 0; i < rest; i++) {
        final int from = (p0 + i) % CAP, to = (from + NODE_SIZE) % CAP;
        vals[to] = vals[from];
      }

      // insert the element
      vals[(mid - rest + CAP - 1) % CAP] = elem;
      inLeft = rest + 1;
    }
  }

  /**
   * Adds an element to the end of the array.
   * @param elem element to add
   */
  public void append(final int elem) {
    if(inRight < IntArray.MAX_DIGIT) {
      // just insert the element
      vals[(mid + inRight) % CAP] = elem;
      inRight++;
    } else if(tree.isEmpty() && inLeft < IntArray.MAX_DIGIT) {
      // move the middle to the right
      mid = (mid + 1) % CAP;
      vals[(mid + inRight + CAP - 1) % CAP] = elem;
      inLeft++;
    } else {
      // push leaf node into the tree
      final int[] leaf = new int[NODE_SIZE];
      final int start = mid;
      for(int i = 0; i < NODE_SIZE; i++) leaf[i] = vals[(start + i) % CAP];
      tree.append(new IntLeaf(leaf));

      // move rest of the nodes to the right
      final int rest = inRight - NODE_SIZE;
      for(int i = 0; i < rest; i++) {
        final int to = (mid + i) % CAP, from = (to + NODE_SIZE) % CAP;
        vals[to] = vals[from];
      }

      // insert the element
      vals[(mid + rest) % CAP] = elem;
      inRight = rest + 1;
    }
  }

  /**
   * Creates an {@link IntArray} containing the elements of this builder.
   * @return resulting array
   */
  public IntArray freeze() {
    final int n = inLeft + inRight;
    if(n == 0) return IntArray.empty();

    final int start = (mid - inLeft + CAP) % CAP;
    if(n <= IntArray.MAX_SMALL) {
      // small int array, fill directly
      final int[] small = new int[n];
      for(int i = 0; i < n; i++) small[i] = vals[(start + i) % CAP];
      return new SmallIntArray(small);
    }

    // deep array
    final int a = tree.isEmpty() ? n / 2 : inLeft, b = n - a;
    final int[] ls = new int[a], rs = new int[b];
    for(int i = 0; i < a; i++) ls[i] = vals[(start + i) % CAP];
    for(int i = a; i < n; i++) rs[i - a] = vals[(start + i) % CAP];
    return new DeepIntArray(ls, tree.freeze(), rs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
    if(tree.isEmpty()) {
      final int n = inLeft + inRight, first = (mid - inLeft + CAP) % CAP;
      if(n > 0) {
        sb.append(vals[first]);
        for(int i = 1; i < n; i++) sb.append(", ").append(vals[(first + i) % CAP]);
      }
      return sb.append(']').toString();
    }

    final int first = (mid - inLeft + CAP) % CAP;
    sb.append(vals[first]);
    for(int i = 1; i < inLeft; i++) sb.append(", ").append(vals[(first + i) % CAP]);
    for(final Integer val : tree) sb.append(", ").append(val);
    for(int i = 0; i < inRight; i++) sb.append(", ").append(vals[(mid + i) % CAP]);
    return sb.append(']').toString();
  }
}
